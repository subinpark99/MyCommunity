package com.dev.community.ui.writing


import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.community.data.model.User
import com.dev.community.util.AppUtils
import com.dev.community.ui.viewModel.PostViewModel
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.FragmentWritingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class WritingFragment : Fragment() {
    private var _binding: FragmentWritingBinding? = null
    private val binding get() = _binding!!
    private val postViewModel: PostViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private var imgList = arrayListOf<String>()  // 이미지 리스트 저장
    private lateinit var user: User

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingBinding.inflate(inflater, container, false)

        binding.currentLocationTv.isSelected = true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeGetUser()
        setupClickListeners()

    }

    private fun observeGetUser() {
        userViewModel.getUser()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    userViewModel.userState.collect {
                        when (it) {
                            is Result.Success -> {
                                user = it.data
                                binding.currentLocationTv.text = it.data.location
                            }

                            is Result.Error -> handleError(it.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }

                launch {
                    postViewModel.addPostState.collect { post ->
                        when (post) {
                            is Result.Success -> {

                                val arguments =
                                    WritingFragmentDirections.actionWritingFragmentToInContentFragment(
                                        post.data, imgList.toTypedArray()
                                    )
                                findNavController().navigate(arguments)

                            }

                            is Result.Error -> handleError(post.message)
                            is Result.Loading -> handleLoading()
                        }
                    }


                }
            }
        }
    }


    private fun setupClickListeners() {
        binding.writeDoneIv.setOnClickListener {
            addPost(user)
            clearInputFields()
            AppUtils.hideKeyboard(requireActivity())
        }

        binding.addPhotoIv.setOnClickListener {
            AppUtils.getPermission(requireContext(), getImage, requireActivity())
        }
    }


    private fun clearInputFields() {
        binding.writingContentEt.text = null
        binding.writingTitleEt.text = null
        binding.writeGalleryRv.layoutManager = null
    }


    private fun addPost(user: User) {
        val title = binding.writingTitleEt.text.toString()
        val content = binding.writingContentEt.text.toString()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            postViewModel.addPost(
                user.age,
                user.location,
                user.nickname,
                title,
                content, imgList
            )

        } else AppUtils.showToast(requireContext(), "제목과 내용을 입력하세요.")
    }


    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount

                for (i in 0 until count) {
                    val imageUri = result.data?.clipData!!.getItemAt(i).uri
                    imgList.add(imageUri.toString())
                    updateRecyclerView()
                }
            }
        }

    }

    private fun updateRecyclerView() {
        val galleryRVAdapter = GalleryAdapter(
            requireContext(),
            imageClickListener = { ImageDialog(it).show(parentFragmentManager, "full_image") })
        galleryRVAdapter.submitList(imgList)

        binding.writeGalleryRv.apply {
            adapter = galleryRVAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun handleError(exception: String) {
        Log.e("ERROR", "WritingFragment - $exception")
    }

    private fun handleLoading() {
        Log.d("loading", "loading...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

