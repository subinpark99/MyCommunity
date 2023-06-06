package com.example.community.ui.writing

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.community.data.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentWritingBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WritingFragment: Fragment() {
    private var _binding: FragmentWritingBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: User
    private val gson : Gson = Gson()
    private val postDB=Firebase.database.getReference("post")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWritingBinding.inflate(inflater, container, false)

        val userJson=MyApplication.prefs.getUser("user","")
        user=gson.fromJson(userJson,User::class.java)

        binding.currentLocationTv.text=user.location
        binding.writeDoneIv.setOnClickListener {

            addPost()
        }

        return binding.root
    }

    private fun getLastPostIdx(completion: (Int) -> Unit) {  // 마지막 postIdx 값 불러옴
        postDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lastPostIdx = 0
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null && post.postIdx > lastPostIdx) {
                        lastPostIdx = post.postIdx
                    }
                }
                completion(lastPostIdx)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("getLastPostIdx",error.toString())
            }
        })
    }

    private fun setPost(post: Post) {  // postIdx 1씩 증가
        getLastPostIdx { lastPostIdx ->
            val newPostIdx = lastPostIdx + 1
            post.postIdx = newPostIdx
            postDB.child(newPostIdx.toString()).setValue(post)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addPost(){

        val title=binding.writingTitleEt.toString()
        val content=binding.writingContentEt.toString()
        val userUid=MyApplication.prefs.getUid("uid","")
        val userJson= MyApplication.prefs.getUser("user","")

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val currentTime = LocalDateTime.now().format(formatter)

        user = gson.fromJson(userJson, User::class.java)

        val addpost = Post(0, userUid, user.nickname, currentTime, 0, title, content)
        setPost(addpost)
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