package com.dev.community.data.repository

import android.util.Log
import com.dev.community.data.model.User
import com.dev.community.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val database: DatabaseReference,
    private val auth: FirebaseAuth,
    private val fcm: FirebaseMessaging,
) : UserRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("No user is logged in")


    override suspend fun registerUser(
        email: String, password: String,
        nickname: String, location: String, age: Int
    ): Result<Boolean> {
        return try {
            // 이메일과 비밀번호로 유저 생성
            auth.createUserWithEmailAndPassword(email, password).await()
            // FCM 토큰을 가져옴
            val token = fcm.token.await()

            val user = auth.currentUser

            if (user != null) {
                val userId = user.uid
                // 유저 정보를 저장하고 결과를 반환
                val saveResult =
                    saveUserData(userId, email, password, nickname, location, age, token)
                return saveResult
            }
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }


    private suspend fun saveUserData(
        userUid: String, email: String, password: String,
        nickname: String, location: String, age: Int,
        token: String
    ): Result<Boolean> {
        return try {
            val user = User(userUid, email, password, nickname, location, age, token = token)
            // 유저 정보를 데이터베이스에 저장
            database.child("user").child(userUid).setValue(user).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }


    override suspend fun getUser(): Result<User> {
        return try {
            val userRef = database.child("user").child(userId).get().await()
            val user = userRef.getValue(User::class.java)

            if (user != null) Result.Success(user)
            else Result.Error("User data not found")

        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }


    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }


    override suspend fun logout(): Result<Boolean> {
        return try {
            auth.signOut()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun withdraw(): Result<Boolean> {
        return try {
            auth.currentUser?.delete()?.await() // 파이어베이스 인증 계정 삭제
            database.child("user").child(userId).removeValue().await() // uid의 모든 데이터 삭제
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // 사용자 UID를 기준으로 위치 정보 변경
    override suspend fun changeLocation(location: String): Result<Boolean> {
        return try {
            database.child("user").child(userId).child("location").setValue(location).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun changePassword(newPw: String): Result<Boolean> {
        return try {
            auth.currentUser?.updatePassword(newPw)?.await()  // 계정 인증 비밀번호 변경
            database.child("user").child(userId).child("password").setValue(newPw).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    // 사용자 UID를 기준으로 알람 상태를 변경
    override suspend fun setAlarmState(state: Boolean) {
        database.child("user").child(userId).child("alarm").setValue(state).await()
    }

    override suspend fun getSwitch(): Result<Boolean> {
        return try {
            val userRef = database.child("user").child(userId).child("alarm")
            val snapshot = userRef.get().await()
            val alarmState = snapshot.getValue(Boolean::class.java)

            if (alarmState != null) Result.Success(alarmState)
            else Result.Error("alarm state not found")

        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override fun updateFcmToken(currentToken: String) {
        fcm.token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // FCM 토큰 가져오기 성공
                    val token = task.result
                    if (token != currentToken) database.child("user").child(userId).child("token").setValue(token)

                } else {
                    Log.e("FCM Token", "FCM 토큰을 가져오는 데 실패했습니다.", task.exception)
                }
            }
    }

}