package com.example.community.ui.home


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.community.data.entity.Post
import com.example.community.databinding.ItemHomeContentsBinding


class ContentRVAdpater(
    private val context: Context
) :
    RecyclerView.Adapter<ContentRVAdpater.ViewHolder>() {

    private val items = ArrayList<Post>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(post: Post) {
        this.items.add(post)
        notifyDataSetChanged()
    }

    interface InContentInterface {
        fun onContentClicked(post: Post)
    }

    private lateinit var itemClickListener: InContentInterface
    fun setItemClickListener(myItemClickListener: InContentInterface) {
        itemClickListener = myItemClickListener
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

        holder.apply {
            bind(items[position])

            binding.layoutClick.setOnClickListener {  // 게시물 클릭시 조회수 증가
                val views = items[position].view + 1
                val update = items[position].copy(view = views)
                itemClickListener.onContentClicked(update)
            }

            if (items[position].imgs != null) {
                img.visibility = View.VISIBLE

                items[position].imgs?.get(0)?.let { displayImage(it, binding.imageExIv) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemHomeContentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imageExIv
        fun bind(post: Post) {

            binding.contentTimeTv.text = post.date
            binding.homeContentTv.text = post.content
            binding.homeTitleTv.text = post.title
            binding.userNicknameTv.text = post.nickname
            binding.contentViewsTv.text = post.view.toString()

        }
    }

    private fun decodeImage(item: String): Bitmap {  // Base64 -> bitmap
        val decodedBytes = Base64.decode(item, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    private fun displayImage(item: String, imageView: ImageView) {
        val decodedImage = decodeImage(item)

        Glide.with(context)
            .load(decodedImage)
            .into(imageView)
    }
}
