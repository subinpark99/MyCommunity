package com.example.community.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.community.databinding.ItemPhotoBinding

class ImageAdapter (
    private val context: Context,
    private var imgs:List<String>
):
    RecyclerView.Adapter<ImageAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPhotoBinding = ItemPhotoBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val uri= imgs[position]
        displayImage(uri, holder.imageUrl)
    }

    override fun getItemCount(): Int = imgs.size

    inner class ViewHolder(val binding: ItemPhotoBinding): RecyclerView.ViewHolder(binding.root){
        val imageUrl=binding.writeImgIv
    }


    fun decodeImage(item: String): Bitmap {  // Base64 -> bitmap
        val decodedBytes = Base64.decode(item, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    fun displayImage(item: String, imageView: ImageView) {
        val decodedImage = decodeImage(item)

        Glide.with(context)
            .load(decodedImage)
            .into(imageView)
    }
}
