package com.dev.community.data.model

import java.io.Serializable

data class User(
    val uid: String = "",  // 기본값 설정
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    var location: String = "",
    val age: Int = 0,
    val alarm: Boolean = false,
    val token: String = ""
) : Serializable