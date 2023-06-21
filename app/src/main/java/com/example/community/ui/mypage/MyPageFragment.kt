package com.example.community.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.community.R
import com.example.community.data.local.MyApplication
import com.example.community.data.viewModel.AuthViewModel
import com.example.community.databinding.FragmentMypageBinding

class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: AuthViewModel by viewModels()

    private lateinit var userUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        nav()
        userUid = MyApplication.prefs.getUid("uid", "")

        return binding.root
    }


    private fun nav() {

        binding.mycontentsTv.setOnClickListener { // 내가 쓴 글
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_myContentsFragment)
        }

        binding.replyTv.setOnClickListener { // 내가 쓴 댓글
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_myReplyFragment)
        }

        binding.logoutTv.setOnClickListener { // 로그아웃
            userViewModel.logout()
            MyApplication.prefs.setAutoLogin("login", false)
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_loginActivity)
        }

        binding.withdrawTv.setOnClickListener { // 회원탈퇴

//            userViewModel.deleteAllMyComment(userUid)
//            userViewModel.deleteAllMyPost(userUid)
//            userViewModel.deleteAllMyReply(userUid) // 적용할까....말까....

            userViewModel.withdraw(userUid)

            MyApplication.prefs.deleteUid("uid")
            MyApplication.prefs.deleteToken("token")
            MyApplication.prefs.deleteUser("user")

            Toast.makeText(requireContext(), "회원탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()
            withdrawState()
            activity!!.finish()

        }


        binding.changePwTv.setOnClickListener {  // 비밀번호 변경
            DialogChangePw().show(parentFragmentManager, "changepw")
        }

        binding.changelocationTv.setOnClickListener {// 내 위치 변경
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_dialogChangeLocation)
        }


        binding.noticeToggleBtn.setOnCheckedChangeListener { _, onSwitch ->
            //  스위치가 켜지면
            if (onSwitch) {
                binding.noticeToggleBtn.isChecked = true
                userViewModel.setSwitchOn(userUid)
            }
            //  스위치가 꺼지면
            else {
                binding.noticeToggleBtn.isChecked = false
                userViewModel.setSwitchOff(userUid)

            }

        }
    }

    private fun withdrawState() {
        userViewModel.withDrawState.observe(this) { state ->
            when (state) {
                true -> {
                    Log.d("withdrawState", "success")
                }
                else -> Log.d("withdrawState", "failed")

            }
        }
    }

    private fun setToggle() {
        userViewModel.setToggle(userUid).observe(viewLifecycleOwner) {
            if (it != null) {
                binding.noticeToggleBtn.isChecked = it
            }
        }
    }


    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        setToggle()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}