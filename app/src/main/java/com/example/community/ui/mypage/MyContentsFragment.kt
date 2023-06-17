package com.example.community.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.local.MyApplication
import com.example.community.data.entity.Post
import com.example.community.databinding.FragmentMycontentsBinding
import com.example.community.ui.home.ContentRVAdpater
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MyContentsFragment : Fragment() {
    private var _binding: FragmentMycontentsBinding? = null
    private val binding get() = _binding!!

    private val postDB = Firebase.database.getReference("post")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMycontentsBinding.inflate(inflater, container, false)

        binding.backIv.setOnClickListener { findNavController().popBackStack() }

        return binding.root
    }

    private fun getMyContents() {

        val rvAdpater = ContentRVAdpater(requireContext())
        binding.mycontentRv.apply {
            adapter = rvAdpater
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        val userUid = MyApplication.prefs.getUid("uid", "")

        val postdb = postDB.orderByChild("uid").equalTo(userUid)
        postdb.addValueEventListener(object :
            ValueEventListener {  // post uid가 current user인 게시물 가져오기
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (contentSnapshot in snapshot.children.reversed()) { // reversed로 최근 게시물이 위로 오게

//                        val post = contentSnapshot.getValue(Post::class.java)
//                        if (post != null) {
//                            rvAdpater.submitList(post)
//                        }
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
                val arguments =
                    MyContentsFragmentDirections.actionMyContentsFragmentToInContentFragment(post)
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
        getMyContents()
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