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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.community.data.model.Comment
import com.dev.community.data.model.User
import com.dev.community.util.AppUtils
import com.dev.community.ui.viewModel.CommentViewModel
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.databinding.FragmentInContentBinding
import com.dev.community.ui.home.adapter.CommentAdapter
import com.dev.community.ui.writing.GalleryAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.dev.community.util.Result
import com.dev.community.ui.writing.ImageDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class InContentFragment : Fragment() {
    private var _binding: FragmentInContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var postId: String
    private lateinit var user: User

    private var isReply = false
    private var parentId: String = ""

    private val commentViewModel: CommentViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInContentBinding.inflate(inflater, container, false)

        val postArgs = navArgs<InContentFragmentArgs>().value
        postId = postArgs.postId
        user = postArgs.user

        setupUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postViewModel.getPostById(postId)
        commentViewModel.getComments(postId)
        observeState()
    }

    private fun setupUI() {

        setClickListeners()
        binding.backIv.setOnClickListener { findNavController().popBackStack() }
        binding.commentEt.hint = "댓글 작성"

    }

    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    postViewModel.postState.collectLatest { result ->
                        when (result) {
                            is Result.Success ->{
                                if (user.uid == result.data.uid) {
                                    binding.deleteTv.visibility = View.VISIBLE
                                    deletePost()
                                } else binding.deleteTv.visibility = View.GONE

                                binding.post = result.data  // 데이터 바인딩
                                setImageAdapter(result.data.imageList)
                            }
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }

                }


                launch {
                    commentViewModel.doneState.collect { result ->
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

                // 게시글 삭제 상태
                launch {
                    postViewModel.deletePostState.collect { result ->
                        when (result) {
                            is Result.Success -> if (result.data) findNavController().popBackStack()
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }

                // 댓글 가져오기 상태
                launch {
                    commentViewModel.getCommentListState.collectLatest { result ->
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
            postViewModel.deletePost(postId)  // 게시글 삭제
        }
    }

    private fun setClickListeners() {

        binding.commentSubmitBnt.setOnClickListener {
            val content = binding.commentEt.text
            if (content.toString().isNotEmpty()) {

                val currentParentId = if (isReply) parentId else ""

                commentViewModel.addComment(
                    postId,
                    user.nickname,
                    content.toString(),
                    currentParentId
                )
                commentViewModel.sendPushAlarm(postId,content.toString())

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
        commentsWithReplies: List<Comment>,
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