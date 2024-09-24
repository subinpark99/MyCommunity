package com.dev.community.app


import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import com.dev.community.BuildConfig
import com.dev.community.util.PreferenceUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceUtil(applicationContext)

        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY )
        FirebaseApp.initializeApp(this)
    }
}