package com.example.community.ui.notice


import android.annotation.SuppressLint
import android.content.ContentValues
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
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentNoticeBinding
import com.example.community.ui.notice.fcm.RetrofitInstance
import com.example.community.ui.notice.fcm.model.NotificationData
import com.example.community.ui.notice.fcm.model.PushNotification
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val commentDB = Firebase.database.getReference("comment")
    private val postDB = Firebase.database.getReference("post")
    private val userDB = Firebase.database.getReference("user")
    private lateinit var userUid: String
    private lateinit var noticeAdapter: NoticeAdapter
    private lateinit var user: User
    private val gson: Gson = Gson()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNoticeBinding.inflate(inflater, container, false)

        userUid = MyApplication.prefs.getUid("uid", "")
        val userJson = MyApplication.prefs.getUser("user", "")
        user = gson.fromJson(userJson, User::class.java)
        noticeAdapter = NoticeAdapter()

        return binding.root
    }


    private fun getMyComments() {

        binding.noticeRv.apply {
            adapter = noticeAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        val postdb = postDB.orderByChild("uid").equalTo(userUid)   // 내가 쓴 글 가져오기
        postdb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (contentSnapshot in snapshot.children) {

                        val post = contentSnapshot.getValue(Post::class.java)

                        if (post != null) {
                            getCommentNotice(post.postIdx)  // 내가 쓴 글의 댓글 가져오기
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("getPost", error.toString())
            }
        })
    }


    private fun getCommentNotice(getCommentPost: Int) {

        commentDB.orderByChild("postIdx").equalTo(getCommentPost.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val commentList = mutableListOf<Comment>()
                        for (commentSnapshot in dataSnapshot.children.reversed()) {
                            val comment = commentSnapshot.getValue(Comment::class.java)
                            if (comment != null && comment.uid != userUid) {  // 내가 쓴 댓글 알림은 뜨지 않게
                                    commentList.add(comment)
                                    getSwitch()
                            }
                        }
                        noticeAdapter.submitList(commentList) // 새로운 데이터를 전달하여 목록 갱신

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("getPost", databaseError.toString())
                }
            })
    }

    private fun getSwitch(){
        userDB.child(userUid).child("alarm")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alarmEnabled = snapshot.getValue(Boolean::class.java)
                    if (alarmEnabled == true) {
                        sendPush()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("s", "sdfsdfsdf")
                }
            })
    }
    private fun getInxContent() {  // 댓글 알림 클릭 시, commentDB의 postIdx에 해당하는 post로 이동

        noticeAdapter.setItemClickListener(object : NoticeAdapter.NoticeInterface {
            override fun onCommentClicked(postIdx: Int) {  // 여기서 postIdx는 commentDB의 postIdx

                val postdb =
                    postDB.orderByChild("postIdx")
                        .equalTo(postIdx.toDouble())  // postDB의 idx가 commentDB의 idx와 같을 때
                postdb.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (contentSnapshot in snapshot.children.reversed()) {
                                val post = contentSnapshot.getValue(Post::class.java)
                                if (post != null) {
                                    onPostClicked(postIdx)
                                    val arguments =
                                        NoticeFragmentDirections.actionNoticeFragmentToInContentFragment(
                                            post
                                        )
                                    findNavController().navigate(arguments) // 해당 게시글로 이동
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("getCommentPost", error.toString())
                    }
                })
            }
        })
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    //  Log.d(TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e(ContentValues.TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, e.toString())
            }
        }


    private fun sendPush() {
        val userToken = MyApplication.prefs.getToken("token", "")
        val pushNotification = PushNotification(
            NotificationData("My Community !", ""),
            userToken
        )
        sendNotification(pushNotification)
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
        getInxContent()
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
