package com.dev.community.ui.other

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("timestamp")
fun setTimestamp(textView: TextView, timestamp: Long) {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    textView.text = format.format(date)
}