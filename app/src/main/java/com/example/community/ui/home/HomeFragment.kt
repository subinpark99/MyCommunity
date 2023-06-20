package com.example.community.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
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
    private lateinit var contentAdapter: ContentRVAdpater

    private val gson: Gson = Gson()

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val userJson = MyApplication.prefs.getUser("user", "")
        val user = gson.fromJson(userJson, User::class.java)

        contentAdapter = ContentRVAdpater(requireContext())

        binding.searchBar.setOnQueryTextListener(searchViewTextListener)

        binding.currentLocationTv.text = user.location
        getPost(user.location)

        setSearchView()

        return binding.root
    }

    private fun setSearchView() {
        val searchView = binding.searchBar
        searchView.isSubmitButtonEnabled = true

        searchView.setOnSearchClickListener {
            binding.location.visibility = View.GONE
            binding.currentLocationTv.visibility = View.GONE
        }

        searchView.setOnCloseListener {
            binding.location.visibility = View.VISIBLE
            binding.currentLocationTv.visibility = View.VISIBLE
            false
        }
    }

    private var searchViewTextListener: SearchView.OnQueryTextListener =

        object : SearchView.OnQueryTextListener {

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextSubmit(s: String): Boolean {
                contentAdapter.filter.filter(s)
                return false
            }

            // 텍스트 입력, 수정 시에 호출
            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun getPost(location: String) {

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

}
