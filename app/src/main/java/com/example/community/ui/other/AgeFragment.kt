package com.example.community.ui.other

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentAgeBinding
import com.example.community.ui.home.ContentRVAdpater
import com.example.community.ui.home.HomeFragmentDirections
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class AgeFragment(
    private val ageRange:String,private val user:User): Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!
    private val postDB= Firebase.database.getReference("post")
    private lateinit var range: IntRange

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

       binding.ageTv.text=ageRange
        val age=mutableListOf("10대", "20대","30대","40대","50대","60대")
        if (ageRange==age[0]) range=10..19
        if (ageRange==age[1]) range=20..29
        if (ageRange==age[2]) range=30..39
        if (ageRange==age[3]) range=40..49
        if (ageRange==age[4]) range=50..59
        if (ageRange==age[5]) range=60..69

        getAgePost()

        return binding.root
    }


    private fun getAgePost(){

        val rvAdpater= ContentRVAdpater(requireContext())
        binding.ageContentsRv.apply {
            adapter=rvAdpater
            layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        postDB.orderByChild("location").equalTo(user.location)
        val postdb=postDB.orderByChild("location").equalTo(user.location)
        postdb.addValueEventListener(object : ValueEventListener {  // 내 지역에 있는 게시물만 가져오기
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists())
                {
                    for( contentSnapshot in snapshot.children.reversed()){ // reversed로 최근 게시물이 위로 오게

                        val post=contentSnapshot.getValue(Post::class.java)
                        if (post != null) {
                            if (post.age in range) {
                                rvAdpater.submitList(post)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("getPost",error.toString())
            }
        })

        rvAdpater.setItemClickListener(object : ContentRVAdpater.InContentInterface{
            override fun onContentClicked(post: Post) {
                onPostClicked(post.postIdx)

              //  val arguments=AgeFragmentDirections.
                //val arguments= HomeFragmentDirections.actionHomeFragmentToInContentFragment(post)
                //findNavController().navigate(arguments)
            }
        })
    }

    fun onPostClicked(postIdx: Int) {
        val updatedPost = FirebaseDatabase.getInstance().getReference("post").child(postIdx.toString()) // 글 조회수 가져와서 증가
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
                Log.d("onPostClicked",error.toString())
            }
        })
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