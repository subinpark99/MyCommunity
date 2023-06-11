package com.example.community.ui.mypage

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.community.R
import com.example.community.databinding.DialogOcrBinding
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.*


class DialogOcr :
    DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogOcrBinding

    private var image: Bitmap? = null  // 사용되는 이미지
    private lateinit var loadImg: Uri
    private var mTess: TessBaseAPI? = null // Tess API reference
    private var datapath = ""  // 언어 데이터가 있는 경로
    private lateinit var ocrTv: TextView // OCR 결과뷰
    private lateinit var getResult: ActivityResultLauncher<Intent>


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogOcrBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  //배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  //dialog 모서리 둥글게

        binding.getPhotoAlbumIv.setOnClickListener {// 앨범 이미지 가져오기 클릭리스너
            initAddPhoto()
        }

        binding.doneIv.setOnClickListener {  // 제출하기

            if (image == null) {
                Toast.makeText(requireContext(), "이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
            } else {

                val argument = Bundle()
                argument.putString("ocr", ocrTv.text.toString())
                findNavController().navigate(
                    R.id.action_dialogOcr_to_dialogChangeLocation,
                    argument
                )

            }
        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val intent = checkNotNull(it.data)

                loadImg = intent.data!!

                Glide.with(this) // 화면에 이미지 불러오기
                    .load(loadImg)
                    .into(binding.ocrImgIv)

                getLocationText()  // 문자 인식
            }
        }

        return binding.root
    }


    private fun initAddPhoto() {
        val writePermission = ContextCompat.checkSelfPermission( // 권한 받아 오기
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        } else {
            val state = Environment.getExternalStorageState()
            if (TextUtils.equals(state, Environment.MEDIA_MOUNTED)) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                getResult.launch(intent)  // 갤러리에서 사진 가져오기
            }

        }
    }

    private fun aRGBBitmap(img: Bitmap): Bitmap? { // ARGB_8888 양식 비트맵
        return img.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun loadBitmapFromMediaStoreBy(photoUri: Uri): Bitmap? { // uri to bitmap
        var image: Bitmap? = null
        try {
            image = if (Build.VERSION.SDK_INT > 27) {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(requireContext().contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)

            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getLocationText() {

        image = loadBitmapFromMediaStoreBy(loadImg)?.let { aRGBBitmap(it) }

        datapath = "${context?.filesDir}/tesseract/"

        checkFile(File(datapath + "tessdata/"))
        val lang = "kor"

        //OCR 세팅
        mTess = TessBaseAPI()
        mTess!!.init(datapath, lang)

        ocrTv = binding.getLocationTv
        processImage()

    }


    /** 이미지에서 텍스트 읽기 **/
    private fun processImage() {
        var ocrRst: String? = null
        mTess!!.setImage(image)
        ocrRst = mTess!!.utF8Text

        val test = ocrRst.split("\n")  // 한 문장씩 출력
        ocrTv.text = test[3]
    }

    /** 언어 데이터 파일, 디바이스에 복사 **/
    private val langFileName = "kor.traineddata"
    private fun copyFiles() {
        try {
            val filepath = datapath + "tessdata/" + langFileName
            val assetManager = activity?.assets
            val instream: InputStream? = assetManager?.open(langFileName)
            val outstream: OutputStream = FileOutputStream(filepath)
            val buffer = ByteArray(1024)
            var read: Int
            if (instream != null) {
                while (instream.read(buffer).also { read = it } != -1) {
                    outstream.write(buffer, 0, read)
                }
            }
            outstream.flush()
            outstream.close()
            instream?.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /** 디바이스에 언어 데이터 파일 존재 유무 체크 **/
    private fun checkFile(dir: File) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles()
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if (dir.exists()) {
            val datafilepath = datapath + "tessdata/" + langFileName
            val datafile = File(datafilepath)
            if (!datafile.exists()) {
                copyFiles()
            }
        }
    }

    override fun onClick(p0: View?) {
        dismiss()
    }

}