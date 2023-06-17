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
import com.example.community.data.entity.Comment
import com.example.community.data.entity.Post
import com.example.community.databinding.FragmentMyreplyBinding
import com.example.community.ui.home.ContentRVAdpater
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MyReplyFragment : Fragment() {
    private var _binding: FragmentMyreplyBinding? = null
    private val binding get() = _binding!!

    private val commentDB = Firebase.database.getReference("comment")
    private val postDB = Firebase.database.getReference("post")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMyreplyBinding.inflate(inflater, container, false)

        binding.backIv.setOnClickListener { findNavController().popBackStack() }
        return binding.root
    }

    private fun getMyComments() {

        val rvAdpater = ContentRVAdpater(requireContext())
        binding.myreplyRv.apply {
            adapter = rvAdpater
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        val userUid = MyApplication.prefs.getUid("uid", "")
        val postset = HashSet<Int>() // 중복 제거

        val commentdb = commentDB.orderByChild("uid").equalTo(userUid)
        commentdb.addValueEventListener(object :
            ValueEventListener {  // comment uid가 current user일 때
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (contentSnapshot in snapshot.children.reversed()) { // reversed로 최근 게시물이 위로 오게

                        val comment = contentSnapshot.getValue(Comment::class.java)

                        if (comment != null) {
                            if (!postset.contains(comment.postIdx)) {
                                postset.add(comment.postIdx)  // 한 게시물에 여러 개의 댓글이 있을 때, 게시물 중복 제거
                                getPost(
                                    rvAdpater,
                                    comment.postIdx
                                ) // commentDB의 postIdx와 postDB의 postIdx가 같은 게시물 가져옴
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
                val arguments =
                    MyReplyFragmentDirections.actionMyReplyFragmentToInContentFragment(post)
                findNavController().navigate(arguments)
            }
        })
    }

    fun getPost(adapter: ContentRVAdpater, commentIdx: Int) {

        val postdb = postDB.orderByChild("postIdx").equalTo(commentIdx.toDouble())
        postdb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val postSnapshot = dataSnapshot.children.first()
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        adapter.submitList(post)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("getPost", databaseError.toString())
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
        getMyComments()
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