package com.example.community.data.entity

data class User(
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    val location: String = "",
    val age: Int = 0,
    val alarm: Boolean = false,
    val token: String = "",
)