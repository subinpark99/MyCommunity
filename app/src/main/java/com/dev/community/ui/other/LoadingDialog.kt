package com.dev.community.ui.other

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView


class LoadingDialog(context: Context) : Dialog(context) {

    init {
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        createLoadingLayout(context)
    }

    private fun createLoadingLayout(context: Context) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }

        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
        }

        val textView = TextView(context).apply {
            text = "Loading..."
            setTextColor(Color.BLACK)
            textSize = 16f
            gravity = Gravity.CENTER
        }

        layout.addView(progressBar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        layout.addView(textView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        setContentView(layout)
    }
}