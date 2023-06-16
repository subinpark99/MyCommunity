package com.example.community.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.community.data.entity.Reply
import com.example.community.databinding.ItemReplyBinding

class ReplyRVAdapter(private val userUid: String) :
    RecyclerView.Adapter<ReplyRVAdapter.ViewHolder>() {

    private val items = arrayListOf<Reply>()

    @SuppressLint("NotifyDataSetChanged")
    fun addReplies(reply: List<Reply>) {
        this.items.clear() // 기존 댓글 삭제
        this.items.addAll(reply)
        notifyDataSetChanged()
    }

    interface DeleteInterface {
        fun onDeleteClicked(replyIdx: Int)
    }

    private lateinit var itemClickListener: DeleteInterface
    fun setItemClickListener(myItemClickListener: DeleteInterface) {
        itemClickListener = myItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemReplyBinding =
            ItemReplyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.apply {
            bind(items[position])
        }
        if (userUid == items[position].uid) {
            holder.binding.deleteReplyTv.visibility = View.VISIBLE
            holder.binding.deleteReplyTv.setOnClickListener {
                itemClickListener.onDeleteClicked(items[position].commentIdx)
            }
        }

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reply:Reply) {
            binding.contentTimeTv.text = reply.date
            binding.contentTv.text = reply.content
            binding.userNicknameTv.text = reply.nickname
        }
    }
}