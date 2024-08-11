package com.dev.community.ui.start

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dev.community.util.AppUtils
import com.dev.community.app.MyApplication
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.R)
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppUtils.hideStatusBar(window)
        setClickListener()
        observeLogin()
    }


    private fun setClickListener() {

        binding.loginBtn.setOnClickListener {  // 로그인 버튼 클릭

            val email = binding.putEmailTv.text.toString()
            val password = binding.putPasswordTv.text.toString()

            userViewModel.loginUser(email, password)  // 로그인 실행
        }

        binding.signupIv.setOnClickListener {  // 회원가입 버튼 클릭
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeLogin() {

        lifecycleScope.launch {
            userViewModel.loginState.collect { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data) {
                            AppUtils.dismissLoadingDialog()
                            loginSuccess()
                        }
                    }
                    is Result.Error -> AppUtils.showToast(this@LoginActivity, "아이디와 비밀번호를 확인해주세요.")
                    is Result.Loading -> AppUtils.showLoadingDialog(this@LoginActivity)
                }
            }
        }
    }

    private fun loginSuccess() {
        MyApplication.prefs.setAutoLogin(true)  // pref에 자동로그인 설정

        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

    }


}