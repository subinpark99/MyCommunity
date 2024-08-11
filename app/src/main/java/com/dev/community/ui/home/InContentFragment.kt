package com.dev.community.ui.home


import android.annotation.SuppressLint
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.community.data.model.Comment
import com.dev.community.data.model.Post
import com.dev.community.data.model.User
import com.dev.community.util.AppUtils
import com.dev.community.ui.viewModel.CommentViewModel
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.databinding.FragmentInContentBinding
import com.dev.community.ui.home.adapter.CommentAdapter
import com.dev.community.ui.writing.GalleryAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.ui.writing.ImageDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class InContentFragment : Fragment() {
    private var _binding: FragmentInContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var post: Post
    private lateinit var user: User
    private var isReply = false
    private var parentId: String = ""

    private val userViewModel: UserViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInContentBinding.inflate(inflater, container, false)
        val postArgs = navArgs<InContentFragmentArgs>().value
        post = postArgs.postData

        binding.post = post

        setupUI(postArgs.imgList.toList())
        setClickListeners(post)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUser()
        observeState()

    }

    private fun setupUI(imgList: List<String>) {

        binding.backIv.setOnClickListener { findNavController().popBackStack() }
        binding.commentEt.hint = "댓글 작성"
        setImageAdapter(imgList)
    }

    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 사용자 상태
                launch {
                    userViewModel.userState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                user = result.data

                                if (user.uid == post.uid) {
                                    binding.deleteTv.visibility = View.VISIBLE
                                    deletePost()
                                } else binding.deleteTv.visibility = View.GONE

                                commentViewModel.getComments(post.postId)  // 댓글 리스트 가져오기
                            }

                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }

                // 댓글 삭제 상태
                launch {
                    commentViewModel.deleteCommentState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> if (result.data) AppUtils.showToast(
                                requireContext(),
                                "완료"
                            )

                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }

                // 댓글 추가 상태
                launch {
                    commentViewModel.addCommentState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
                                    AppUtils.dismissLoadingDialog()
                                    AppUtils.showToast(requireContext(), "완료")
                                }
                            }
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }

                // 게시글 삭제 상태
                launch {
                    postViewModel.deletePostState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> if (result.data) findNavController().popBackStack()
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }

                // 댓글 가져오기 상태
                launch {
                    commentViewModel.getCommentListState
                        .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                        .collectLatest { result ->
                            when (result) {
                                is Result.Success -> setCommentAdapter(result.data)
                                is Result.Error -> handleError(result.message)
                                is Result.Loading -> handleLoading()
                            }
                        }
                }
            }
        }
    }


    private fun deletePost() {
        binding.deleteTv.setOnClickListener {
            postViewModel.deletePost(post.postId)  // 게시글 삭제
        }
    }

    private fun setClickListeners(post: Post) {
        binding.commentSubmitBnt.setOnClickListener {
            val content = binding.commentEt.text
            if (content.toString().isNotEmpty()) {

                val currentParentId = if (isReply) parentId else ""

                commentViewModel.addComment(
                    post.postId,
                    user.nickname,
                    content.toString(),
                    currentParentId,
                    user.alarm
                )

                isReply = false
                parentId = ""
                binding.commentEt.hint = "댓글 작성"
                content.clear()

            } else AppUtils.showSnackbar(binding.root, "댓글을 작성해주세요")
        }
    }


    private fun setImageAdapter(imgList: List<String>) {
        val imgAdapter = GalleryAdapter(requireContext(),
            imageClickListener = { ImageDialog(it).show(parentFragmentManager, "full_image") })
        binding.imgRv.adapter = imgAdapter
        binding.imgRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        imgAdapter.submitList(imgList)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setCommentAdapter(
        commentsWithReplies: List<Comment>
    ) {
        val commentAdapter = CommentAdapter(
            user.uid,
            deleteClickListener = { commentId, parentId ->
                commentViewModel.deleteComment(commentId, parentId)  // 댓글 삭제
            },
            replyClickListener = { comment ->
                onReplyIconClick(comment)
            }, commentsWithReplies
        )

        binding.commentRv.adapter = commentAdapter
        binding.commentRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        commentAdapter.submitList(commentsWithReplies)
    }

    private fun onReplyIconClick(comment: Comment) {
        isReply = true
        parentId = comment.commentId
        binding.commentEt.hint = "${comment.nickname} 에게 대댓글 작성"
    }


    private fun handleError(exception: String) {
        Log.e("ERROR", "InContentFragment - $exception")
    }

    private fun handleLoading() {
        Log.d("loading", "loading...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}