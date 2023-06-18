package com.example.community.data.entity


data class Reply(  // 대댓글
    val uid:String="",
    var postIdx: Int =0,
    val nickname: String? = "",
    val date: String? ="",
    val time:String="",
    var content:String="",
    var replyIdx:Int=0,
    var commentIdx:Int=0
)