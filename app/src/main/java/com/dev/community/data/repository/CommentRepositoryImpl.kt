package com.dev.community.data.repository

import android.content.Context
import android.util.Log
import com.dev.community.data.model.Comment
import com.dev.community.data.model.Post
import com.dev.community.data.model.User
import com.dev.community.util.Result
import com.dev.community.ui.notice.fcm.RetrofitInstance
import com.dev.community.ui.notice.fcm.model.Message
import com.dev.community.ui.notice.fcm.model.NotificationData
import com.dev.community.ui.notice.fcm.model.PushNotification
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val database: DatabaseReference,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : CommentRepository {

    private val userUid: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("No user is logged in")


    // 댓글 추가 - commentIdx에 해당하는 경로에 Comment 객체를 저장
    override suspend fun addComment(
        postId: String,
        nickname: String,
        content: String,
        parentId: String,
    ): Result<Boolean> {
        return try {
            // 고유한 commentId 생성
            val commentRef = database.child("comment").push()
            val commentId = commentRef.key ?: throw Exception("Could not generate comment ID")

            val comment = Comment(
                commentId = commentId, postId = postId, parentId = parentId,
                uid = userUid, nickname = nickname, content = content
            )
            // 댓글 저장
            commentRef.setValue(comment).await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


   override suspend fun sendPushAlarm(postId: String, content: String) {
        val postRef = database.child("post").child(postId)
        val postSnapshot = postRef.get().await()
        val post = postSnapshot.getValue(Post::class.java) ?: return

        val userRef = database.child("user").child(post.uid)
        val userSnapshot = userRef.get().await()
        val user = userSnapshot.getValue(User::class.java) ?: return

        if (user.alarm && user.uid != userUid ) {
            withContext(Dispatchers.IO) {
                sendFcmNotification(content, user.token)
            }
        }else {
            Log.e("FCM", "FCM token is null or empty")
        }

    }

    private fun getAccessToken(): String {
        val assetManager = context.assets
        val inputStream = assetManager.open("serviceAccountKey.json")

        val googleCredentials = GoogleCredentials
            .fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

        googleCredentials.refreshIfExpired()  // 만료된 경우 새로고침
        return googleCredentials.accessToken.tokenValue
    }

    private suspend fun sendFcmNotification(message: String, token: String) {
        val notificationData = NotificationData(
            title = "새로운 댓글이 달렸어요!",
            body = message
        )

        val messages = Message(
            token = token,
            notification = notificationData
        )

        val pushNotification = PushNotification(
            message = messages
        )

        val response = RetrofitInstance.api.postNotification(token = "Bearer ${getAccessToken()}",pushNotification)
        if (response.isSuccessful) {
            Log.d("FCM", "SUCCESS: ${response.message()}")
        } else {
            Log.e("FCM", "Error: ${response.errorBody()?.string() }")
        }

    }


    // postId에 해당하는 모든 댓글 가져옴
    override suspend fun getComments(postId: String): Flow<Result<List<Comment>>> = callbackFlow {

        val commentRef = database.child("comment").orderByChild("postId").equalTo(postId)
        val commentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = snapshot.children.mapNotNull { it.getValue(Comment::class.java) }
                launch {
                    trySend(Result.Success(groupComments(comments)))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.Error(error.message)).isFailure
            }
        }

        commentRef.addValueEventListener(commentListener)
        awaitClose { commentRef.removeEventListener(commentListener) }
    }


    private suspend fun groupComments(comments: List<Comment>): List<Comment> {
        // 댓글을 parentId를 키로 그룹화
        val commentMap: Map<String, List<Comment>> = comments.groupBy { it.parentId }

        // 자식 댓글을 재귀적으로 추가하는 함수
        suspend fun addReplies(comment: Comment): List<Comment> {
            val replies = commentMap[comment.commentId]?.sortedBy { it.date } ?: emptyList()
            val completeReplies = replies.flatMap { addReplies(it) }

            // 부모 댓글일 때 자식 댓글들의 content가 모두 null인지 확인
            if (comment.parentId.isEmpty() && comment.content.isEmpty() && completeReplies.all { it.content.isEmpty() }) {
                deleteCommentRecursively(comment.commentId)
                return emptyList()
            }

            return listOf(comment) + completeReplies
        }

        // 루트 댓글(부모 댓글이 없는 댓글)을 찾아서 정렬한 후 자식 댓글을 추가
        return (commentMap[""] ?: emptyList()).sortedBy { it.date }.flatMap { addReplies(it) }
    }


    // 댓글과 자식 댓글을 재귀적으로 삭제
    private suspend fun deleteCommentRecursively(commentId: String) {
        val commentRef = database.child("comment").child(commentId)
        val repliesSnapshot = database.child("comment")
            .orderByChild("parentId").equalTo(commentId).get().await()

        if (repliesSnapshot.exists()) {
            repliesSnapshot.children.forEach { replySnapshot ->
                val replyCommentId = replySnapshot.key ?: return@forEach
                deleteCommentRecursively(replyCommentId)
            }
        }
        commentRef.removeValue()
    }


    override suspend fun deleteComment(commentId: String, parentId: String): Result<Boolean> {
        return try {
            val commentRef = database.child("comment").child(commentId)
            val commentSnapshot = commentRef.get().await()

            if (!commentSnapshot.exists()) {
                return Result.Error("Comment not found")
            }

            if (parentId.isEmpty()) {
                // 부모 댓글인 경우 자식 댓글 확인
                val repliesSnapshot = database.child("comment")
                    .orderByChild("parentId").equalTo(commentId).get().await()

                if (repliesSnapshot.exists()) {
                    // 자식 댓글이 있는 경우: 댓글 내용을 null로 설정
                    commentRef.child("content").setValue(null).await()
                } else {
                    // 자식 댓글이 없는 경우: 댓글 삭제
                    commentRef.removeValue().await()
                }
            } else {
                // 자식 댓글인 경우: 댓글 내용을 null로 설정
                commentRef.child("content").setValue(null).await()
            }
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override suspend fun getNoticeComments(): Result<List<Comment>> {
        return try {
            // 내가 쓴 게시물 리스트 가져오기
            val postList = database.child("post").orderByChild("uid").equalTo(userUid)
                .get().await().children.mapNotNull { it.getValue(Post::class.java) }

            // 게시물 리스트의 각 게시물에 대한 댓글 리스트 가져오기
            val allComments = mutableListOf<Comment>()
            postList.forEach { post ->
                val comments = database.child("comment").orderByChild("postId")
                    .equalTo(post.postId).get().await().children.mapNotNull {
                        val comment = it.getValue(Comment::class.java)
                        if (comment?.uid != userUid && comment?.content != null) comment else null // 내가 쓴 댓글은 제외
                    }
                allComments.addAll(comments)
            }

            val sortedList = allComments.sortedByDescending { it.date }

            // 3. 최종 댓글 리스트를 Result로 반환
            Result.Success(sortedList)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


}
