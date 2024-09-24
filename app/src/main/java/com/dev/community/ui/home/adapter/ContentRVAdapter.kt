package com.dev.community.ui.home.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.community.data.model.Post
import com.dev.community.databinding.ItemHomeContentsBinding

class ContentRVAdapter(
    private val contentClickListener: (String) -> Unit
) : RecyclerView.Adapter<ContentRVAdapter.ViewHolder>(), Filterable {

    private val items = ArrayList<Post>()  // 원본
    var filterItem = ArrayList<Post>()  // 검색결과
    private var setFilter = ItemFilter()

    @SuppressLint("NotifyDataSetChanged")
    fun getList(posts: List<Post>) {
        items.clear()
        items.addAll(posts)
        filterItem.clear()
        filterItem.addAll(posts)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return setFilter
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemHomeContentsBinding = ItemHomeContentsBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = filterItem[position]

        holder.apply {
            bind(post)

            binding.layoutClick.setOnClickListener {  // 게시물 클릭 시 조회수 증가
                contentClickListener(post.postId)
            }

            val context = binding.root.context

            if (post.imageList.isNotEmpty()) {
                binding.imageExIv.visibility = View.VISIBLE
                Glide.with(context)
                    .load(post.imageList[0])
                    .into(binding.imageExIv)
            } else {
                binding.imageExIv.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = filterItem.size

    inner class ViewHolder(val binding: ItemHomeContentsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.post = post
        }
    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            val filteredList = if (charSequence.isNullOrEmpty()) {
                items
            } else {
                val filterPattern = charSequence.toString().lowercase().trim()
                items.filter {
                    it.title.lowercase()
                        .contains(filterPattern) || it.content.lowercase()
                        .contains(filterPattern)
                } as ArrayList<Post>
            }

            return FilterResults().apply { values = filteredList }
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
            filterItem.clear()

            if (filterResults?.values is List<*>) {
                filterItem.addAll(filterResults.values as List<Post>)
            }
            notifyDataSetChanged()
        }
    }
}
