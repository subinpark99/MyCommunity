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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.local.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.entity.Reply
import com.example.community.data.entity.User
import com.example.community.data.viewModel.CommentViewModel
import com.example.community.data.viewModel.PostViewModel
import com.example.community.databinding.FragmentInContentBinding
import com.example.community.ui.writing.GalleryAdapter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

class InContentFragment : Fragment() {

    private var _binding: FragmentInContentBinding? = null
    private val binding get() = _binding!!

    private val replyDB = Firebase.database.getReference("reply")
    private lateinit var postData: Post
    private var currentCommentIdx: Int = 0

    private var updateCommentIdx by Delegates.notNull<Int>()

    private val commentViewModel: CommentViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()

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

        saveComment(userUid)

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
    private fun saveComment(userUid: String){

        binding.commentSubmitBnt.setOnClickListener { // 댓글 db에 저장

            val content = binding.commentEt.text.toString()

            commentViewModel.getLatestComment().observe(this) { commentList ->
                if (commentList != null && commentList.isNotEmpty()) {
                    val latestComment= commentList.maxByOrNull { it.commentIdx }
                    updateCommentIdx = latestComment!!.commentIdx.plus(1)
                    updateComment(userUid,updateCommentIdx,content)
                } else updateComment(userUid,0,content)
            }

            binding.commentEt.text = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateComment(
        userUid: String, updateCommentIdx: Int, content: String
    ) {

        val formatter = DateTimeFormatter.ofPattern("MM/dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val currentDate = LocalDateTime.now().format(formatter)
        val currentTime = LocalDateTime.now().format(timeFormatter)

        commentViewModel.addComment(userUid,postData.postIdx,content,updateCommentIdx,postData.nickname,
        currentDate,currentTime)

        setCommentState()
    }


    private fun setCommentState() {
        commentViewModel.addCommentState.observe(this) { state ->
            when (state) {
                true -> {
                    Toast.makeText(requireContext(), "완료", Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("setComment", "failed")

            }
        }
    }


    private fun getComment(adapter: CommentAdapter) {  // 댓글 데이터 불러와서 화면에 표시

        binding.commentRv.adapter = adapter
        binding.commentRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        commentViewModel.getComment(postData.postIdx).observe(this){
            if (it!=null){
                adapter.submitList(it)
            }
        }
    }


    private fun deleteComment(adapter: CommentAdapter) {  // 댓글 삭제
        adapter.setItemClickListener(object : CommentAdapter.DeleteInterface {
            override fun onDeleteClicked(commentIdx: Int) {

                commentViewModel.deleteComment(commentIdx)

                deleteCommentState()
            }
        })
    }

    private fun deleteCommentState(){
        commentViewModel.deleteCommentState.observe(this) { state ->
            when (state) {
                true -> {
                    Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("deleteComment", "failed")

            }
        }
    }

    private fun deletePost() {

        val postIdx = postData.postIdx // 게시글 삭제

        commentViewModel.deleteAllPostComment(postIdx)
        deletePostCommentsState(postIdx)
    }

    private fun deletePostCommentsState(postIdx:Int){  // 게시글 연관 댓글 모두 삭제
        commentViewModel.deletePostCommentState.observe(this) { state ->
            when (state) {
                true -> {
                    postViewModel.deletePost(postIdx)
                    deletePostState()
                }
                else -> Log.d("deletePostComments", "failed")
            }
        }
    }

    private fun deletePostState(){
        postViewModel.deletePostState.observe(this) { state ->
            when (state) {
                true -> {
                    val arguments =
                        InContentFragmentDirections.actionInContentFragmentToHomeFragment()
                    findNavController().navigate(arguments)
                    Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("deletePost", "failed")

            }
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
