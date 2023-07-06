package com.example.community.data.entity


data class Post(  // 게시글
    var postIdx: Int = 0,
    val age: Int = 0,
    val location: String = "",
    val uid: String = "",
    val nickname: String = "",
    val date: String = "",
    var view: Int = 0,
    val title: String = "",
    val content: String = "",
    val imgs: List<String>? = null
) : java.io.Serializable
