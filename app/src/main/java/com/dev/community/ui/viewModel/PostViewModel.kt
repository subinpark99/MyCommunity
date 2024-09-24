package com.dev.community.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.community.data.model.Post
import com.dev.community.data.repository.PostRepository
import com.dev.community.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepo: PostRepository,
) : ViewModel() {

    private val _postState = MutableStateFlow<Result<Post>>(Result.Loading)
    val postState: StateFlow<Result<Post>> = _postState.asStateFlow()

    private val _addImageState = MutableSharedFlow<Result<String>>()
    val addImageState = _addImageState.asSharedFlow()

    private val _postsState = MutableStateFlow<Result<List<Post>>>(Result.Loading)
    val postsState: StateFlow<Result<List<Post>>> = _postsState.asStateFlow()

    private val _deletePostState = MutableSharedFlow<Result<Boolean>>()
    val deletePostState = _deletePostState.asSharedFlow()


    // 게시물 추가
    fun addPost(age: Int, location: String, nickname: String, title: String, content: String) {
        viewModelScope.launch {
            val result = postRepo.addPost(age, location, nickname, title, content)
            _postState.value = result
        }
    }

    // 이미지 추가
    fun addImage(postId: String, imageList: List<String>) {
        viewModelScope.launch {
            val result = postRepo.addImage(postId, imageList)
            _addImageState.emit(result)

        }
    }

    // 게시물 가져오기
    fun getPostById(postId: String) {
        viewModelScope.launch {
            val result = postRepo.getPostById(postId)
            _postState.value = result

        }
    }

    // 위치 기반 게시물 리스트 가져오기
    fun getLocationPosts(location: String) {
        viewModelScope.launch {
            val result = postRepo.getLocationPosts(location)
            _postsState.value = result
        }
    }

    // 사용자 게시물 리스트 가져오기
    fun getMyPosts() {
        viewModelScope.launch {
            val result = postRepo.getMyPosts()
            _postsState.value = result
        }
    }

    // 사용자가 댓글을 단 게시물 리스트 가져오기
    fun getMyCommentedPosts() {
        viewModelScope.launch {
            val result = postRepo.getMyCommentedPosts()
            _postsState.value=result
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
