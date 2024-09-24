package com.dev.community.ui.viewModel


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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepo: CommentRepository
) : ViewModel() {

    private val _doneState = MutableSharedFlow<Result<Boolean>>()
    val doneState = _doneState.asSharedFlow()

    private val _getCommentListState =
        MutableStateFlow<Result<List<Comment>>>(Result.Loading)
    val getCommentListState = _getCommentListState.asStateFlow()

    private val _getNoticeComState = MutableStateFlow<Result<List<Comment>>>(Result.Loading)
    val getNoticeComState = _getNoticeComState.asStateFlow()


    fun addComment(
        postId: String, nickname: String, content: String, parentId: String
    ) {
        viewModelScope.launch {
            val result = commentRepo.addComment(postId, nickname, content, parentId)
            _doneState.emit(result)
        }
    }

    fun sendPushAlarm(postId: String, content: String){
        viewModelScope.launch {
            commentRepo.sendPushAlarm(postId, content)
        }
    }

    fun getComments(postId: String) {
        viewModelScope.launch {
            commentRepo.getComments(postId).collectLatest {
                 _getCommentListState.value = it
            }
        }
    }

    fun getNoticeComments() {
        viewModelScope.launch {
            val result= commentRepo.getNoticeComments()
            _getNoticeComState.value =result
        }
    }

    fun deleteComment(commentId: String, parentId: String) {
        viewModelScope.launch {
            val result = commentRepo.deleteComment(commentId, parentId)
            _doneState.emit(result)
        }
    }
}
