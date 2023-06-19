package com.example.community.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.community.data.entity.Comment
import com.example.community.data.repository.CommentRepository



class CommentViewModel: ViewModel() {

    private val commmentRepo = CommentRepository()

    private var _addCommentState = MutableLiveData<Boolean>()
    val addCommentState: LiveData<Boolean> = _addCommentState

    private var _deleteCommentState = MutableLiveData<Boolean>()
    val deleteCommentState: LiveData<Boolean> = _deleteCommentState

    private var _getMyCommentState = MutableLiveData<Boolean>()
    val getMyCommentState: LiveData<Boolean> = _getMyCommentState


    fun getLatestComment(): MutableLiveData<Comment?> {
        return commmentRepo.getLatestComment()
    }

    fun addComment(
        uid:String,postIdx:Int,content:String,commentIdx:Int,nickname:String,date:String,time:String
    ) {
        if (checkAddNull(content)) {
            val comment= Comment(uid, postIdx, content, commentIdx, nickname, date, time)
            commmentRepo.addComment(commentIdx,comment){ success ->
                if (success) {
                    _addCommentState.postValue(true)
                } else {
                    _addCommentState.postValue(false)
                }
            }
        } else {
            return
        }

    }

    private fun checkAddNull(
        content: String
    ): Boolean {
        return content.isNotEmpty()
    }


    fun getComment(postIdx: Int): MutableLiveData<MutableList<Comment>> {
        return commmentRepo.getComment(postIdx)
    }

    fun deleteComment(commentIdx: Int){
        return commmentRepo.deleteComment(commentIdx){ success ->
            if (success) {
                _deleteCommentState.postValue(true)
            } else {
                _deleteCommentState.postValue(false)
            }
        }
    }

    fun deleteAllPostComment(postIdx: Int){
        return commmentRepo.deletePostComment(postIdx)

    }

    fun getNoticeComment(postIdx: Int,userUid:String): MutableLiveData<MutableList<Comment>> {
        return commmentRepo.getNoticeComment(postIdx,userUid)
    }

    fun getMyComments(userUid:String):MutableLiveData<Comment?>{
        return commmentRepo.getMyComments(userUid){ success ->
            if (success) {
                _getMyCommentState.postValue(true)
            } else {
                _getMyCommentState.postValue(false)
            }
        }
    }

}