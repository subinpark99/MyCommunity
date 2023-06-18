package com.example.community.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.community.data.entity.Post
import com.google.firebase.database.*


class PostRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getLatestPost(): MutableLiveData<Post?> {
        val postLiveData = MutableLiveData<Post?>()

        val postRef = database.child("post").orderByChild("postIdx").limitToLast(1)
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var latestPost: Post? = null
                for (childSnapshot in snapshot.children) {
                    val post = childSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        latestPost = post
                    }
                }
                postLiveData.value = latestPost
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getLatestPost", error.toString())
            }
        })

        return postLiveData
    }

    fun addPost(postIdx: Int, post: Post, state: (Boolean) -> Unit) {
        val postRef = database.child("post").child(postIdx.toString())
        postRef.setValue(post).addOnSuccessListener {
            state(true)
        }.addOnFailureListener { state(false) }
    }


    fun getLocationPost(userLocation: String): MutableLiveData<MutableList<Post>> { // 내 지역에 있는 게시물만 가져오기

        val postLiveData = MutableLiveData<MutableList<Post>>()

        val postRef = database.child("post").orderByChild("location").equalTo(userLocation)
        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) postList.add(post)
                }
                postLiveData.value = postList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getLocationPost", error.toString())
            }
        })

        return postLiveData

    }


    fun updatePostCnt(postIdx: Int) { // 게시물 클릭 시 조회수 증가

        val updateCntRef = database.child("post").child(postIdx.toString())

        updateCntRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {

                    post.view = post.view + 1    // 조회수 증가
                    updateCntRef.setValue(post)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("updatePostCnt", error.toString())
            }
        })

    }

    fun deletePost(postIdx: Int, state: (Boolean) -> Unit) {

        val deleteRef = database.child("post")
        deleteRef.child(postIdx.toString()).removeValue()
            .addOnSuccessListener { state(true) }.addOnFailureListener { state(false) }

    }

}


