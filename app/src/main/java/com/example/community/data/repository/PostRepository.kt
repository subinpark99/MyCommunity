package com.example.community.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.ui.notice.fcm.RetrofitInstance
import com.example.community.ui.notice.fcm.model.NotificationData
import com.example.community.ui.notice.fcm.model.PushNotification
import com.google.firebase.database.*
import kotlinx.coroutines.runBlocking


class PostRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getLatestPost(): MutableLiveData<Post?> { // 가장 최근 게시물 가져오기
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


    fun sendPushAlarm(postIdx: Int) {

        database.child("post").orderByChild("postIdx").equalTo(postIdx.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val post = childSnapshot.getValue(Post::class.java)
                        val userUid = post?.uid

                        if (userUid != null) {
                            val userRef = database.child("user").child(userUid)
                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val user = userSnapshot.getValue(User::class.java)
                                    val alarmStatus = user?.alarm

                                    if (alarmStatus == true) {
                                        runBlocking {
                                            sendFcmNotification("새로운 댓글을 확인하세요", user.token)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("sendPushalarm", "failed")
                                }

                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("getPostIdx","failed")
                }

            })

    }


    suspend fun sendFcmNotification(message: String, token: String) {

        PushNotification(
            data = NotificationData("새로운 댓글이 달렸어요!", message),
            to = token
        ).also {
            RetrofitInstance.api.postNotification(it)
        }
    }


    fun getLocationPost(userLocation: String): MutableLiveData<MutableList<Post>> { // 내 지역에 있는 게시물만 가져오기

        val postLiveData = MutableLiveData<MutableList<Post>>()

        val postRef = database.child("post").orderByChild("location").equalTo(userLocation)
        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = mutableListOf<Post>()
                for (postSnapshot in snapshot.children.reversed()) {
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

    fun getMyPost(userUid: String): MutableLiveData<MutableList<Post>?> {

        val postLiveData = MutableLiveData<MutableList<Post>?>()

        val myPost = database.child("post").orderByChild("uid").equalTo(userUid)

        myPost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val postList = mutableListOf<Post>()
                for (postSnapshot in snapshot.children.reversed()) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) postList.add(post)
                }
                postLiveData.value = postList

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getPost", error.toString())
            }
        })
        return postLiveData
    }


    fun getNoticePost(postIdx: Int, state: (Boolean) -> Unit): MutableLiveData<Post?> {

        val postLiveData = MutableLiveData<Post?>()

        val getNoticePost =
            database.child("post").orderByChild("postIdx").equalTo(postIdx.toDouble())

        getNoticePost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (contentSnapshot in snapshot.children) {

                        val post = contentSnapshot.getValue(Post::class.java)

                        if (post != null) postLiveData.value = post
                        state(true)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                state(false)
                Log.d("getNoticePost", error.toString())
            }
        })
        return postLiveData

    }


}


