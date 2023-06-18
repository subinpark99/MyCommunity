package com.example.community.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.community.R
import com.example.community.data.local.MyApplication
import com.example.community.databinding.FragmentMypageBinding

class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!


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
           // auth.signOut()
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_loginActivity)
        }

        binding.withdrawTv.setOnClickListener { // 회원탈퇴

//            auth.currentUser?.delete() // 계정 삭제
//            userDB.child(userUid).removeValue() // 파이어베이스에 저장된 계정 삭제
            Toast.makeText(requireContext(), "회원탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()

            activity?.finish()
        }

        binding.changePwTv.setOnClickListener {  // 비밀번호 변경
            DialogChangePw().show(parentFragmentManager, "changepw")
        }

        binding.changelocationTv.setOnClickListener {// 내 위치 변경
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_dialogChangeLocation)
        }


//        binding.noticeToggleBtn.setOnCheckedChangeListener { CompoundButton, onSwitch ->
//            //  스위치가 켜지면
//            if (onSwitch) {
//                binding.noticeToggleBtn.isChecked = true
//
//                userDB.child(userUid).child("alarm").setValue(true)
//            }
//            //  스위치가 꺼지면
//            else {
//                binding.noticeToggleBtn.isChecked = false
//                userDB.child(userUid).child("alarm").setValue(false)
//
//            }
//
//        }
    }
    private fun setToggle(){
//        userDB.child(userUid).child("alarm").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val alarmEnabled = dataSnapshot.getValue(Boolean::class.java)
//                if (alarmEnabled != null) {
//                    binding.noticeToggleBtn.isChecked = alarmEnabled
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//               Log.d("setToggle",databaseError.toString())
//            }
//        })
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