package com.example.community.ui.notice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.MyApplication
import com.example.community.data.entity.Comment
import com.example.community.data.entity.Post
import com.example.community.databinding.FragmentNoticeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NoticeFragment:Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val commentDB= Firebase.database.getReference("comment")
    private val postDB= Firebase.database.getReference("post")
    private lateinit var userUid:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNoticeBinding.inflate(inflater, container, false)

        userUid = MyApplication.prefs.getUid("uid", "")

        return binding.root
    }

    private fun getMyComments(){

        val commmentAdpater= NoticeAdapter()
        binding.noticeRv.apply {
            adapter=commmentAdpater
            layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        val postdb=postDB.orderByChild("uid").equalTo(userUid)  // 내가 쓴 글 가져오기
        postdb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    for( contentSnapshot in snapshot.children.reversed()){ // reversed로 최근 게시물이 위로 오게

                        val post=contentSnapshot.getValue(Post::class.java)

                        if (post != null ) {
                            getCommentNotice(commmentAdpater)  // 내가 쓴 글의 댓글 가져오기
                            return  // 한 번만
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("getPost",error.toString())
            }
        })

//        rvAdpater.setItemClickListener(object : ContentRVAdpater.InContentInterface{
//            override fun onContentClicked(post: Post) {
//                onPostClicked(post.postIdx)
//                val arguments=MyReplyFragmentDirections.actionMyReplyFragmentToInContentFragment(post)
//                findNavController().navigate(arguments)
//            }
//        })
    }

    fun getCommentNotice(adapter: NoticeAdapter){

        commentDB.addListenerForSingleValueEvent((object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for( commentSnapshot in dataSnapshot.children.reversed()) {
                            val comment = commentSnapshot.getValue(Comment::class.java)
                        if (comment != null && comment.uid!=userUid) {  // 내가 쓴 댓글 알림은 뜨지 않게
                                adapter.submitList(comment)
                            }
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("getPost",databaseError.toString())
                }
            }))

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