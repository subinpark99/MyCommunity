package com.example.community.ui.mypage

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.community.R
import com.example.community.data.local.MyApplication
import com.example.community.data.entity.User
import com.example.community.data.viewModel.AuthViewModel
import com.example.community.databinding.DialogChangePwBinding
import com.example.community.ui.signup_login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class DialogChangePw : DialogFragment(), View.OnClickListener {

    lateinit var binding: DialogChangePwBinding
    private lateinit var user: User
    private val gson: Gson = Gson()
    private lateinit var userUid: String
    private val userViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogChangePwBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  //배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  //dialog 모서리 둥글게

        val userJson = MyApplication.prefs.getUser("user", "")
        user = gson.fromJson(userJson, User::class.java)
        userUid = MyApplication.prefs.getUid("uid", "")

        updatePassword()

        return binding.root
    }

    private fun updatePassword() {

        binding.doneIv.setOnClickListener {

            val curPw = binding.putCurrentPw.text.toString()
            val newPw = binding.putNewPw.text.toString()

            if (curPw != user.password) {
                Snackbar.make(binding.root, "비밀번호가 일치하지 않습니다", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw.isEmpty()) {
                Snackbar.make(binding.root, "입력을 완료해주세요", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.changePassword(userUid, newPw)

            changeLocationPref(user, newPw)
            updatePasswordState()


        }
    }

    private fun updatePasswordState() {
        userViewModel.changePwState.observe(this) { state ->
            when (state) {
                true -> {
                    Toast.makeText(requireContext(), "비밀번호 변경 완료, 재로그인 해주세요", Toast.LENGTH_SHORT)
                        .show()

                    userViewModel.logout()

                    MyApplication.prefs.setAutoLogin("login", false)

                    val navController = findNavController()
                    navController.popBackStack(R.id.homeFragment, false)

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()

                }
                else -> Log.d("updatePw", "failed")

            }
        }
    }

    private fun changeLocationPref(user: User, newPassword: String) {
        val changedUser = User(
            user.email, newPassword, user.nickname,
            user.location, user.age, user.alarm, user.fcmToken
        )
        val userJson = gson.toJson(changedUser)
        MyApplication.prefs.setUser("user", userJson)
    }


    override fun onClick(p0: View?) {
        dismiss()
    }

}