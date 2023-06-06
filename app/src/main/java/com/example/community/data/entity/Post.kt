package com.example.community.data.entity


data class Post(
    var postIdx: Int = 0,
    val uid: String? = null,
    val nickname: String = "",
    val date: String="",
    val view: Int = 0,
    val title: String = "",
    val content: String = "",
    val imgs: List<String>? = null
)