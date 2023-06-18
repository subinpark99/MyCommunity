package com.example.community.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.local.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.viewModel.PostViewModel
import com.example.community.databinding.FragmentMycontentsBinding
import com.example.community.ui.home.ContentRVAdpater


class MyContentsFragment : Fragment() {
    private var _binding: FragmentMycontentsBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMycontentsBinding.inflate(inflater, container, false)

        binding.backIv.setOnClickListener { findNavController().popBackStack() }

        val userUid = MyApplication.prefs.getUid("uid", "")
        getMyContents(userUid)

        return binding.root
    }

    private fun getMyContents(userUid: String) {

        val rvAdpater = ContentRVAdpater(requireContext())
        binding.mycontentRv.apply {
            adapter = rvAdpater
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        postViewModel.getMyPosts(userUid).observe(this) {

            if (it != null) {
                rvAdpater.getMyList(it)
            }
        }


        rvAdpater.setItemClickListener(object : ContentRVAdpater.InContentInterface {
            override fun onContentClicked(post: Post) {
                onPostClicked(post.postIdx)
                val arguments =
                    MyContentsFragmentDirections.actionMyContentsFragmentToInContentFragment(post)
                findNavController().navigate(arguments)
            }
        })
    }

    fun onPostClicked(postIdx: Int) {
        postViewModel.updatePostCnt(postIdx)
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