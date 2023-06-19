package com.example.community.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.community.data.entity.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging


class AuthRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth: FirebaseAuth = Firebase.auth
    private val fcm: FirebaseMessaging = Firebase.messaging

    fun registerUser(
        email: String, password: String, user: User,
        state: (Boolean) -> Unit
    ) {  // 회원가입

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    state(true)
                    val userRef = database.child("user").child(auth.uid.toString())
                    userRef.setValue(user).addOnFailureListener {
                        Log.d("registerUser", "failed")
                    }
                }
            }
            .addOnFailureListener {
                state(false)
            }
    }

    fun loginUser(email: String, password: String, state: (Boolean) -> Unit) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userUid = auth.currentUser!!.uid
                getFcmToken(userUid)
                state(true)
            }
            .addOnFailureListener {
                state(false)
            }
    }


    fun getUser(userUid: String): MutableLiveData<User?> { // user 정보 가져오기

        val userLiveData = MutableLiveData<User?>()

        val userRef = database.child("user").child(userUid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user!==null) userLiveData.value = user
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getUser", "failed")
            }
        })

        return userLiveData

    }


    fun getFcmToken(userUid: String) {

        fcm.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("FetchingFCM registration token failed", task.exception.toString())
                return@OnCompleteListener
            }
            val token = task.result
            val tokenRef = database.child("user").child(userUid).child("fcmToken")
                .child("token")

            tokenRef.setValue(token).addOnFailureListener {
                Log.d("getFcmToken", "failure")
            }
        })
    }

    fun logout() {
        auth.signOut()
    }

    fun withdraw(userUid: String, state: (Boolean) -> Unit) {  // 계정 삭제

        auth.currentUser!!.delete() .addOnSuccessListener {
            database.child("user").child(userUid).removeValue()
            state(true) }
            .addOnFailureListener { state(false) }
    }



    fun deleteAllMyComment(userUid: String) {
        val deleteRef = database.child("comment").orderByChild("uid").equalTo(userUid)

        deleteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (Snapshot in snapshot.children) {
                    Snapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("deleteAllMyComment", error.toString())
            }
        })
    }

    fun deleteAllMyPost(userUid: String) {
        val deleteRef = database.child("post").orderByChild("uid").equalTo(userUid)

        deleteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (Snapshot in snapshot.children) {
                    Snapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("deleteAllMyPost", error.toString())
            }
        })
    }

    fun deleteAllMyReply(userUid: String) {
        val deleteRef = database.child("reply").orderByChild("uid").equalTo(userUid)

        deleteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (Snapshot in snapshot.children) {
                    Snapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("deleteAllMyPost", error.toString())
            }
        })
    }

}