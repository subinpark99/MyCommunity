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

    private val commentDB= Firebase.database.getReference("comment")
    private lateinit var postData:Post

    private lateinit var user: User
    private val gson : Gson = Gson()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInContentBinding.inflate(inflater, container, false)

        val args:InContentFragmentArgs by navArgs()
        postData=args.post
        bind()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun bind(){

        binding.backIv.setOnClickListener {// 뒤로가기
            findNavController().popBackStack()
        }

        // post값 가져와서 보여주기
        binding.contentTimeTv.text=postData.date
        binding.contentTv.text=postData.content
        binding.userNicknameTv.text=postData.nickname
        binding.titleTv.text=postData.title
        binding.viewTv.text=postData.view.toString()

        val adpater= postData.imgs?.let { ImageAdapter(requireContext(), it) }
        binding.imgRv.adapter= adpater
        binding.imgRv.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.replySubmitBnt.setOnClickListener { // 댓글 db에 저장
            val userUid = MyApplication.prefs.getUid("uid", "")
            val userJson = MyApplication.prefs.getUser("user", "")
            user = gson.fromJson(userJson, User::class.java)
            addComment(userUid, postData.postIdx,user.nickname)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addComment(uid:String, postIdx:Int, nickname:String){

        val content=binding.replyEt.text.toString()
        val formatter = DateTimeFormatter.ofPattern("MM/dd")
        val currentTime = LocalDateTime.now().format(formatter)

        val comment=Comment(uid,postIdx,content,0,nickname,currentTime)
        setPost(comment)
    }

    private fun getComment(){  // 댓글 데이터 불러와서 화면에 표시

        val adapter=CommentAdapter()
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


    private fun getLastCommentIdx(completion: (Int) -> Unit) {  // 마지막 postIdx 값 불러옴
        commentDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lastCommentIdx = 0
                for (postSnapshot in snapshot.children) {
                    val comment = postSnapshot.getValue(Comment::class.java)
                    if (comment != null && comment.commentIdx > lastCommentIdx) {
                        lastCommentIdx = comment.commentIdx
                    }
                }
                completion(lastCommentIdx)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("getLastPostIdx",error.toString())
            }
        })
    }

    private fun setPost(comment: Comment) {  // commentIdx 1씩 증가
        getLastCommentIdx { lastCommentIdx ->
            val newCommentIdx = lastCommentIdx + 1
            comment.commentIdx = newCommentIdx
            commentDB.child(newCommentIdx.toString()).setValue(comment)
        }
    }


    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        getComment()
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