package com.example.community.ui.home


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.community.data.entity.Post
import com.example.community.databinding.ItemHomeContentsBinding


class ContentRVAdpater(
    private val context: Context
) :
    RecyclerView.Adapter<ContentRVAdpater.ViewHolder>(), Filterable {

    private val items = ArrayList<Post>()  // 원본

    var filterItem = ArrayList<Post>()  // 검색결과
    var setFilter = ItemFilter()

    init {
        filterItem.addAll(items)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun locationList(post: List<Post>) {
        items.clear()
        items.addAll(post)
        filterItem.clear()
        filterItem.addAll(post)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun ageList(post: List<Post>) {
        filterItem.clear()
        filterItem.addAll(post)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getMyList(post: List<Post>) {
        filterItem.clear()
        filterItem.addAll(post)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getReplyList(post:Post){
        filterItem.add(post)
        notifyDataSetChanged()
    }

    interface InContentInterface {
        fun onContentClicked(post: Post)
    }

    override fun getFilter(): Filter {
        return setFilter
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
            bind(filterItem[position])

            binding.layoutClick.setOnClickListener {  // 게시물 클릭시 조회수 증가
                val views = filterItem[position].view + 1
                val update = filterItem[position].copy(view = views)
                itemClickListener.onContentClicked(update)
            }

            if (filterItem[position].imgs != null) {
                img.visibility = View.VISIBLE

                filterItem[position].imgs?.get(0)?.let { displayImage(it, binding.imageExIv) }
            }
        }
    }

    override fun getItemCount(): Int = filterItem.size

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


    inner class ItemFilter : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterString = charSequence.toString()
            val results = FilterResults()
            Log.d(TAG, "charSequence : $charSequence")


            val filteredList: ArrayList<Post> = ArrayList<Post>()  // 원본 복제
            //공백제외 아무런 값이 없을 경우 -> 원본 배열
            if (filterString.trim { it <= ' ' }.isEmpty()) {
                results.values = items
                results.count = items.size

                return results
                //공백제외 2글자 이하인 경우 -> 제목으로만 검색
            } else if (filterString.trim { it <= ' ' }.length <= 2) {
                for (post in items) {
                    if (post.title.contains(filterString)) {
                        filteredList.add(post)
                    }
                }
                //그 외의 경우(공백제외 2글자 초과) -> 제목, 내용으로 검색
            } else {
                for (post in items) {
                    if (post.title.contains(filterString) || post.content.contains(filterString)) {
                        filteredList.add(post)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size

            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(char: CharSequence?, result: FilterResults?) {

            filterItem.clear()
            filterItem.addAll(result?.values as ArrayList<Post>)

            if (filterItem.isEmpty()) {
                Toast.makeText(context, "검색결과가 없어요!", Toast.LENGTH_SHORT).show()
            }
            notifyDataSetChanged()
        }
    }
}
