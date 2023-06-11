package com.example.community.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.community.R
import com.example.community.data.MyApplication
import com.example.community.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val userDB = Firebase.database.getReference("user")
    private val postDB = Firebase.database.getReference("post")
    private val commentDB = Firebase.database.getReference("comment")
    private lateinit var userUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        auth = Firebase.auth

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
            auth.signOut()
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_loginActivity)
        }

        binding.withdrawTv.setOnClickListener { // 회원탈퇴

            deleteComment()
            deletePost()
            auth.currentUser?.delete() // 계정 삭제
            userDB.child(userUid).removeValue() // 파이어베이스에 저장된 계정 삭제
            Toast.makeText(requireContext(), "회원탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()

            activity?.finish()
        }

        binding.changePwTv.setOnClickListener {  // 비밀번호 변경
            DialogChangePw().show(parentFragmentManager, "changepw")
        }

        binding.changelocationTv.setOnClickListener {// 내 위치 변경
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_dialogChangeLocation)
        }
    }

    private fun deletePost() {
        postDB.orderByChild("uid").equalTo(userUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        postSnapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("deletePost", error.toString())
                }
            })
    }

    private fun deleteComment() {
        commentDB.orderByChild("uid").equalTo(userUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (commentSnapshot in snapshot.children) {
                        commentSnapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("deleteComment", error.toString())
                }

            })
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
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