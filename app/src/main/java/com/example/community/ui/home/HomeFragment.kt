package com.example.community.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.community.data.MyApplication
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentHomeBinding
import com.google.gson.Gson


class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User
    private val gson : Gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val userJson=MyApplication.prefs.getUser("user","")
        user=gson.fromJson(userJson,User::class.java)

        binding.currentLocationTv.text=user.location

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
