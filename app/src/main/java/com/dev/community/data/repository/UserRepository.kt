package com.dev.community.data.repository

import com.dev.community.data.model.User
import com.dev.community.util.Result

interface UserRepository {

    suspend fun registerUser(
        email: String, password: String,
        nickname: String, location: String, age: Int
    ): Result<Boolean>

    suspend fun getUser(): Result<User>
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun logout(): Result<Boolean>
    suspend fun withdraw(): Result<Boolean>

    suspend fun changeLocation(location: String): Result<Boolean>
    suspend fun changePassword(newPw: String): Result<Boolean>

    suspend fun setAlarmState(state: Boolean)
    suspend fun setSwitchOn() = setAlarmState(true)
    suspend fun setSwitchOff() = setAlarmState(false)
    suspend fun getSwitch(): Result<Boolean>

    fun updateFcmToken(currentToken: String)
}
