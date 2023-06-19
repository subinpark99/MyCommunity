package com.example.community.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.local.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.data.viewModel.PostViewModel
import com.example.community.databinding.FragmentHomeBinding
import com.google.gson.Gson


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels()

    private val gson: Gson = Gson()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val userJson = MyApplication.prefs.getUser("user", "")
        val user = gson.fromJson(userJson, User::class.java)

        binding.currentLocationTv.text = user.location
        getPost(user.location)

        return binding.root
    }


    private fun getPost(location: String) {

        val contentAdapter = ContentRVAdpater(requireContext())
        binding.homeContentsRv.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        postViewModel.getLocationPost(location).observe(this) {
            if (it != null) {
                contentAdapter.locationList(it)
            }
        }

        contentAdapter.setItemClickListener(object : ContentRVAdpater.InContentInterface {
            override fun onContentClicked(post: Post) {
                onPostClicked(post.postIdx)
                val arguments = HomeFragmentDirections.actionHomeFragmentToInContentFragment(post)
                findNavController().navigate(arguments)
            }
        })
    }

    fun onPostClicked(postIdx: Int) { // 게시물 조회수 증가
        postViewModel.updatePostCnt(postIdx)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
