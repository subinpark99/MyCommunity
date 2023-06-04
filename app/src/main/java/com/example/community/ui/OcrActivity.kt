package com.example.community.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.community.databinding.ActivityOcrBinding
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.*


class OcrActivity: AppCompatActivity() {

    private lateinit var binding: ActivityOcrBinding

    private var image: Bitmap? = null  // 사용되는 이미지
    private lateinit var loadImg:Uri
    private var mTess: TessBaseAPI? = null // Tess API reference
    private var datapath = ""  // 언어 데이터가 있는 경로
    private lateinit var ocrTv: TextView // OCR 결과뷰
    private lateinit var getResult: ActivityResultLauncher<Intent>


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        binding.getPhotoAlbumIv.setOnClickListener {// 앨범 이미지 가져오기 클릭리스너
            initAddPhoto()
        }

        binding.doneIv.setOnClickListener {  // 제출하기

            if (image==null){
                Toast.makeText(this, "이미지를 선택해주세요",Toast.LENGTH_SHORT).show()
            }else {
                val intent = Intent(this, SignUpActivity::class.java)
                intent.putExtra("location",ocrTv.text.toString())
                startActivity(intent)
            }
        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val intent = checkNotNull(it.data)

                loadImg= intent.data!!

                Glide.with(this) // 화면에 이미지 불러오기
                    .load(loadImg)
                    .into(binding.ocrImgIv)

                getLocationText()  // 문자 인식
            }
        }
    }


    private fun initAddPhoto() {
        val writePermission = ContextCompat.checkSelfPermission( // 권한 받아 오기
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
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

    private fun loadBitmapFromMediaStoreBy(photoUri: Uri) : Bitmap?{ // uri to bitmap
        var image: Bitmap? = null
        try{
            image = if(Build.VERSION.SDK_INT > 27){
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(this.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)

            }else{
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            }
        }catch(e:IOException){
            e.printStackTrace()
        }
        return image
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getLocationText(){

        image= loadBitmapFromMediaStoreBy(loadImg)?.let { aRGBBitmap(it) }

        //언어파일 경로
        datapath = "$filesDir/tesseract/"

        //트레이닝데이터가 카피되어 있는지 체크
        checkFile(File(datapath + "tessdata/"))

        //Tesseract API 언어 세팅
        val lang = "kor"

        //OCR 세팅
        mTess = TessBaseAPI()
        mTess!!.init(datapath, lang)

        ocrTv=binding.getLocationTv
        processImage()

    }


    /** 이미지에서 텍스트 읽기 **/
    private fun processImage() {
        var ocrRst: String? = null
        mTess!!.setImage(image)
        ocrRst = mTess!!.utF8Text

        val test=ocrRst.split("\n")  // 한 문장씩 출력
        ocrTv.text = test[3]
    }

    /** 언어 데이터 파일, 디바이스에 복사 **/
    private val langFileName = "kor.traineddata"
    private fun copyFiles() {
        try {
            val filepath = datapath + "tessdata/" + langFileName
            val assetManager = assets
            val instream: InputStream = assetManager.open(langFileName)
            val outstream: OutputStream = FileOutputStream(filepath)
            val buffer = ByteArray(1024)
            var read: Int
            while (instream.read(buffer).also { read = it } != -1) {
                outstream.write(buffer, 0, read)
            }
            outstream.flush()
            outstream.close()
            instream.close()
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

    private fun init() {

        // 상태바 없애기
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

}