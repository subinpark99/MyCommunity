package com.example.community.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.MyApplication
import com.example.community.data.entity.Comment
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentInContentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InContentFragment : Fragment() {

    private var _binding: FragmentInContentBinding? = null
    private val binding get() = _binding!!

    private val commentDB = Firebase.database.getReference("comment")
    private val postDB = Firebase.database.getReference("post")
    private lateinit var postData: Post

    private lateinit var user: User
    private val gson: Gson = Gson()

    private lateinit var userUid: String
    private lateinit var userJson: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInContentBinding.inflate(inflater, container, false)

        val args: InContentFragmentArgs by navArgs()
        postData = args.post

        userUid = MyApplication.prefs.getUid("uid", "")
        userJson = MyApplication.prefs.getUser("user", "")

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun bind() {

        binding.backIv.setOnClickListener {// 뒤로가기
            findNavController().popBackStack()
        }

        // post값 가져와서 보여주기
        binding.contentTimeTv.text = postData.date
        binding.contentTv.text = postData.content
        binding.userNicknameTv.text = postData.nickname
        binding.titleTv.text = postData.title
        binding.titleTv.isSelected=true
        binding.viewTv.text = postData.view.toString()


        binding.replySubmitBnt.setOnClickListener { // 댓글 db에 저장
            user = gson.fromJson(userJson, User::class.java)
            val content = binding.replyEt.text.toString()
            if (content.isEmpty()) {
                return@setOnClickListener
            }
            val formatter = DateTimeFormatter.ofPattern("MM/dd")
            val timeFormatter=DateTimeFormatter.ofPattern("HH:mm")

            val currentDate = LocalDateTime.now().format(formatter)
            val currentTime=LocalDateTime.now().format(timeFormatter)
            val comment =
                Comment(userUid, postData.postIdx, content, 0, user.nickname, currentDate,currentTime)
            setPost(comment)
            binding.replyEt.text=null
        }

        val adapter = CommentAdapter(userUid)
        getComment(adapter)
        deleteComment(adapter)

        if (userUid == postData.uid) { // 현 사용자 uid와 post uid가 같으면
            binding.deleteTv.visibility = View.VISIBLE

            binding.deleteTv.setOnClickListener {
                deletePost() // 게시글 삭제
                }
            }
        }

    private fun getComment(adapter: CommentAdapter){  // 댓글 데이터 불러와서 화면에 표시

        binding.replyRv.adapter=adapter
        binding.replyRv.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        commentDB.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments= ArrayList<Comment>() // 새로운 댓글
                if (snapshot.exists()) {
                    for(cmSnapShot in snapshot.children) {
                        val data = cmSnapShot.getValue(Comment::class.java)

                        if (data != null && data.postIdx==postData.postIdx) {
                            comments.add(data)
                        }
                    }
                }
                adapter.submitList(comments)  // 댓글 전체 update
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("getCommentFail",error.toString())
            }
        })
    }

    private fun deleteComment(adapter: CommentAdapter) {  // 댓글 삭제
        adapter.setItemClickListener(object : CommentAdapter.DeleteInterface {
            override fun onDeleteClicked(commentIdx: Int) {
                commentDB.child(commentIdx.toString()).removeValue().addOnSuccessListener {
                }.addOnFailureListener { exception ->
                    Log.d("deleteComment", "Failed to delete comment: $exception")
                }
            }
        })
    }

    private fun deletePost() {

        val postIdx= postData.postIdx // 게시글 삭제

        commentDB.orderByChild("postIdx").equalTo(postIdx.toDouble()) // commentDB 에서 postIdx가 postDB의 postIdx와 같을 때
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    for (commentSnapshot in dataSnapshot.children) { // 각 댓글에 대한 데이터
                         commentSnapshot.ref.removeValue()
                    }

                     postDB.child(postIdx.toString()).removeValue()

                    val arguments =
                        InContentFragmentDirections.actionInContentFragmentToHomeFragment()
                    findNavController().navigate(arguments)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("deletePost", databaseError.toString())
                }
            })

    }


    private fun setPost(comment: Comment) { // commentIdx 1씩 증가
            getLastCommentIdx { lastCommentIdx ->
                val newCommentIdx = lastCommentIdx + 1
                comment.commentIdx = newCommentIdx
                commentDB.child(newCommentIdx.toString()).setValue(comment)
            }
        }

        private fun getLastCommentIdx(completion: (Int) -> Unit) { // 마지막 commentIdx 값 불러옴
            commentDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lastCommentIdx = 0
                    for (commentSnapshot in snapshot.children) {
                        val comment = commentSnapshot.getValue(Comment::class.java)
                        if (comment != null && comment.commentIdx > lastCommentIdx) {
                            lastCommentIdx = comment.commentIdx
                        }
                    }
                    completion(lastCommentIdx)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("getLastCommentIdx", error.toString())
                }
            })
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        bind()
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
