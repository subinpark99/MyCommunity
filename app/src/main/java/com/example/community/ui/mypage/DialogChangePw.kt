package com.example.community.ui.mypage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.community.data.MyApplication
import com.example.community.data.entity.User
import com.example.community.databinding.DialogChangePwBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class DialogChangePw  : DialogFragment(), View.OnClickListener {

    lateinit var binding: DialogChangePwBinding
    private lateinit var user: User
    private val gson: Gson = Gson()
    private lateinit var userUid: String
    private val userDB = Firebase.database.getReference("user")

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

            val auth=Firebase.auth
            val curPw=binding.putCurrentPw.text.toString()
            val newPw=binding.putNewPw.text.toString()

            if (curPw!=user.pw){
                Snackbar.make(binding.root, "비밀번호가 일치하지 않습니다", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw.isEmpty()) {
                Snackbar.make(binding.root, "입력을 완료해주세요", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.currentUser?.updatePassword(newPw)
            userDB.child(userUid).child("pw").setValue(newPw)
            Toast.makeText(requireContext(),"비밀번호 변경 완료, 재로그인 해주세요",Toast.LENGTH_SHORT).show()
            auth.signOut() // logout
            requireActivity().finish()
            }
    }


    override fun onClick(p0: View?) {
        dismiss()
    }

}