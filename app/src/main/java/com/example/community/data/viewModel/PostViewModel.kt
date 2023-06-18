package com.example.community.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community.data.entity.Post
import com.example.community.data.repository.PostRepository
import kotlinx.coroutines.launch


class PostViewModel : ViewModel() {

    private val postRepo = PostRepository()

    private var _addPostState = MutableLiveData<Boolean>()
    val addPostState: LiveData<Boolean> = _addPostState

    private var _deletePostState = MutableLiveData<Boolean>()
    val deletePostState: LiveData<Boolean> = _deletePostState

    fun getLatestPost(): MutableLiveData<Post?> {
        return postRepo.getLatestPost()
    }

    fun addPost(
        postIdx: Int,
        age: Int,
        location: String,
        uid: String,
        nickname: String,
        date: String,
        time: String,
        view: Int,
        title: String,
        content: String,
        imgs: List<String>
    ) {
        if (checkAddNull(title, content)) {
            val post =
                Post(postIdx, age, location, uid, nickname, date, time, view, title, content, imgs)
            postRepo.addPost(postIdx, post) { success ->
                if (success) {
                    _addPostState.postValue(true)
                } else {
                    _addPostState.postValue(false)
                }
            }
        } else {
            return
        }

    }

    private fun checkAddNull(
        title: String,
        content: String

    ): Boolean {
        return !(title.isEmpty() && content.isEmpty())
    }

    fun getLocationPost(userLocation: String): MutableLiveData<MutableList<Post>> {
        return postRepo.getLocationPost(userLocation)
    }

    fun updatePostCnt(postIdx: Int) {
        viewModelScope.launch {
            postRepo.updatePostCnt(postIdx)
        }
    }

    fun deletePost(postIdx: Int) {
        viewModelScope.launch {
            postRepo.deletePost(postIdx) { success ->
                if (success) {
                    _deletePostState.postValue(true)
                } else {
                    _deletePostState.postValue(false)
                }
            }
        }
    }
}