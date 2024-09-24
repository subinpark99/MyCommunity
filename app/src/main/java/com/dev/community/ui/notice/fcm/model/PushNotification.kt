package com.dev.community.ui.notice.fcm.model

data class PushNotification(
    val message: Message
)

data class Message(
    val token: String,  // 수신 디바이스의 FCM 토큰
    val notification: NotificationData
)

