package com.example.community.ui.home


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.community.data.entity.Comment
import com.example.community.databinding.ItemCommentBinding


class CommentAdapter(private val userUid: String) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private val items = arrayListOf<Comment>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(comment: List<Comment>) {
        this.items.clear() // 기존 댓글 삭제
        this.items.addAll(comment)
        notifyDataSetChanged()
    }

    interface DeleteInterface {
        fun onDeleteClicked(commentIdx: Int)
    }

    private lateinit var itemClickListener: DeleteInterface
    fun setItemClickListener(myItemClickListener: DeleteInterface) {
        itemClickListener = myItemClickListener
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

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.contentTimeTv.text = comment.date
            binding.contentTv.text = comment.content
            binding.userNicknameTv.text = comment.nickname
        }
    }
}


