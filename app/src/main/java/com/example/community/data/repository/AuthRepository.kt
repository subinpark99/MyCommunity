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
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val auth: FirebaseAuth = Firebase.auth,
    private val fcm: FirebaseMessaging = Firebase.messaging
) {

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
                if (user !== null) userLiveData.value = user
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

        auth.currentUser!!.delete().addOnSuccessListener {
            database.child("user").child(userUid).removeValue()
            state(true)
        }
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

    fun changeLocation(userUid: String, setLocation: String) {
        val user = database.child("user").child(userUid).child("location")
        user.setValue(setLocation)
    }

    fun changePassword(userUid: String, newPw: String, state: (Boolean) -> Unit) {

        val user = database.child("user").child(userUid).child("password")
        changeDBPw(newPw)

        user.setValue(newPw)
            .addOnSuccessListener {
                state(true)
            }
            .addOnFailureListener {
                state(false)
            }

    }

    private fun changeDBPw(newPw: String) {
        auth.currentUser?.updatePassword(newPw) // 파이어베이스 계정 비번 업데이트
    }

    fun setSwitchOn(userUid: String) {
        database.child("user").child(userUid).child("alarm").setValue(true)
    }

    fun setSwitchOff(userUid: String) {
        database.child("user").child(userUid).child("alarm").setValue(false)
    }

    fun setToggle(userUid: String): MutableLiveData<Boolean?> {
        val userLiveData = MutableLiveData<Boolean?>()
        val userRef = database.child("user").child(userUid).child("alarm")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alarmEnabled = snapshot.getValue(Boolean::class.java)
                if (alarmEnabled != null) {
                    userLiveData.value = alarmEnabled
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("setToggle", error.toString())
            }

        })
        return userLiveData
    }


}