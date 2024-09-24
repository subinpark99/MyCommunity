package com.dev.community.ui.notice


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.community.data.model.User
import com.dev.community.util.AppUtils
import com.dev.community.ui.viewModel.CommentViewModel
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.util.Result
import com.dev.community.databinding.FragmentNoticeBinding
import com.dev.community.ui.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private var user = User()

    private lateinit var noticeAdapter: NoticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)

        binding.noItem.visibility = View.VISIBLE
        binding.noticeRv.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentViewModel.getNoticeComments()
        setupRecyclerView()
        observeState()
    }


    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userViewModel.userState.collectLatest {
                        when (it) {
                            is Result.Success -> user = it.data
                            is Result.Error -> Log.e("ERROR", it.message)
                            is Result.Loading -> Log.e("Loading", "로딩중")
                        }
                    }
                }


                launch {
                    commentViewModel.getNoticeComState
                        .collectLatest { result ->
                            when (result) {
                                is Result.Success -> {

                                    AppUtils.dismissLoadingDialog()

                                    if (result.data.isEmpty()) {
                                        binding.noItem.visibility = View.VISIBLE
                                        binding.noticeRv.visibility = View.GONE
                                    } else {
                                        binding.noItem.visibility = View.GONE
                                        binding.noticeRv.visibility = View.VISIBLE

                                        noticeAdapter.addComment(result.data)
                                    }
                                }

                                is Result.Error -> {
                                    handleError(result.message)
                                    AppUtils.dismissLoadingDialog() }
                                is Result.Loading -> handleLoading()
                            }
                        }
                }
            }
        }
    }


    private fun setupRecyclerView() {

        noticeAdapter = NoticeAdapter(commentClickListener = { postId ->
            postViewModel.updatePostCnt(postId)

            navigateToContent(postId)
        })
        binding.noticeRv.apply {
            adapter = noticeAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun navigateToContent(postId: String) {
        postViewModel.getPostById(postId)

        viewLifecycleOwner.lifecycleScope.launch {
            postViewModel.postState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val postData = result.data
                            if (postData.postId == postId) {
                                val arguments =
                                    NoticeFragmentDirections.actionNoticeFragmentToInContentFragment(
                                        postData.postId,
                                        user
                                    )
                                findNavController().navigate(arguments)
                            }
                        }

                        is Result.Error -> handleError(result.message)
                        is Result.Loading -> handleLoading()
                    }
                }
        }

    }

    private fun handleError(exception: String) {
        Log.e("ERROR", "NoticeFragment - $exception")
    }

    private fun handleLoading() {
        Log.d("loading", "loading...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}