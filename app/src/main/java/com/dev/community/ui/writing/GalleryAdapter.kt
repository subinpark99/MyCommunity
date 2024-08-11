package com.dev.community.ui.writing

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.community.databinding.ItemPhotoBinding


class GalleryAdapter(
    private val context: Context,
    private val imageClickListener: (String) -> Unit
) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val imgs = ArrayList<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(items: List<String>) {
        imgs.clear()
        imgs.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPhotoBinding =
            ItemPhotoBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val uri = imgs[position]
        displayImage(uri, holder.imageUrl)

        holder.imageUrl.setOnClickListener {
            imageClickListener(uri)
        }
    }

    override fun getItemCount(): Int = imgs.size

    inner class ViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageUrl = binding.writeImgIv
    }


    private fun displayImage(item: String, imageView: ImageView) {
        Glide.with(context)
            .load(item)
            .into(imageView)
    }
}
