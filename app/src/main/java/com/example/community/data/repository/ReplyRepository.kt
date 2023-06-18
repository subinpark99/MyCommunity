package com.example.community.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.community.data.entity.Reply
import com.google.firebase.database.*

class ReplyRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getLatestReply(): MutableLiveData<Reply?> {
        val replyLiveData = MutableLiveData<Reply?>()

        val replyRef = database.child("reply").orderByChild("replyIdx").limitToLast(1)
        replyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var latestReply: Reply? = null
                for (childSnapshot in snapshot.children) {
                    val reply = childSnapshot.getValue(Reply::class.java)
                    if (reply != null) {
                        latestReply = reply
                    }
                }
                replyLiveData.value = latestReply
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getLatestReply", error.toString())
            }
        })

        return replyLiveData
    }

    fun addReply(replyIdx: Int, reply: Reply, state: (Boolean) -> Unit) {
        val replyRef = database.child("reply").child(replyIdx.toString())
        replyRef.setValue(reply).addOnSuccessListener {
            state(true)
        }.addOnFailureListener { state(false) }
    }


    fun getReply(commentIdx: Int): MutableLiveData<MutableList<Reply>> { // commentIdx에 해당하는 대댓글 가져오기

        val replyLiveData = MutableLiveData<MutableList<Reply>>()

        val replyRef =
            database.child("reply").orderByChild("commentIdx").equalTo(commentIdx.toDouble())
        replyRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val replyList = mutableListOf<Reply>()
                for (replySnapshot in snapshot.children) {
                    val reply = replySnapshot.getValue(Reply::class.java)
                    if (reply != null) replyList.add(reply)
                }
                replyLiveData.value = replyList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getLocationPost", error.toString())
            }
        })

        return replyLiveData
    }


    fun deleteReply(replyIdx: Int, state: (Boolean) -> Unit) {

        val deleteRef = database.child("reply")
        deleteRef.child(replyIdx.toString()).removeValue()
            .addOnSuccessListener { state(true) }.addOnFailureListener { state(false) }
    }

    fun deleteCommentReply(commentIdx: Int) {

        val deleteReply =
            database.child("reply").orderByChild("commentIdx").equalTo(commentIdx.toDouble())
        deleteReply.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (replySnapshot in snapshot.children) { // 각 댓글에 대한 데이터
                    replySnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("deleteAllReply", error.toString())
            }
        })

    }

    fun deletePostReply(postIdx: Int) {

        val deleteReply =
            database.child("reply").orderByChild("postIdx").equalTo(postIdx.toDouble())
        deleteReply.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (replySnapshot in snapshot.children) { // 각 댓글에 대한 데이터
                    replySnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("deleteAllReply", error.toString())
            }
        })

    }

}