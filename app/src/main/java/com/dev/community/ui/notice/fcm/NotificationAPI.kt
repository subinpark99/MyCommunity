package com.dev.community.ui.notice.fcm

import com.dev.community.ui.notice.fcm.Repository.Companion.CONTENT_TYPE
import com.dev.community.ui.notice.fcm.Repository.Companion.SERVER_KEY
import com.dev.community.ui.notice.fcm.model.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}