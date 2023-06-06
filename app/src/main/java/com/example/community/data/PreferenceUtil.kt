package com.example.community.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user", Context.MODE_PRIVATE)

    fun getUid(key: String, defValue: String): String {  // uid 조회
        return prefs.getString(key, defValue).toString()
    }

    fun setUid(key: String, str: String) {  // uid 저장
        prefs.edit().putString(key, str).apply()
    }

    fun getUser(key: String, defValue: String): String {  // user 정보 조회
        return prefs.getString(key, defValue).toString()
    }

    fun setUser(key: String, str: String) {  // user 정보 저장
        prefs.edit().putString(key, str).apply()
    }
}