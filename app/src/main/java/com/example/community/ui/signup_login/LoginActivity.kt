package com.example.community.ui.signup_login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.community.data.local.MyApplication
import com.example.community.data.viewModel.AuthViewModel
import com.example.community.databinding.ActivityLoginBinding
import com.example.community.ui.other.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: AuthViewModel by viewModels()
    private var gson: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        initLoginButton()
    }

    private fun initLoginButton() {

        binding.loginBtn.setOnClickListener {

            val email = binding.putEmailTv.text.toString()
            val password = binding.putPasswordTv.text.toString()
            loginViewModel.loginUser(email, password)

        }
        loginViewModel.loginState.observe(this) { state ->
            when (state) {
                true -> {  // 로그인 성공하면 메인으로 이동

                    val user = Firebase.auth.currentUser!!.uid
                    saveUser(user)

                }
                else -> Toast.makeText(this, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun saveUser(userUid: String) {
        loginViewModel.getUser(userUid).observe(this) {

            val token: String = it?.fcmToken!!["token"] as String
            val userJson = gson.toJson(it)

            MyApplication.prefs.setUser("user", userJson)  // current user 정보 저장
            MyApplication.prefs.setToken("token", token)
            MyApplication.prefs.setUid("uid", userUid)
            MyApplication.prefs.setAutoLogin("login", true)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    }


    private fun init() {

        binding.signupIv.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 상태바 없애기
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}