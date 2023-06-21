package com.example.community.ui.home


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.community.data.entity.Comment
import com.example.community.data.viewModel.ReplyViewModel
import com.example.community.databinding.ItemCommentBinding


class CommentAdapter(
    private val userUid: String,
    val context: Context,
    val replyViewModel: ReplyViewModel,
    val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private val items = arrayListOf<Comment>()


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(comment: List<Comment>) {
        this.items.clear()
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

            binding.comment = comment

            val adapter = ReplyRVAdapter(userUid)
            binding.replyRv.adapter = adapter
            binding.replyRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            replyViewModel.getReply(comment.commentIdx).observe(lifecycleOwner) {
                adapter.addReplies(it)  // 대댓글 가져오기
            }

            adapter.setItemClickListener(object : ReplyRVAdapter.DeleteInterface {
                override fun onDeleteClicked(replyIdx: Int) {
                    replyViewModel.deleteReply(replyIdx)  // 대댓글 삭제
                    deleteReplyState()
                }
            })
        }
    }

    private fun deleteReplyState() {
        replyViewModel.deleteReplyState.observe(lifecycleOwner) { state ->
            when (state) {
                true -> {
                    Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("deleteReply", "failed")

            }
        }
    }
}
