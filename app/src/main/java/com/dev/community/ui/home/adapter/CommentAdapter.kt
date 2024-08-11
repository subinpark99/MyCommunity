package com.dev.community.ui.home.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.community.data.model.Comment
import com.dev.community.databinding.ItemCommentBinding
import com.dev.community.databinding.ItemReplyBinding

class CommentAdapter(
    private val userUid: String,
    private val deleteClickListener: (String, String) -> Unit,
    private val replyClickListener: (Comment) -> Unit,
    private val comments: List<Comment>
) : ListAdapter<Comment, RecyclerView.ViewHolder>(CommentDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_COMMENT = 0
        private const val VIEW_TYPE_REPLY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).parentId.isEmpty()) VIEW_TYPE_COMMENT else VIEW_TYPE_REPLY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_COMMENT) {
            CommentViewHolder.from(parent)
        } else {
            ReplyViewHolder.from(parent, comments)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val comment = getItem(position)
        when (holder) {
            is CommentViewHolder -> {
                holder.bind(
                    comment,
                    userUid,
                    deleteClickListener,
                    replyClickListener
                )
            }

            is ReplyViewHolder -> holder.bind(
                comment,
                userUid,
                deleteClickListener,
                replyClickListener
            )
        }
    }

    class CommentViewHolder private constructor(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(
            comment: Comment,
            userUid: String,
            deleteClickListener: (String, String) -> Unit,
            replyClickListener: (Comment) -> Unit
        ) {
            binding.comment = comment
            binding.executePendingBindings()

            binding.apply {
                if (comment.content.isEmpty()) {
                    contentTv.text = "삭제된 댓글입니다."
                    contentTimeTv.visibility = View.GONE
                    userNicknameTv.visibility = View.GONE
                    addReplyIv.visibility = View.GONE
                    deleteCommentTv.visibility = View.GONE
                } else {

                    // 댓글 삭제 버튼 클릭 리스너 설정
                    if (userUid == comment.uid) {
                        deleteCommentTv.visibility = View.VISIBLE
                        deleteCommentTv.setOnClickListener {
                            deleteClickListener(comment.commentId, comment.parentId)
                        }
                    } else {
                        deleteCommentTv.visibility = View.GONE
                    }

                    // 답글 추가 버튼 클릭 리스너 설정
                    addReplyIv.setOnClickListener {
                        replyClickListener(comment)
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): CommentViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCommentBinding.inflate(layoutInflater, parent, false)
                return CommentViewHolder(binding)
            }
        }
    }

    class ReplyViewHolder private constructor(
        val binding: ItemReplyBinding,
        private val comments: List<Comment>
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(
            comment: Comment,
            userUid: String,
            deleteClickListener: (String, String) -> Unit,
            replyClickListener: (Comment) -> Unit
        ) {
            binding.reply = comment
            binding.executePendingBindings()

            binding.apply {
                if (comment.content.isEmpty()) {

                    contentTv.text = "삭제된 댓글입니다."
                    replyToTv.visibility = View.GONE
                    contentTimeTv.visibility = View.GONE
                    userNicknameTv.visibility = View.GONE
                    subArrowIcon.visibility = View.GONE
                    addReplyIv.visibility = View.GONE
                    deleteReplyTv.visibility = View.GONE
                } else {
                    if (userUid == comment.uid) {
                        deleteReplyTv.visibility = View.VISIBLE
                        deleteReplyTv.setOnClickListener {
                            deleteClickListener(comment.commentId, comment.parentId)
                        }
                    } else deleteReplyTv.visibility = View.GONE

                    addReplyIv.setOnClickListener {
                        replyClickListener(comment)
                    }

                    val parentNickname =
                        comments.find { it.commentId == comment.parentId }?.nickname ?: "알 수 없음"
                    replyToTv.text = "$parentNickname 에게" // 부모 닉네임 표시

                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, comments: List<Comment>): ReplyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemReplyBinding.inflate(layoutInflater, parent, false)
                return ReplyViewHolder(binding, comments)
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}
