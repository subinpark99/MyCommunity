package com.dev.community.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.community.util.AppUtils
import com.dev.community.data.model.User
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.FragmentHomeBinding
import com.dev.community.ui.home.adapter.ContentRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val postViewModel: PostViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var user = User()

    private lateinit var contentAdapter: ContentRVAdapter
    private lateinit var searchViewTextListener: SearchView.OnQueryTextListener

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.currentLocationTv.isSelected = true // 텍스트 흐르는 효과

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUser() // user 정보 가져오기
        observeState()
        setupRecyclerView() // RecyclerView 설정
        setupSearchView() // 검색창 설정
    }


    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {  // user 정보 관찰
                    userViewModel.userState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                user = result.data
                                binding.currentLocationTv.text = user.location
                                postViewModel.getLocationPosts(user.location)
                            }

                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> Log.d("loading", "loading...")
                        }
                    }
                }

                launch {
                    postViewModel.postsState.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                AppUtils.dismissLoadingDialog()

                                if (result.data.isEmpty()) {
                                    binding.noText.visibility = View.VISIBLE
                                    binding.homeContentsRv.visibility = View.GONE
                                } else {

                                    binding.noText.visibility = View.GONE
                                    binding.homeContentsRv.visibility = View.VISIBLE

                                    contentAdapter.getList(result.data)
                                }
                            }

                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> AppUtils.showLoadingDialog(requireContext())

                        }
                    }
                }

            }
        }
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentRVAdapter(contentClickListener = { postId ->
            onPostClicked(postId) // 게시물 클릭 시 조회수 증가
            navigateToPostDetail(postId) // 게시물 상세 페이지로 이동
        })
        binding.homeContentsRv.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun onPostClicked(postId: String) {
        postViewModel.updatePostCnt(postId)
    }


    private fun navigateToPostDetail(postId: String) {
        val arguments = HomeFragmentDirections.actionHomeFragmentToInContentFragment(
            postId, user
        )
        findNavController().navigate(arguments)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setupSearchView() {
        val searchView = binding.searchBar
        searchView.isSubmitButtonEnabled = true

        searchView.setOnSearchClickListener {
            toggleLocationVisibility(false) // 위치 정보 숨김
        }

        searchView.setOnCloseListener {
            toggleLocationVisibility(true) // 위치 정보 표시
            observeState()
            false
        }

        searchViewTextListener = object : SearchView.OnQueryTextListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextSubmit(s: String): Boolean {
                contentAdapter.filter.filter(s) // 검색 텍스트 필터링
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        }
        searchView.setOnQueryTextListener(searchViewTextListener)
    }


    private fun toggleLocationVisibility(isVisible: Boolean) {
        binding.location.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.currentLocationTv.visibility = if (isVisible) View.VISIBLE else View.GONE
    }


    private fun handleError(exception: String) {
        Log.e("ERROR", "HomeFragment - $exception")
    }
}
