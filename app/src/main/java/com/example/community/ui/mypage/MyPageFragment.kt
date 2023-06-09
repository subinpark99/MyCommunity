package com.example.community.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.community.R
import com.example.community.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyPageFragment: Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        auth = Firebase.auth

       nav()

        return binding.root
    }


    private fun nav(){
        binding.mycontentsTv.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_myContentsFragment)
        }

        binding.replyTv.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_myReplyFragment)
        }

        binding.logoutTv.setOnClickListener {
            auth.signOut()
            view?.findNavController()?.navigate(R.id.action_myPageFragment_to_loginActivity)
            activity?.finish()
        }
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