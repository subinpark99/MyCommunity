package com.dev.community.data.repository


import androidx.core.net.toUri
import com.dev.community.data.model.Comment
import com.dev.community.data.model.Post
import com.dev.community.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val database: DatabaseReference,
    private val auth: FirebaseAuth,
    private val storage: StorageReference
) : PostRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("No user is logged in")

    override suspend fun addPost(
        age: Int,
        location: String,
        nickname: String,
        title: String,
        content: String,
    ): Result<Post> {
        return try {

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
            Result.Success(post)

        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun addImage(postId: String, imageList: List<String>): Result<String> {
        return try {
            val storageRef = storage.child("images/$postId")

            // stoarge에 저장
            val downloadUrls = imageList.mapIndexed { index, imgUri ->
                val imageRef = storageRef.child("image_$index")
                val uploadTask = imageRef.putFile(imgUri.toUri()).await()
                uploadTask.storage.downloadUrl.await().toString() // 다운로드 URL 가져오기
            }

            // Realtime Database에 저장
            database.child("post").child(postId).child("imageList").setValue(downloadUrls).await()

            Result.Success(postId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override suspend fun getPostById(postId: String): Result<Post> {
        return try {
            val post = database.child("post").child(postId).get().await().getValue(Post::class.java)
                ?: throw Exception("Post not found")
            Result.Success(post)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getLocationPosts(location: String): Result<List<Post>> {
        return try {
            val postRef = database.child("post").orderByChild("location").equalTo(location)
            val snapshot = postRef.get().await()
            val postList = snapshot.children.reversed().mapNotNull { it.getValue(Post::class.java) }

            Result.Success(postList)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getMyPosts(): Result<List<Post>> {
        return try {
            val postList = database.child("post").orderByChild("uid").equalTo(userId)
                .get().await().children.reversed().mapNotNull { it.getValue(Post::class.java) }

            Result.Success(postList)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getMyCommentedPosts(): Result<List<Post>> {
        return try {
            val commentRef = database.child("comment").orderByChild("uid").equalTo(userId)
            val commentSnapshot = commentRef.get().await()
            val comments =
                commentSnapshot.children.reversed().mapNotNull { it.getValue(Comment::class.java) }

            // 댓글에서 게시물 ID를 추출하고 중복 제거
            val postIds = comments.map { it.postId }.distinct()

            val postList = coroutineScope {
                postIds.map { postId ->
                    async {
                        database.child("post").child(postId).get().await()
                            .getValue(Post::class.java)
                    }
                }.awaitAll().filterNotNull()
            }

            Result.Success(postList)
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