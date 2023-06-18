package com.example.community.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.community.data.entity.Comment
import com.google.firebase.database.*

class CommentRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getLatestComment(): MutableLiveData<Comment?> {
        val commentLiveData = MutableLiveData<Comment?>()

        val commentRef = database.child("comment").orderByChild("commentIdx").limitToLast(1)
        commentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var latestComment: Comment? = null
                for (childSnapshot in snapshot.children) {
                    val comment = childSnapshot.getValue(Comment::class.java)

                    if (comment != null) {
                        latestComment = comment
                    }
                }
                commentLiveData.value = latestComment
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getLatestPost", error.toString())
            }
        })

        return commentLiveData
    }


    fun addComment(commentIdx: Int, comment: Comment, state: (Boolean) -> Unit) {
        val commentRef = database.child("comment").child(commentIdx.toString())
        commentRef.setValue(comment).addOnSuccessListener {
            state(true)
        }.addOnFailureListener { state(false) }
    }


    fun getComment(postIdx: Int): MutableLiveData<MutableList<Comment>> { // postIdx에 해당하는 댓글 가져오기

        val commentLiveData = MutableLiveData<MutableList<Comment>>()

        val commentRef =
            database.child("comment").orderByChild("postIdx").equalTo(postIdx.toDouble())
        commentRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<Comment>()
                for (comSnapshot in snapshot.children) {
                    val comment = comSnapshot.getValue(Comment::class.java)
                    if (comment != null) commentList.add(comment)
                }
                commentLiveData.value = commentList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getComment", error.toString())
            }
        })

        return commentLiveData

    }

    fun deleteComment(commentIdx: Int, state: (Boolean) -> Unit) {
        database.child("comment").child(commentIdx.toString()).removeValue()
            .addOnSuccessListener { state(true) }.addOnFailureListener { state(false) }
    }

    fun deletePostComment(postIdx: Int, state: (Boolean) -> Unit) {

        val deleteComment =
            database.child("comment").orderByChild("postIdx").equalTo(postIdx.toDouble())
        deleteComment.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (commentSnapshot in snapshot.children) { // 각 댓글에 대한 데이터
                    commentSnapshot.ref.removeValue()
                    state(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("deleteAllComment", error.toString())
                state(false)
            }
        })

    }

}