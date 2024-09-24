package com.dev.community.ui.notice.fcm


import com.dev.community.BuildConfig
import com.dev.community.ui.notice.fcm.model.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Content-type: application/json")
    @POST("v1/projects/${BuildConfig.PROJECT_ID}/messages:send")
    suspend fun postNotification(
        @Header("Authorization") token: String,
        @Body notification: PushNotification
    ): Response<ResponseBody>
}