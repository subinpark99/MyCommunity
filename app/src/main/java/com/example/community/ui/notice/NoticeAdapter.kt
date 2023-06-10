package com.example.community.ui.notice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.community.data.entity.Comment
import com.example.community.databinding.ItemNoticeBinding

class NoticeAdapter:
    RecyclerView.Adapter<NoticeAdapter.ViewHolder>(){

    private val items = arrayListOf<Comment>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(comment: List<Comment>) {
        this.items.clear() // 기존 댓글 삭제
        this.items.addAll(comment)
        notifyDataSetChanged()
    }

    interface NoticeInterface {
        fun onCommentClicked(postIdx:Int) // commentDB의 postIdx
    }
    private lateinit var itemClickListener: NoticeInterface
    fun setItemClickListener(myItemClickListener: NoticeInterface) {
        itemClickListener = myItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemNoticeBinding = ItemNoticeBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.apply {
            bind(items[position])
            binding.layoutClick.setOnClickListener {  // 알림 클릭시 게시물로 이동
                itemClickListener.onCommentClicked(items[position].postIdx)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemNoticeBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(comment: Comment){

            binding.noticeContentTv.text=comment.content
            binding.noticeDateTv.text=comment.date
            binding.noticeTimeTv.text=comment.time

        }
    }
}
