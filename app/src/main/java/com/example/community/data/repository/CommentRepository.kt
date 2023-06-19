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

    fun deletePostComment(postIdx: Int) {

        val deleteComment =
            database.child("comment").orderByChild("postIdx").equalTo(postIdx.toDouble())
        deleteComment.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (commentSnapshot in snapshot.children) { // 각 댓글에 대한 데이터
                    commentSnapshot.ref.removeValue()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("deleteAllComment", error.toString())

            }
        })

    }

    fun getNoticeComment(
        postIdx: Int,
        userUid: String
    ): MutableLiveData<MutableList<Comment>> {  // 내가 쓴 게시물의 댓글

        val commentLiveData = MutableLiveData<MutableList<Comment>>()

        val commentRef =
            database.child("comment").orderByChild("postIdx").equalTo(postIdx.toDouble())

        commentRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<Comment>()
                for (comSnapshot in snapshot.children) {
                    val comment = comSnapshot.getValue(Comment::class.java)
                    if (comment != null && comment.uid != userUid) commentList.add(comment)
                }
                commentLiveData.value = commentList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getComment", error.toString())
            }
        })

        return commentLiveData

    }

    fun getMyComments(userUid: String, state: (Boolean) -> Unit): MutableLiveData<Comment?> {

        val commentLiveData = MutableLiveData<Comment?>()
        val postSet = HashSet<Int>()
        val myComment = database.child("comment").orderByChild("uid").equalTo(userUid)

        myComment.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (contentSnapshot in snapshot.children.reversed()) {

                        val comment = contentSnapshot.getValue(Comment::class.java)

                        if (comment != null) {

                            if (!postSet.contains(comment.postIdx)) {
                                postSet.add(comment.postIdx)
                                commentLiveData.value = comment

                            }
                            state(true)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                state(false)
                Log.d("getComment", error.toString())
            }
        })
        return commentLiveData
    }



}

