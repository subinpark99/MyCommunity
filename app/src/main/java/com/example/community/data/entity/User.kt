package com.example.community.data.entity

data class User(
    val email:String="",
    val password:String="",
    val nickname:String="",
    val location:String="",
    val age:String="",
    val alarm:Boolean=false,
    val fcmToken:HashMap<String, Any>? = null  // 토큰 안에 토큰 값
)