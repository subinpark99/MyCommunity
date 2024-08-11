package com.dev.community.data.repository


import com.dev.community.data.model.Comment
import com.dev.community.util.Result
import kotlinx.coroutines.flow.Flow


interface CommentRepository {
    suspend fun addComment(
        postId: String,
        nickname: String,
        content: String,
        parentId: String,
        alarm: Boolean
    ): Result<Boolean>

    suspend fun getComments(postId: String): Flow<Result<List<Comment>>>
    suspend fun getNoticeComments(): Flow<Result<Comment>>
    suspend fun deleteComment(commentId: String, parentId: String): Result<Boolean>
}