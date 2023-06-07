package com.example.community.ui.writing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.community.data.MyApplication
import com.example.community.data.entity.Post
import com.example.community.data.entity.User
import com.example.community.databinding.FragmentWritingBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WritingFragment: Fragment() {
    private var _binding: FragmentWritingBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: User
    private val gson : Gson = Gson()
    private val postDB=Firebase.database.getReference("post")

    private var imgList= arrayListOf<String>()  // 이미지 리스트 가져오기

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWritingBinding.inflate(inflater, container, false)

        val userJson=MyApplication.prefs.getUser("user","")
        user=gson.fromJson(userJson,User::class.java)

        binding.currentLocationTv.text=user.location

        binding.addPhotoIv.setOnClickListener {
            getPermission()
        }

        binding.writeDoneIv.setOnClickListener {  // 작성 완료
            addPost()
        }

        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun addPost() {

        val title = binding.writingTitleEt.text.toString()
        val content = binding.writingContentEt.text.toString()

        if (title.isEmpty() or content.isEmpty()) {
            Toast.makeText(requireContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            val userUid = MyApplication.prefs.getUid("uid", "")
            val userJson = MyApplication.prefs.getUser("user", "")

            val formatter = DateTimeFormatter.ofPattern("MM/dd")
            val currentTime = LocalDateTime.now().format(formatter)

            user = gson.fromJson(userJson, User::class.java)

            val addpost = Post(0, userUid, user.nickname, currentTime, 0, title, content, imgList)
            setPost(addpost)
            Toast.makeText(requireContext(),"완료",Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLastPostIdx(completion: (Int) -> Unit) {  // 마지막 postIdx 값 불러옴
        postDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lastPostIdx = 0
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null && post.postIdx > lastPostIdx) {
                        lastPostIdx = post.postIdx
                    }
                }
                completion(lastPostIdx)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("getLastPostIdx",error.toString())
            }
        })
    }

    private fun setPost(post: Post) {  // postIdx 1씩 증가
        getLastPostIdx { lastPostIdx ->
            val newPostIdx = lastPostIdx + 1
            post.postIdx = newPostIdx
            postDB.child(newPostIdx.toString()).setValue(post)
        }
    }

    @SuppressLint("IntentReset")
    private fun getPermission(){
        val writePermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한 없어서 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),200)
        } else {
            // 권한 있음
            val intent = Intent()
            intent.type = "image/*"
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   // 다중 이미지 가져오기
            intent.action = Intent.ACTION_GET_CONTENT

            getImage.launch(intent)
        }
    }

    private val getImage=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result->

        if ( result.resultCode == Activity.RESULT_OK) {

            if (result.data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = result.data?.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = result.data?.clipData!!.getItemAt(i).uri
                        val imagePath=getImagePathFromUri(imageUri)
                        val encoded=encodeImage(imagePath)
                        imgList.add(encoded)
                    }
            }
        } else { // 단일 선택
            result.data?.data?.let {
                val imageUri : Uri? = result.data!!.data
                if (imageUri != null) {
                    imgList.add(encodeImage(imageUri.toString()))
                }
            }
        }
        onRecyclerView()
    }

    private fun onRecyclerView() {

        val galleryRVAdapter=GalleryAdapter(requireContext())
        galleryRVAdapter.submitList(imgList)
        binding.writeGalleryRv.adapter = galleryRVAdapter
        binding.writeGalleryRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun encodeImage(imagePath: String): String {  // 이미지 파일 변환 (realtime database에 이미지 저장)
        val imageBytes = getBytesFromImagePath(imagePath)
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun getBytesFromImagePath(imagePath: String): ByteArray { // 이미지 파일을 바이트 배열로 변환하는 함수
        val file = File(imagePath)
        val inputStream = FileInputStream(file)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int

        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        inputStream.close()
        outputStream.close()

        return outputStream.toByteArray()
    }

    @SuppressLint("Range")
    private fun getImagePathFromUri(uri: Uri): String {    // 이미지 URI로부터 실제 경로 가져오기
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val imagePath = cursor?.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
        cursor?.close()
        return imagePath ?: ""
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}