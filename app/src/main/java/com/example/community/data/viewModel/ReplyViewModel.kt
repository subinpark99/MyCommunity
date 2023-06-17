package com.example.community.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.example.community.data.entity.Post
import com.example.community.data.entity.Reply
import com.example.community.data.repository.ReplyRepository


class ReplyViewModel {

    private val replyRepo = ReplyRepository()

    private var _addReplyState = MutableLiveData<Boolean>()
    val addReplyState: LiveData<Boolean> = _addReplyState

    private var _deleteReplyState = MutableLiveData<Boolean>()
    val deleteReplyState: LiveData<Boolean> = _deleteReplyState


    fun getLatestReply(): MutableLiveData<MutableList<Reply>?> {
        return replyRepo.getLatestReply()
    }

    fun addReply(
        uid:String,
        postIdx: Int,
        nickname: String,
        date: String,
        content:String,
        replyIdx:Int,
        commentIdx:Int
    ) {
        if (checkAddNull(content)) {
            val reply =
                Reply(uid, postIdx, nickname, date, content, replyIdx, commentIdx)
            replyRepo.addReply(replyIdx,reply) { success ->
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



    fun deleteReply(replyIdx: Int){
        return replyRepo.deleteReply(replyIdx){ success ->
            if (success) {
                _deleteReplyState.postValue(true)
            } else {
                _deleteReplyState.postValue(false)
            }
        }
    }
}