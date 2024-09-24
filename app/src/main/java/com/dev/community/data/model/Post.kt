package com.dev.community.data.model

import java.io.Serializable


data class Post(
    var postId: String = "",
    val uid: String = "",
    val nickname: String = "",
    val title: String = "",
    val content: String = "",
    val age: Int = 0,
    val location: String = "",
    val date: Long = System.currentTimeMillis(),
    var view: Int = 0,
    var imageList: List<String> = emptyList()
) : Serializable
