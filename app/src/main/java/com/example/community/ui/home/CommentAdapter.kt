package com.example.community.ui.home


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.community.data.entity.Comment
import com.example.community.data.entity.Reply
import com.example.community.databinding.ItemCommentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


class CommentAdapter(
    private val userUid: String,
    private val replyDB:DatabaseReference,
    val context: Context
) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private val items = arrayListOf<Comment>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(comment: List<Comment>) {
        this.items.clear() // 기존 댓글 삭제
        this.items.addAll(comment)
        notifyDataSetChanged()
    }

    interface DeleteInterface {  // 댓글 삭제
        fun onDeleteClicked(commentIdx: Int)
    }

    private lateinit var itemClickListener: DeleteInterface
    fun setItemClickListener(myItemClickListener: DeleteInterface) {
        itemClickListener = myItemClickListener
    }

    interface ReplyInterface { // 대댓글 아이콘 클릭시 대댓글 작성
        fun onReplyClicked(commentIdx: Int)
    }

    private lateinit var itemsClickListener: ReplyInterface
    fun setItemClickListener(myItemClickListener: ReplyInterface) {
        itemsClickListener = myItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCommentBinding =
            ItemCommentBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.apply {
            bind(items[position])
        }
        if (userUid == items[position].uid) {
            holder.binding.deleteCommentTv.visibility = View.VISIBLE
            holder.binding.deleteCommentTv.setOnClickListener {
                itemClickListener.onDeleteClicked(items[position].commentIdx)
            }
        }

        holder.binding.addReplyIv.setOnClickListener {
            itemsClickListener.onReplyClicked(items[position].commentIdx)

        }

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.contentTimeTv.text = comment.date
            binding.contentTv.text = comment.content
            binding.userNicknameTv.text = comment.nickname

            val adapter=ReplyRVAdapter(userUid)
            binding.replyRv.adapter = adapter
            binding.replyRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


            replyDB.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reply = ArrayList<Reply>() // 새로운 댓글
                    if (snapshot.exists()) {
                        for (cmSnapShot in snapshot.children) {
                            val data = cmSnapShot.getValue(Reply::class.java)

                            if (data != null && data.commentIdx == comment.commentIdx) {
                                reply.add(data)
                            }
                        }
                    }
                    adapter.addReplies(reply)  // 댓글 전체 update
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("getCommentFail", error.toString())
                }
            })
        }
    }
}