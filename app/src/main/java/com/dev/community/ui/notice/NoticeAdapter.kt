package com.dev.community.ui.notice

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.community.data.model.Comment
import com.dev.community.databinding.ItemNoticeBinding


class NoticeAdapter(
    private val commentClickListener: (String) -> Unit,
) :
    RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    private var commentItems = mutableListOf<Comment>()

    @SuppressLint("NotifyDataSetChanged")
    fun addComment(comment: List<Comment>) {
        commentItems.clear()
        commentItems.addAll(comment)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemNoticeBinding =
            ItemNoticeBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(commentItems[position])
    }

    override fun getItemCount(): Int = commentItems.size

    inner class ViewHolder(val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {

            binding.comment = comment

            binding.layoutClick.setOnClickListener {  // 알림 클릭시 게시물로 이동
                commentClickListener(comment.postId)
            }
        }

    }
}
