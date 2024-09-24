package com.dev.community.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.dev.community.databinding.FragmentAgeBinding
import com.dev.community.ui.home.adapter.ContentRVAdapter
import com.dev.community.ui.start.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgeFragment : Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!

    private lateinit var range: IntRange
    private lateinit var ageRange: String
    private var user = User()
    private lateinit var contentRVAdapter: ContentRVAdapter

    private val postViewModel: PostViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentAgeBinding.inflate(inflater, container, false)

        binding.backIv.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        ageRange = arguments?.getString("age", "").toString()
        binding.ageTv.text = ageRange

        val ageRanges = listOf(
            "10대" to 10..19,
            "20대" to 20..29,
            "30대" to 30..39,
            "40대" to 40..49,
            "50대" to 50..59,
            "60대" to 60..69
        )

        range = ageRanges.firstOrNull { it.first == ageRange }?.second!!
        setRvAdapter()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUser()
        observeState()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    userViewModel.userState.collectLatest {
                        when (it) {
                            is Result.Success -> {
                                user= it.data
                                postViewModel.getLocationPosts(it.data.location)
                            }

                            is Result.Error -> Log.e("ERROR", it.message)
                            is Result.Loading -> Log.e("Loading", "로딩중")

                        }
                    }
                }

                launch {
                    postViewModel.postsState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {

                                AppUtils.dismissLoadingDialog()

                                val ageRange = result.data.filter {   // ageRange로 필터링된 리스트 생성
                                    it.age in range
                                }

                                if (ageRange.isEmpty()) {
                                    binding.noText.visibility = View.VISIBLE
                                    binding.ageContentsRv.visibility = View.GONE
                                } else {
                                    contentRVAdapter.getList(ageRange)
                                    binding.noText.visibility = View.GONE
                                    binding.ageContentsRv.visibility = View.VISIBLE
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

    private fun setRvAdapter() {

        contentRVAdapter = ContentRVAdapter(contentClickListener = { postId ->
            onPostClicked(postId)
        })
        binding.ageContentsRv.apply {
            adapter = contentRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun onPostClicked(postId:String) { // 게시물 조회수 증가
        postViewModel.updatePostCnt(postId)
        val arguments = AgeFragmentDirections.actionAgeFragmentToInContentFragment(
            postId, user)
        findNavController().navigate(arguments)
    }


    private fun handleError(exception: String) {
        Log.e("ERROR", "AgeFragment - $exception")
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