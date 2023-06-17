package com.example.community.ui.other

import android.content.Intent
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
import com.example.community.data.entity.User
import com.example.community.data.viewModel.PostViewModel
import com.example.community.databinding.FragmentAgeBinding
import com.example.community.ui.home.ContentRVAdpater
import com.google.gson.Gson


class AgeFragment : Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!

    private lateinit var range: IntRange
    private lateinit var ageRange: String
    private val gson: Gson = Gson()

    private val postViewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAgeBinding.inflate(inflater, container, false)

        val userJson = MyApplication.prefs.getUser("user", "")
        val user = gson.fromJson(userJson, User::class.java)

        binding.backIv.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
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

        getAgePost(user.location)

        return binding.root
    }

    private fun getAgePost(userLocation: String) {

        val contentAdpater = ContentRVAdpater(requireContext())
        binding.ageContentsRv.apply {
            adapter = contentAdpater
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        postViewModel.getLocationPost(userLocation).observe(this) { postList ->
            if (postList != null) {
                val ageRange = postList.filter { it.age in range }
                contentAdpater.ageList(ageRange)
            }
        }

        contentAdpater.setItemClickListener(object : ContentRVAdpater.InContentInterface {
            override fun onContentClicked(post: Post) {
                onPostClicked(post.postIdx)

                val arguments = AgeFragmentDirections.actionAgeFragmentToInContentFragment(post)
                findNavController().navigate(arguments)
            }
        })
    }

    fun onPostClicked(postIdx: Int) { // 게시물 조회수 증가
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