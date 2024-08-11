package com.dev.community.ui.other

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dev.community.data.model.User
import com.dev.community.util.AppUtils
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.ActivityChangepwBinding
import com.dev.community.ui.start.LoginActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePwActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangepwBinding

    private lateinit var user: User

    private val userViewModel: UserViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityChangepwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppUtils.hideStatusBar(window)
        binding.close.setOnClickListener { finish() }

        updatePassword()

        userViewModel.getUser()
        observeState()
    }


    private fun observeState() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userViewModel.userState.collect { result ->
                        when (result) {
                            is Result.Success -> user = result.data
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()

                        }
                    }
                }

                launch {
                    userViewModel.changePasswordState.collect {
                        when (it) {
                            is Result.Success -> {
                                if (it.data) {
                                    AppUtils.showToast(
                                        this@ChangePwActivity,
                                        "비밀번호 변경 완료, 재로그인 해주세요"
                                    )
                                    userViewModel.logout()

                                    val intent =
                                        Intent(this@ChangePwActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    this@ChangePwActivity.finish()
                                }
                            }

                            is Result.Error -> handleError(it.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }
            }

        }
    }


    private fun updatePassword() {

        binding.doneIv.setOnClickListener {

            val curPw = binding.putCurrentPw.text.toString()
            val newPw = binding.putNewPw.text.toString()
            val renewPw = binding.reputNewPw.text.toString()

            if (curPw != user.password) {
                Snackbar.make(binding.root, "기존 비밀번호가 일치하지 않습니다", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw != renewPw) {
                Snackbar.make(binding.root, "새 비밀번호가 일치하지 않습니다", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (newPw.isEmpty()) {
                Snackbar.make(binding.root, "입력을 완료해주세요", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.changePassword(newPw)
        }
    }


    private fun handleError(exception: String) {
        Log.e("ERROR", "ChangePwActivity- $exception")
    }

    private fun handleLoading() {
        Log.d("loading", "loading...")
    }

}