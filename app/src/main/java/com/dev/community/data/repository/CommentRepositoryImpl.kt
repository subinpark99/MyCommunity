package com.dev.community.data.repository

import com.dev.community.data.model.Comment
import com.dev.community.data.model.Post
import com.dev.community.data.model.User
import com.dev.community.util.Result
import com.dev.community.ui.notice.fcm.RetrofitInstance
import com.dev.community.ui.notice.fcm.model.NotificationData
import com.dev.community.ui.notice.fcm.model.PushNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val database: DatabaseReference,
    private val auth: FirebaseAuth,
) : CommentRepository {

    private val userUid: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("No user is logged in")


    // 댓글 추가 - commentIdx에 해당하는 경로에 Comment 객체를 저장
    override suspend fun addComment(
        postId: String,
        nickname: String,
        content: String,
        parentId: String,
        alarm: Boolean
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

            if (alarm) {
                CoroutineScope(Dispatchers.IO).launch {
                    sendPushAlarm(comment.postId, comment.content)
                }
            }

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
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


    override suspend fun getNoticeComments(): Flow<Result<Comment>> =
        callbackFlow {
            val postsSnapshot =
                database.child("post").orderByChild("uid").equalTo(userUid).get().await()

            // 포스트 데이터가 없을 경우 Error 반환
            if (!postsSnapshot.exists() || postsSnapshot.childrenCount == 0L) {
                trySend(Result.Error("No posts found for the given user UID"))
                close()  // Flow 종료
                return@callbackFlow
            }

            val commentListener = object : ChildEventListener {
                override fun onChildAdded(
                    commentSnapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    val comment = commentSnapshot.getValue(Comment::class.java) ?: return
                    if (userUid != comment.uid && comment.content.isNotEmpty()) trySend(
                        Result.Success(
                            comment
                        )
                    )
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    trySend(Result.Error(error.message))
                }
            }

            // 포스트에 대해 댓글 리스너를 추가
            var hasComments = false
            for (postSnapshot in postsSnapshot.children) {
                val postId = postSnapshot.key ?: continue
                val commentsRef = database.child("comment").orderByChild("postId").equalTo(postId)

                commentsRef.addChildEventListener(commentListener)

                // 첫 번째 댓글이 없는 경우 확인
                val commentsSnapshot = commentsRef.get().await()
                if (commentsSnapshot.exists()) {
                    hasComments = true
                }
            }

            // 댓글이 없을 경우 Error 반환
            if (!hasComments) {
                trySend(Result.Error("No comments found for the user's posts"))
                close()  // Flow 종료
                return@callbackFlow
            }


            awaitClose {
                for (postSnapshot in postsSnapshot.children) {
                    val postId = postSnapshot.key ?: continue
                    val commentsRef =
                        database.child("comment").orderByChild("postId").equalTo(postId)
                    commentsRef.removeEventListener(commentListener)
                }
            }
        }


    private suspend fun sendPushAlarm(postId: String, content: String) {
        val postRef = database.child("post").child(postId)
        val postSnapshot = postRef.get().await()
        val post = postSnapshot.getValue(Post::class.java) ?: return

        val userRef = database.child("user").child(post.uid)
        val userSnapshot = userRef.get().await()
        val user = userSnapshot.getValue(User::class.java) ?: return

        if (user.alarm && user.uid != userUid) {
            sendFcmNotification(content, user.token)
        }
    }

    private suspend fun sendFcmNotification(message: String, token: String) {
        val notification = PushNotification(
            data = NotificationData("새로운 댓글이 달렸어요!", message),
            to = token
        )
        RetrofitInstance.api.postNotification(notification)
    }
}
