package com.example.community.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.MyApplication
import com.example.community.data.entity.Comment
import com.example.community.data.entity.Post
import com.example.community.data.entity.Reply
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentInContentBinding
import com.example.community.ui.writing.GalleryAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
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
    private val replyDB = Firebase.database.getReference("reply")
    private lateinit var postData: Post
    private var currentCommentIdx: Int = 0

    private lateinit var user: User
    private val gson: Gson = Gson()

    private lateinit var userUid: String
    private lateinit var userJson: String
    var i=0

    @RequiresApi(Build.VERSION_CODES.O)
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
        user = gson.fromJson(userJson, User::class.java)

        saveComment()

        return binding.root
    }


    private fun bind() {

        binding.backIv.setOnClickListener {// 뒤로가기
            findNavController().popBackStack()
        }

        // post값 가져와서 보여주기
        binding.contentTimeTv.text = postData.date
        binding.contentTv.text = postData.content
        binding.userNicknameTv.text = postData.nickname
        binding.titleTv.text = postData.title
        binding.titleTv.isSelected = true
        binding.viewTv.text = postData.view.toString()


        if (userUid == postData.uid) { // 현 사용자 uid와 post uid가 같으면
            binding.deleteTv.visibility = View.VISIBLE

            binding.deleteTv.setOnClickListener {
                deletePost() // 게시글 삭제
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveComment(){

        binding.commentSubmitBnt.setOnClickListener { // 댓글 db에 저장

            val content = binding.commentEt.text.toString()
            if (content.isEmpty()) {
                return@setOnClickListener
            }
            val formatter = DateTimeFormatter.ofPattern("MM/dd")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            val currentDate = LocalDateTime.now().format(formatter)
            val currentTime = LocalDateTime.now().format(timeFormatter)
            val comment =
                Comment(
                    userUid,
                    postData.postIdx,
                    content,
                    0,
                    user.nickname,
                    currentDate,
                    currentTime
                )
            setPost(comment)
            binding.commentEt.text = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickReply(adapter:CommentAdapter){

        adapter.setItemClickListener(object :CommentAdapter.ReplyInterface{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onReplyClicked(commentIdx: Int) {
                currentCommentIdx=commentIdx
                binding.commentLayout.visibility=View.GONE
                binding.replyLayout.visibility=View.VISIBLE


            }
        })
        binding.replySubmitBnt.setOnClickListener {
            val commentIdx=currentCommentIdx
            saveReply(commentIdx)
            Toast.makeText(requireContext(),"done",Toast.LENGTH_SHORT).show()
            binding.commentLayout.visibility=View.VISIBLE
            binding.replyLayout.visibility=View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveReply(commentIdx:Int){
        val content = binding.replyEt.text.toString()
        if (content.isEmpty()) {
            return
        }
        val formatter = DateTimeFormatter.ofPattern("MM/dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val currentDate = LocalDateTime.now().format(formatter)
        val currentTime = LocalDateTime.now().format(timeFormatter)
        val reply =
            Reply(
                userUid,
                postData.postIdx,
                user.nickname,
                currentDate,
                content,
                i,
                commentIdx
            )

        val replydb = FirebaseDatabase.getInstance().reference
        replydb.child("reply").child(i.toString()).setValue(reply)

    }


    private fun getComment(adapter: CommentAdapter) {  // 댓글 데이터 불러와서 화면에 표시

        binding.commentRv.adapter = adapter
        binding.commentRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        commentDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = ArrayList<Comment>() // 새로운 댓글
                if (snapshot.exists()) {
                    for (cmSnapShot in snapshot.children) {
                        val data = cmSnapShot.getValue(Comment::class.java)

                        if (data != null && data.postIdx == postData.postIdx) {
                            comments.add(data)
                        }
                    }
                }
                adapter.submitList(comments)  // 댓글 전체 update
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getCommentFail", error.toString())
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

        val postIdx = postData.postIdx // 게시글 삭제

        commentDB.orderByChild("postIdx")
            .equalTo(postIdx.toDouble()) // commentDB 에서 postIdx가 postDB의 postIdx와 같을 때
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onRecyclerView(){

        val imgAdapter = GalleryAdapter(requireContext())
        postData.imgs?.let { imgAdapter.submitList(it) }
        binding.imgRv.adapter = imgAdapter
        binding.imgRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val commentAdapter = CommentAdapter(userUid,replyDB,requireContext())
        getComment(commentAdapter)
        deleteComment(commentAdapter)
        clickReply(commentAdapter)

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        bind()
        onRecyclerView()
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
