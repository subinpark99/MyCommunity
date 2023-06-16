package com.example.community.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val postDB = Firebase.database.getReference("post")
    private val userDB = Firebase.database.getReference("user")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        initLocation()
        return binding.root
    }


    private fun initLocation(){
        val userUid= MyApplication.prefs.getUid("uid", "")

        userDB.child(userUid).child("location")
            .addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userLocation=snapshot.getValue(String::class.java)
                if (userLocation != null) {
                    binding.currentLocationTv.text=userLocation
                    getPost(userLocation)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getUser",error.toString())
            }

        })
    }

    private fun getPost(location:String) {

        val rvAdpater = ContentRVAdpater(requireContext())
        binding.homeContentsRv.apply {
            adapter = rvAdpater
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        val postdb = postDB.orderByChild("location").equalTo(location)
        postdb.addValueEventListener(object : ValueEventListener {  // 내 지역에 있는 게시물만 가져오기
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (contentSnapshot in snapshot.children.reversed()) { // reversed로 최근 게시물이 위로 오게

                        val post = contentSnapshot.getValue(Post::class.java)
                        if (post != null) {
                            rvAdpater.submitList(post)
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
                val arguments = HomeFragmentDirections.actionHomeFragmentToInContentFragment(post)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
