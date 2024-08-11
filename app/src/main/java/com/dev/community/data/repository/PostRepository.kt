package com.dev.community.data.repository


import com.dev.community.data.model.Post
import com.dev.community.data.model.PostWithImages
import com.dev.community.util.Result

interface PostRepository {

    suspend fun addPost(
        age: Int,
        location: String,
        nickname: String,
        title: String,
        content: String,
        imageList: List<String>
    ): Result<Post>
    suspend fun getPostWithImages(postId: String): Result<PostWithImages>
    suspend fun getLocationPostsWithImages(location: String): Result<List<PostWithImages>>
    suspend fun getMyPostsWithImages(): Result<List<PostWithImages>>
    suspend fun getMyCommentedPostsWithImages(): Result<List<PostWithImages>>
    suspend fun updatePostCnt(postId: String)
    suspend fun deletePost(postIdx: String): Result<Boolean>

}
