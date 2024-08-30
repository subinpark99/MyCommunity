package com.dev.community.ui.start

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dev.community.util.AppUtils
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.ActivitySignupBinding
import com.dev.community.ui.other.MapActivity
import com.dev.community.ui.other.OcrActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.R)
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        AppUtils.hideStatusBar(window)

        getTextIntent()
        setClickListener()
    }


    private fun getTextIntent() {
        intent?.let {
            val location = it.getStringExtra("location")
            val email = it.getStringExtra("email")
            val password = it.getStringExtra("password")
            val nickname = it.getStringExtra("nickname")
            val age = it.getStringExtra("age")

            binding.setLocationTv.text = location
            binding.putEmailTv.setText(email)
            binding.putPasswordTv.setText(password)
            binding.putNicknameEv.setText(nickname)
            binding.putAgeEv.setText(age)
        }
    }

    private fun setClickListener() {
        binding.apply {
            locationLayout.setOnClickListener { showLocationDialog() }
            cancelBtn.setOnClickListener { finish() }
            submitBtn.setOnClickListener { submitRegistration() }
        }
    }

    private fun showLocationDialog() {
        val options = arrayOf("현재 위치로 설정", "주민등록증으로 설정")
        AlertDialog.Builder(this)
            .setTitle("선택하세요")
            .setItems(options) { _, which ->
                val targetActivity = when (which) {
                    0 -> MapActivity::class.java
                    else -> OcrActivity::class.java
                }
                val intent = Intent(this, targetActivity).apply {
                    putExtra("page", "signup")
                    putExtra("email", binding.putEmailTv.text.toString())
                    putExtra("password", binding.putPasswordTv.text.toString())
                    putExtra("nickname", binding.putNicknameEv.text.toString())
                    putExtra("age", binding.putAgeEv.text.toString())
                }
                startActivity(intent)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun submitRegistration() {
        val email = binding.putEmailTv.text.toString()
        val password = binding.putPasswordTv.text.toString()
        val nickname = binding.putNicknameEv.text.toString()
        val location = binding.setLocationTv.text.toString()
        val age = binding.putAgeEv.text.toString().toIntOrNull() ?: 0

        observeRegisterState(email, password, nickname, location, age)
    }

    private fun observeRegisterState(
        email: String,
        password: String,
        nickname: String,
        location: String,
        age: Int,
    ) {
        userViewModel.registerUser(email, password, nickname, location, age)
        lifecycleScope.launch {
            userViewModel.registerState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { result ->
                    when (result) {
                        is Result.Success -> if (result.data) handleSignUpSuccess()
                        is Result.Error -> AppUtils.showToast(this@SignUpActivity, result.message)
                        is Result.Loading -> Log.e("Loading", "로딩중")
                    }
                }
        }
    }

    private fun handleSignUpSuccess() {
        AppUtils.showToast(this, "회원가입에 성공했습니다!")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()

    }

}