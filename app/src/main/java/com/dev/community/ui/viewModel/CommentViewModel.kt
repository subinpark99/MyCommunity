package com.dev.community.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.community.data.model.Comment
import com.dev.community.data.repository.CommentRepository
import com.dev.community.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepo: CommentRepository
) : ViewModel() {

    private val _addCommentState = MutableSharedFlow<Result<Boolean>>()
    val addCommentState = _addCommentState.asSharedFlow()

    private val _deleteCommentState = MutableSharedFlow<Result<Boolean>>()
    val deleteCommentState = _addCommentState.asSharedFlow()

    private val _getCommentListState =
        MutableStateFlow<Result<List<Comment>>>(Result.Loading)
    val getCommentListState = _getCommentListState.asStateFlow()

    private val _getNoticeComState = MutableStateFlow<Result<Comment>>(Result.Loading)
    val getNoticeComState = _getNoticeComState.asStateFlow()


    fun addComment(
        postId: String, nickname: String, content: String, parentId: String, alarm: Boolean
    ) {
        viewModelScope.launch {
            val result = commentRepo.addComment(postId, nickname, content, parentId, alarm)
            _addCommentState.emit(result)
        }
    }

    fun getComments(postId: String) {
        viewModelScope.launch {
            commentRepo.getComments(postId).collect {
                 _getCommentListState.value = it
            }
        }
    }

    fun getNoticeComments() {
        viewModelScope.launch {
            commentRepo.getNoticeComments().collect {
                _getNoticeComState.value = it
            }
        }
    }

    fun deleteComment(commentId: String, parentId: String) {
        viewModelScope.launch {
            val result = commentRepo.deleteComment(commentId, parentId)
            _deleteCommentState.emit(result)
        }
    }
}
