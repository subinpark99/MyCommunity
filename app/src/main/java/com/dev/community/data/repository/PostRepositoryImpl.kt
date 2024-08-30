package com.dev.community.data.repository

import android.util.Log
import androidx.core.net.toUri
import com.dev.community.data.model.Comment
import com.dev.community.data.model.Post
import com.dev.community.data.model.PostWithImages
import com.dev.community.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PostRepositoryImpl @Inject constructor(
    private val database: DatabaseReference,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    private val firestore: FirebaseFirestore
) : PostRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("No user is logged in")

    override suspend fun addPost(
        age: Int,
        location: String,
        nickname: String,
        title: String,
        content: String,
        imageList: List<String>
    ): Result<Post> {
        return try {
            // 고유한 postId 생성
            val postRef = database.child("post").push()
            val postId = postRef.key ?: throw Exception("Could not generate post ID")
            val post = Post(
                postId = postId,
                age = age,
                location = location,
                nickname = nickname,
                title = title,
                content = content,
                uid = userId
            )

            // 게시물 저장
            postRef.setValue(post).await()

            // 이미지 업로드가 있을 때, 업로드 결과를 기다림
            val uploadResult = if (imageList.isNotEmpty()) {
                uploadImagesToFirestore(postId, imageList)
            } else {
                Result.Success(true)
            }

            if (uploadResult is Result.Error) {
                // 이미지 업로드 실패 시, 게시물 삭제
                database.child("post").child(postId).removeValue().await()
                return uploadResult
            }

            Result.Success(post)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun uploadImagesToFirestore(
        postId: String,
        imageList: List<String>
    ): Result<Boolean> {
        return try {
            // Storage에 실제 이미지 저장
            val storageRef = storage.child("images/$postId")
            val imageUrls = imageList.mapIndexed { index, img ->
                val imageRef = storageRef.child("image_$index")
                val uploadTask = imageRef.putFile(img.toUri()).await()
                uploadTask.storage.downloadUrl.await().toString()
            }

            // Firestore에 이미지 URL 저장
            firestore.collection("postImages").document(postId)
                .set(mapOf("imageUrls" to imageUrls)).await()

            Result.Success(true)

        } catch (e: Exception) {
            Log.e("Upload Error", "Error uploading images", e)
            Result.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchPostWithImages(postId: String): PostWithImages {
        val post = database.child("post").child(postId).get().await().getValue(Post::class.java)
            ?: throw Exception("Post not found")

        val imageUrls = firestore.collection("postImages").document(postId)
            .get().await().get("imageUrls") as List<String>? ?: emptyList()

        return PostWithImages(post, imageUrls)
    }

    override suspend fun getMyPostsWithImages(): Result<List<PostWithImages>> {
        return try {
            val postList = database.child("post").orderByChild("uid").equalTo(userId)
                .get().await().children.reversed().mapNotNull { it.getValue(Post::class.java) }


            // 각 게시물의 이미지 URL을 비동기적으로 가져옴
            val postsWithImages = coroutineScope {
                postList.map { post ->
                    async { fetchPostWithImages(post.postId) }
                }.awaitAll()
            }

            Result.Success(postsWithImages)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    // 현재 사용자가 댓글을 단 게시물과 관련된 이미지 URL 리스트를 가져옴
    override suspend fun getMyCommentedPostsWithImages(): Result<List<PostWithImages>> {
        return try {
            val commentRef = database.child("comment").orderByChild("uid").equalTo(userId)
            val commentSnapshot = commentRef.get().await()
            val comments =
                commentSnapshot.children.reversed().mapNotNull { it.getValue(Comment::class.java) }

            // 댓글에서 게시물 ID를 추출하고 중복 제거
            val postIds = comments.map { it.postId }.distinct()

            val postsWithImages = coroutineScope {
                postIds.map { postId ->
                    async { fetchPostWithImages(postId) }
                }.awaitAll()
            }

            Result.Success(postsWithImages)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    //  특정 위치에 기반한 게시물과 관련된 이미지 URL 리스트를 가져오기
    override suspend fun getLocationPostsWithImages(location: String): Result<List<PostWithImages>> {
        return try {
            val postRef = database.child("post").orderByChild("location").equalTo(location)
            val snapshot = postRef.get().await()
            val postList = snapshot.children.reversed().mapNotNull { it.getValue(Post::class.java) }

            val postsWithImages = coroutineScope {
                postList.map { post ->
                    async { fetchPostWithImages(post.postId) }
                }.awaitAll()
            }

            Result.Success(postsWithImages)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getPostWithImages(postId: String): Result<PostWithImages> {
        return try {
            val postWithImages = fetchPostWithImages(postId)
            Result.Success(postWithImages)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


   // 게시물 조회 수 업데이트
    override suspend fun updatePostCnt(postId: String) {
        val updateCntRef = database.child("post").child(postId)
        val snapshot = updateCntRef.get().await()

        val post = snapshot.getValue(Post::class.java)
        if (post != null) {
            post.view += 1
            updateCntRef.setValue(post).await()
        }
    }

   // 특정 postIdx에 해당하는 게시물과 해당 postId를 가진 모든 댓글을 삭제
    override suspend fun deletePost(postIdx: String): Result<Boolean> {
        return try {
            val postRef = database.child("post").child(postIdx)
            val snapshot = postRef.get().await()
            val post = snapshot.getValue(Post::class.java)

            if (post?.uid == userId) {
                // 게시물 삭제
                postRef.removeValue().await()

                // postId가 postIdx인 모든 댓글 삭제
                val commentsRef = database.child("comment")
                val commentsQuery = commentsRef.orderByChild("postId").equalTo(postIdx)
                val commentsSnapshot = commentsQuery.get().await()

                for (comment in commentsSnapshot.children) {
                    comment.ref.removeValue()
                }

                Result.Success(true)
            } else {
                Result.Error("Permission denied")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

}