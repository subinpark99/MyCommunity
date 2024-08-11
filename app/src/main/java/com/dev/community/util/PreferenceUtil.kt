package com.dev.community.util

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class PreferenceUtil @Inject constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("state", Context.MODE_PRIVATE)

    fun setAutoLogin(autologin: Boolean) {  // 자동 로그인 설정
        prefs.edit().putBoolean("login", autologin).apply()
    }

    fun getAutoLogin(): Boolean {  // 자동 로그인 상태 조회
        return prefs.getBoolean("login", false)
    }

}







