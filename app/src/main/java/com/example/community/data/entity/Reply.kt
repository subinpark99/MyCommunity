package com.example.community.data.entity

import java.time.LocalDateTime

data class Reply(
    val uid:String="",
    var postIdx:Int=0,
    var dateTime: LocalDateTime,
    var comment:String="",
    var commentIdx:Int=0
)