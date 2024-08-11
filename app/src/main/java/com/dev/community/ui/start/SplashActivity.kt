package com.dev.community.ui.start

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.dev.community.util.AppUtils
import com.dev.community.app.MyApplication
import com.dev.community.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }


    private fun autoLogin() {

        val state = MyApplication.prefs.getAutoLogin()

        if (state) {
            val intent =
                Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun init() {

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            autoLogin()
        }, 1000)
        AppUtils.hideStatusBar(window)
    }

}