package com.dev.community.ui.mypage

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.community.util.AppUtils
import com.dev.community.data.model.PostWithImages
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.util.Result
import com.dev.community.databinding.FragmentMycontentsBinding
import com.dev.community.ui.home.adapter.ContentRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyContentsFragment : Fragment() {
    private var _binding: FragmentMycontentsBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels()
    private lateinit var contentAdapter: ContentRVAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMycontentsBinding.inflate(inflater, container, false)

        binding.backIv.setOnClickListener { findNavController().popBackStack() }

        setupRecyclerView()
        handleArguments()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeState()
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentRVAdapter(contentClickListener = { postData ->
            onPostClicked(postData)
        })
        binding.mycontentRv.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun onPostClicked(postData: PostWithImages) {
        postViewModel.updatePostCnt(postData.post.postId)
        val arguments =
            MyContentsFragmentDirections.actionMyContentsFragmentToInContentFragment(
                postData.post,
                postData.imageUrls.toTypedArray()
            )
        findNavController().navigate(arguments)
    }

    // 전달받은 데이터에 따른 화면 이동
    private fun handleArguments() {
        val args: MyContentsFragmentArgs by navArgs()
        when (args.page) {
            "contents" -> getMyContents()
            else -> getMyComments()
        }
    }

    // 내가 쓴 글 가져오기

    @SuppressLint("SetTextI18n")
    private fun getMyContents() {
        binding.mycontenttv.text = "내가 쓴 글"
        postViewModel.getMyPosts()
    }


    // 내가 쓴 댓글 가져오기
    @SuppressLint("SetTextI18n")
    private fun getMyComments() {
        binding.mycontenttv.text = "내가 쓴 댓글"
        postViewModel.getMyCommentedPost()
    }


    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    postViewModel.myPostListState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                AppUtils.dismissLoadingDialog()
                                if (result.data.isNotEmpty()) {
                                    binding.noText.visibility = View.GONE
                                    binding.mycontentRv.visibility = View.VISIBLE
                                    contentAdapter.getList(result.data)
                                } else {
                                    binding.noText.visibility = View.VISIBLE
                                    binding.mycontentRv.visibility = View.GONE
                                }
                            }

                            is Result.Error -> {
                                handleError(result.message)
                                AppUtils.dismissLoadingDialog()
                            }
                            is Result.Loading -> handleLoading()
                        }
                    }
                }

                launch {
                    postViewModel.myCommentsListState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                AppUtils.dismissLoadingDialog()
                                if (result.data.isNotEmpty()) {
                                    binding.noText.visibility = View.GONE
                                    binding.mycontentRv.visibility = View.VISIBLE
                                    contentAdapter.getList(result.data)
                                } else {
                                    binding.noText.visibility = View.VISIBLE
                                    binding.mycontentRv.visibility = View.GONE
                                }
                            }

                            is Result.Error -> {
                                handleError(result.message)
                                AppUtils.dismissLoadingDialog()
                            }

                            is Result.Loading -> handleLoading()
                        }
                    }
                }

            }
        }
    }

    private fun handleError(exception: String) {
        Log.e("ERROR", "MyContentsFragment - $exception")
    }

    private fun handleLoading() {
        AppUtils.showLoadingDialog(requireContext())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}