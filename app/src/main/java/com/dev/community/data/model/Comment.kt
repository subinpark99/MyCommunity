package com.dev.community.data.model


data class Comment(
    var commentId: String = "",  // 기본값 설정
    var postId: String = "",
    var parentId: String = "",
    val uid: String = "",
    val nickname: String = "",
    var content: String = "",
    val date: Long = System.currentTimeMillis()
)
