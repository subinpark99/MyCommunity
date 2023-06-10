package com.example.community.ui.other

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentAgeBinding
import com.example.community.ui.home.ContentRVAdpater
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


class AgeFragment : Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!
    private val postDB = Firebase.database.getReference("post")
    private lateinit var range: IntRange
    private lateinit var ageRange: String
    private lateinit var user: User
    private val gson: Gson = Gson()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAgeBinding.inflate(inflater, container, false)

        binding.backIv.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        ageRange = arguments?.getString("age", "").toString()
        val userJson = MyApplication.prefs.getUser("user", "")
        user = gson.fromJson(userJson, User::class.java)

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

        return binding.root
    }


    private fun getAgePost() {

        val rvAdpater = ContentRVAdpater(requireContext())
        binding.ageContentsRv.apply {
            adapter = rvAdpater
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        postDB.orderByChild("location").equalTo(user.location)
        val postdb = postDB.orderByChild("location").equalTo(user.location)
        postdb.addValueEventListener(object : ValueEventListener {  // 내 지역에 있는 게시물만 가져오기
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (contentSnapshot in snapshot.children.reversed()) { // reversed로 최근 게시물이 위로 오게

                        val post = contentSnapshot.getValue(Post::class.java)
                        if (post != null) {
                            if (post.age in range) {
                                rvAdpater.submitList(post)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getPost", error.toString())
            }
        })

        rvAdpater.setItemClickListener(object : ContentRVAdpater.InContentInterface {
            override fun onContentClicked(post: Post) {
                onPostClicked(post.postIdx)

                val arguments = AgeFragmentDirections.actionAgeFragmentToInContentFragment(post)
                findNavController().navigate(arguments)
            }
        })
    }

    fun onPostClicked(postIdx: Int) {
        val updatedPost = FirebaseDatabase.getInstance().getReference("post")
            .child(postIdx.toString()) // 글 조회수 가져와서 증가
        updatedPost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    // 조회수 증가
                    post.view = post.view + 1
                    // 데이터베이스에 업데이트
                    updatedPost.setValue(post)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("onPostClicked", error.toString())
            }
        })
    }


    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        getAgePost()
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