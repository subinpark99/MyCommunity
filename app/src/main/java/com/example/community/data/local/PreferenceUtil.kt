package com.example.community.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.community.data.entity.User
import com.google.gson.Gson

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user", Context.MODE_PRIVATE)

    fun getUid(key: String, defValue: String): String {  // uid 조회
        return prefs.getString(key, defValue).toString()
    }

    fun setUid(key: String, str: String) {  // uid 저장
        prefs.edit().putString(key, str).apply()
    }

    fun getUser(): User? {  // user 정보 조회
        val userInfo = prefs.getString("userInfo", "").toString()
        return Gson().fromJson(userInfo, User::class.java)
    }

    fun setUser(user: User) {  // user 정보 저장
        val userInfo = Gson().toJson(user)
        prefs.edit().putString("userInfo", userInfo).apply()
    }

    fun deleteUid(str: String) {
        prefs.edit().remove(str).apply()
    }

    fun deleteUser(str: String) {
        prefs.edit().remove(str).apply()
    }

    fun setAutoLogin(str: String, autologin: Boolean) {
        prefs.edit().putBoolean(str, autologin).apply()
    }

    fun getAutoLogin(str: String): Boolean {
        return prefs.getBoolean(str, false)
    }


}