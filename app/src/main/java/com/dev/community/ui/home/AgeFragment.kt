package com.dev.community.ui.home

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
import com.dev.community.data.model.PostWithImages
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.FragmentAgeBinding
import com.dev.community.ui.home.adapter.ContentRVAdapter
import com.dev.community.ui.start.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgeFragment : Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!

    private lateinit var range: IntRange
    private lateinit var ageRange: String
    private lateinit var contentRVAdapter: ContentRVAdapter

    private val postViewModel: PostViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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


    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    userViewModel.userState.collect {
                        when (it) {
                            is Result.Success -> postViewModel.getLocationPostWithImg(it.data.location)
                            is Result.Error -> Log.e("ERROR", it.message)
                            is Result.Loading -> Log.e("Loading", "로딩중")

                        }
                    }
                }

                launch {
                    lifecycleScope.launch {
                        postViewModel.locationPostWithImgState.collect { result ->
                            when (result) {
                                is Result.Success -> {

                                    AppUtils.dismissLoadingDialog()
                                    val ageRange = result.data.filter {   // ageRange로 필터링된 리스트 생성
                                        it.post.age in range
                                    }

                                    // 필터링된 리스트를 다시 PostWithImages 형태로 변환
                                    val transformedData = ageRange.map { postWithImages ->
                                        PostWithImages(
                                            postWithImages.post,
                                            postWithImages.imageUrls
                                        )
                                    }

                                    if (transformedData.isEmpty()) {
                                        binding.noText.visibility = View.VISIBLE
                                        binding.ageContentsRv.visibility = View.GONE
                                    } else {
                                        contentRVAdapter.getList(transformedData)
                                        binding.noText.visibility = View.GONE
                                        binding.ageContentsRv.visibility = View.VISIBLE
                                    }
                                }

                                is Result.Error -> Log.e("ERROR", "AgeFragment - ${result.message}")
                                is Result.Loading -> AppUtils.showLoadingDialog(requireContext())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setRvAdapter() {

        contentRVAdapter = ContentRVAdapter(contentClickListener = { postData ->
            onPostClicked(postData)
        })
        binding.ageContentsRv.apply {
            adapter = contentRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun onPostClicked(postData: PostWithImages) { // 게시물 조회수 증가
        postViewModel.updatePostCnt(postData.post.postId)
        val arguments = AgeFragmentDirections.actionAgeFragmentToInContentFragment(
            postData.post,
            postData.imageUrls.toTypedArray()
        )
        findNavController().navigate(arguments)
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