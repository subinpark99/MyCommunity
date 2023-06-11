package com.example.community.data.entity

data class User(
    val email:String="",
    val pw:String="",
    val nickname:String="",
    val location:String="",
    val age:Int=0,
    val fcmtoken:HashMap<String, Any>? = null  // 토큰 안에 토큰 값
)