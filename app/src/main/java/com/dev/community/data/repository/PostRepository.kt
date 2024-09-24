package com.dev.community.data.repository


import com.dev.community.data.model.Post
import com.dev.community.util.Result


interface PostRepository {

    suspend fun addPost(
        age: Int,
        location: String,
        nickname: String,
        title: String,
        content: String
    ): Result<Post>

    suspend fun addImage(
        postId: String,
        imageList:List<String>
    ): Result<String>

    suspend fun getPostById(postId: String): Result<Post>
    suspend fun getLocationPosts(location: String): Result<List<Post>>
    suspend fun getMyPosts(): Result<List<Post>>
    suspend fun getMyCommentedPosts(): Result<List<Post>>

    suspend fun updatePostCnt(postId: String)
    suspend fun deletePost(postIdx: String): Result<Boolean>
}
