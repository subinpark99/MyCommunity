package com.dev.community.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.community.data.model.Post
import com.dev.community.data.model.PostWithImages
import com.dev.community.data.repository.PostRepository
import com.dev.community.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepo: PostRepository
) : ViewModel() {

    private val _addPostState = MutableSharedFlow<Result<Post>>()
    val addPostState = _addPostState.asSharedFlow()

    private val _postIdState = MutableStateFlow<Result<PostWithImages>>(Result.Loading)
    val postIdState: StateFlow<Result<PostWithImages>> = _postIdState

    private val _locationPostWithImgState =
        MutableStateFlow<Result<List<PostWithImages>>>(Result.Loading)
    val locationPostWithImgState: StateFlow<Result<List<PostWithImages>>> =
        _locationPostWithImgState

    private val _myPostListState =
        MutableStateFlow<Result<List<PostWithImages>>>(Result.Loading)
    val myPostListState: StateFlow<Result<List<PostWithImages>>> = _myPostListState

    private val _myCommentsListState =
        MutableStateFlow<Result<List<PostWithImages>>>(Result.Loading)
    val myCommentsListState: StateFlow<Result<List<PostWithImages>>> =
        _myCommentsListState

    private val _deletePostState = MutableSharedFlow<Result<Boolean>>()
    val deletePostState = _deletePostState.asSharedFlow()


    fun addPost(
        age: Int,
        location: String,
        nickname: String,
        title: String,
        content: String,
        imageList: List<String>
    ) {
        viewModelScope.launch {
            val result = postRepo.addPost(age, location, nickname, title, content, imageList)
            _addPostState.emit(result)
        }
    }


    fun getPostById(postId: String) {
        viewModelScope.launch {
            val result = postRepo.getPostWithImages(postId)
            _postIdState.value = result
        }
    }

    fun getLocationPostWithImg(location: String) {
        viewModelScope.launch {
            val result = postRepo.getLocationPostsWithImages(location)
            _locationPostWithImgState.value = result
        }
    }


    fun getMyPosts() {
        viewModelScope.launch {
            val result = postRepo.getMyPostsWithImages()
            _myPostListState.value = result
        }
    }


    fun getMyCommentedPost() {
        viewModelScope.launch {
            val result = postRepo.getMyCommentedPostsWithImages()
            _myCommentsListState.value = result
        }
    }

    fun updatePostCnt(postIdx: String) {
        viewModelScope.launch {
            postRepo.updatePostCnt(postIdx)
        }
    }

    fun deletePost(postIdx: String) {
        viewModelScope.launch {
            val result = postRepo.deletePost(postIdx)
            _deletePostState.emit(result)
        }
    }

}
