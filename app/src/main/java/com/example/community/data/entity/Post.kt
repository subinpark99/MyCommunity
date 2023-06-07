package com.example.community.data.entity

import android.os.Parcel
import android.os.Parcelable

data class Post(  // 게시글
    var postIdx: Int=0,
    val uid: String = "",
    val nickname: String = "",
    val date: String ="",
    var view: Int = 0,
    val title: String = "",
    val content: String = "",
    val imgs: List<String>?=null
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.createStringArrayList()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(postIdx)
        parcel.writeString(uid)
        parcel.writeString(nickname)
        parcel.writeString(date)
        parcel.writeInt(view)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeStringList(imgs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}