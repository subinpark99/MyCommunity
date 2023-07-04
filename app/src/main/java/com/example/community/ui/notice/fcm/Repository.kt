package com.example.community.ui.notice.fcm

import com.example.community.R

class Repository {
    companion object {
        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = "${R.string.server_key}"
        const val CONTENT_TYPE = "application/json"
    }
}