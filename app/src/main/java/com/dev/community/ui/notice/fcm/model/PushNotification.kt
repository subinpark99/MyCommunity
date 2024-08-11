package com.dev.community.ui.notice.fcm.model

data class PushNotification(
    var data: NotificationData,
    var to: String
)