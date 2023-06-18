package com.example.community.ui.notice


import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.local.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.viewModel.CommentViewModel
import com.example.community.data.viewModel.PostViewModel
import com.example.community.data.viewModel.ReplyViewModel
import com.example.community.databinding.FragmentNoticeBinding
import com.example.community.ui.notice.fcm.RetrofitInstance
import com.example.community.ui.notice.fcm.model.NotificationData
import com.example.community.ui.notice.fcm.model.PushNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val replyViewModel: ReplyViewModel by viewModels()

    private lateinit var userUid: String
    private lateinit var noticeAdapter: NoticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNoticeBinding.inflate(inflater, container, false)

        userUid = MyApplication.prefs.getUid("uid", "")
        noticeAdapter = NoticeAdapter()

        return binding.root
    }


    private fun getMyPosts() {

        binding.noticeRv.apply {
            adapter = noticeAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        postViewModel.getMyPosts(userUid).observe(this) { // 내가 쓴 게시물 가져오기
            if (it != null)
                getMyPostState(it.postIdx)
        }

    }

    private fun getMyPostState(postIdx: Int) {
        postViewModel.getMyPostState.observe(this) { state ->
            when (state) {
                true -> { // 내가 쓴 게시물의 댓글, 대댓글 가져오기
                    getCommentNotice(postIdx)
                    getReplyNotice(postIdx)
                }
                else -> Log.d("getmypost", "failed")
            }
        }
    }


    private fun getCommentNotice(getMyPostIdx: Int) {

        commentViewModel.getNoticeComment(getMyPostIdx, userUid).observe(this) {
            if (it != null) noticeAdapter.commentList(it)
        }
    }

    private fun getReplyNotice(getMyPostIdx: Int) {
        replyViewModel.getNoticeReply(getMyPostIdx, userUid).observe(this) {
            if (it != null) noticeAdapter.replyList(it)
        }
    }

    private fun getSwitch() {
//        userDB.child(userUid).child("alarm")
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val alarmEnabled = snapshot.getValue(Boolean::class.java)
//                    if (alarmEnabled == true) {
//                        sendPush()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.d("s", "sdfsdfsdf")
//                }
//            })
    }

    private fun getInContent() {  // 댓글 알림 클릭 시, commentDB와 replyDB의 postIdx에 해당하는 post로 이동

        noticeAdapter.setItemClickListener(object : NoticeAdapter.NoticeInterface {
            override fun onCommentClicked(postIdx: Int) {  // 여기서 postIdx는 commentDB의 postIdx

                postViewModel.getNoticePost(postIdx).observe(this@NoticeFragment) {
                    onPostClicked(postIdx)
                    if (it != null) {
                        getNoticePostState(it)
                    }
                }
            }

            override fun onReplyClicked(postIdx: Int) { // 여기서 postIdx는 replyDB의 postIdx
                postViewModel.getNoticePost(postIdx).observe(this@NoticeFragment) {
                    onPostClicked(postIdx)
                    if (it != null) {
                        getNoticePostState(it)
                    }
                }
            }
        })
    }

    private fun getNoticePostState(post: Post) {
        postViewModel.getNoticePostState.observe(this) { state ->
            when (state) {
                true -> {

                    val arguments =
                        NoticeFragmentDirections.actionNoticeFragmentToInContentFragment(
                            post
                        )
                    findNavController().navigate(arguments) // 해당 게시글로 이동
                }
                else -> Log.d("getnoticepost", "failed")
            }
        }
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
        postViewModel.updatePostCnt(postIdx)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        getMyPosts()
        getInContent()
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
