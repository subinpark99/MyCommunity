package com.example.community.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.community.data.entity.Reply
import com.example.community.data.repository.ReplyRepository


class ReplyViewModel : ViewModel() {

    private val replyRepo = ReplyRepository()

    private var _addReplyState = MutableLiveData<Boolean>()
    val addReplyState: LiveData<Boolean> = _addReplyState

    private var _deleteReplyState = MutableLiveData<Boolean>()
    val deleteReplyState: LiveData<Boolean> = _deleteReplyState

    fun getLatestReply(): MutableLiveData<Reply?> {
        return replyRepo.getLatestReply()
    }

    fun addReply(
        uid: String,
        postIdx: Int,
        nickname: String,
        date: String,
        time: String,
        content: String,
        replyIdx: Int,
        commentIdx: Int
    ) {
        if (checkAddNull(content)) {
            val reply =
                Reply(uid, postIdx, nickname, date, time, content, replyIdx, commentIdx)
            replyRepo.addReply(replyIdx, reply) { success ->
                if (success) {
                    _addReplyState.postValue(true)
                } else {
                    _addReplyState.postValue(false)
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


    fun getReply(commentIdx: Int): MutableLiveData<MutableList<Reply>> {
        return replyRepo.getReply(commentIdx)
    }


    fun deleteReply(replyIdx: Int) {
        return replyRepo.deleteReply(replyIdx) { success ->
            if (success) {
                _deleteReplyState.postValue(true)
            } else {
                _deleteReplyState.postValue(false)
            }
        }
    }

    fun deleteAllCommentReply(commentIdx: Int) {
        return replyRepo.deleteCommentReply(commentIdx)
    }

    fun deleteAllPostReply(postIdx: Int) {
        replyRepo.deletePostReply(postIdx)
    }

    fun getNoticeReply(postIdx: Int, userUid: String): MutableLiveData<MutableList<Reply>> {
        return replyRepo.getNoticeReply(postIdx, userUid)
    }
}