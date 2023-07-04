package com.example.community.ui.notice.fcm

import com.example.community.BuildConfig


class Repository {
    companion object {
        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = BuildConfig.FCM_SERVER_KEY
        const val CONTENT_TYPE = "application/json"
    }
}