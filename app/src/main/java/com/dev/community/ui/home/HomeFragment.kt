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
import com.dev.community.data.model.PostWithImages
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.FragmentHomeBinding
import com.dev.community.ui.home.adapter.ContentRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val postViewModel: PostViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var contentAdapter: ContentRVAdapter
    private lateinit var searchViewTextListener: SearchView.OnQueryTextListener

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                    userViewModel.userState.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val user = result.data
                                binding.currentLocationTv.text = user.location
                                postViewModel.getLocationPostWithImg(user.location)  // 지역별 게시물 리스트 가져오기
                            }

                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> Log.d("loading", "loading...")
                        }
                    }
                }

                launch {
                    postViewModel.locationPostWithImgState.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                AppUtils.dismissLoadingDialog()

                                if (result.data.isEmpty()) {
                                    binding.noText.visibility = View.VISIBLE
                                    binding.homeContentsRv.visibility = View.GONE
                                } else {
                                    contentAdapter.getList(result.data)
                                    binding.noText.visibility = View.GONE
                                    binding.homeContentsRv.visibility = View.VISIBLE
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
        contentAdapter = ContentRVAdapter(contentClickListener = { postData ->
            onPostClicked(postData) // 게시물 클릭 시 조회수 증가
            navigateToPostDetail(postData) // 게시물 상세 페이지로 이동
        })
        binding.homeContentsRv.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun onPostClicked(postData: PostWithImages) {
        postViewModel.updatePostCnt(postData.post.postId)
    }


    private fun navigateToPostDetail(postData: PostWithImages) {
        val arguments = HomeFragmentDirections.actionHomeFragmentToInContentFragment(
            postData.post,
            postData.imageUrls.toTypedArray()
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
