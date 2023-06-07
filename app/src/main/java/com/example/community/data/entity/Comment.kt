package com.example.community.data.entity


data class Comment( // 댓글
    val uid:String="",
    var postIdx:Int=0,
    var content:String="",
    var commentIdx:Int=0,
    val nickname: String="",
    val date: String="",
)
