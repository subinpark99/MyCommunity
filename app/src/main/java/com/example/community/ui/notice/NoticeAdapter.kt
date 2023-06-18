package com.example.community.ui.notice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.community.data.entity.Comment
import com.example.community.data.entity.Reply
import com.example.community.databinding.ItemNoticeBinding


class NoticeAdapter :
    RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    private val commentItems = arrayListOf<Comment>()
    private val replyItems = arrayListOf<Reply>()

    @SuppressLint("NotifyDataSetChanged")
    fun commentList(comment: List<Comment>) {
        this.commentItems.clear()
        this.commentItems.addAll(comment)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun replyList(reply: List<Reply>) {
        this.replyItems.clear()
        this.replyItems.addAll(reply)
        notifyDataSetChanged()
    }


    interface NoticeInterface {
        fun onCommentClicked(postIdx: Int) // commentDB의 postIdx
        fun onReplyClicked(postIdx: Int) // replyDB의 postIdx
    }

    private lateinit var itemClickListener: NoticeInterface
    fun setItemClickListener(myItemClickListener: NoticeInterface) {
        itemClickListener = myItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemNoticeBinding =
            ItemNoticeBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position < commentItems.size) {
            holder.bind(commentItems[position])
        } else {
            val replyPosition = position - commentItems.size
            if (replyPosition < replyItems.size) {
                holder.bind(replyItems[replyPosition])
            }
        }
    }

    override fun getItemCount(): Int = commentItems.size + replyItems.size

    inner class ViewHolder(val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {

            binding.noticeContentTv.text = comment.content
            binding.noticeDateTv.text = comment.date
            binding.noticeTimeTv.text = comment.time

            binding.layoutClick.setOnClickListener {  // 알림 클릭시 게시물로 이동
                itemClickListener.onCommentClicked(comment.postIdx)
            }
        }

        fun bind(reply: Reply) {

            binding.noticeContentTv.text = reply.content
            binding.noticeDateTv.text = reply.date
            binding.noticeTimeTv.text = reply.time

            binding.layoutClick.setOnClickListener {  // 알림 클릭시 게시물로 이동
                itemClickListener.onReplyClicked(reply.postIdx)
            }
        }
    }
}
