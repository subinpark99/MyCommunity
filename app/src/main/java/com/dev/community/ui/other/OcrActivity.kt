package com.dev.community.ui.other

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dev.community.util.AppUtils
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.ActivityOcrBinding
import com.dev.community.ui.start.MainActivity
import com.dev.community.ui.start.SignUpActivity
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.*

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.R)
class OcrActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOcrBinding

    private lateinit var loadImg: Uri
    private lateinit var ocrTv: TextView // OCR 결과뷰

    private var image: Bitmap? = null  // 사용되는 이미지
    private var mTess: TessBaseAPI? = null // Tess API reference
    private var datapath = ""  // 언어 데이터가 있는 경로
    private val langFileName = "kor.traineddata"

    private val userViewModel: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppUtils.hideStatusBar(window)
        setupListeners() // 버튼 클릭 리스너 설정
    }


    private fun setupListeners() {
        binding.getPhotoAlbumIv.setOnClickListener { AppUtils.getPermission(this, getImage, this) }
        binding.doneIv.setOnClickListener { submitOcrResult() }  // OCR 결과 제출
        binding.cancelButton.setOnClickListener { finish() }
    }

    // 이미지 선택 결과
    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        if (it.resultCode == Activity.RESULT_OK) {
            val intent = checkNotNull(it.data)
            loadImg = intent.data!!
            displayImage(loadImg) // 선택한 이미지 표시
            performOcr() // OCR 수행
        }
    }


    private fun displayImage(photoUri: Uri) {
        Glide.with(this)
            .load(photoUri)
            .into(binding.ocrImgIv)
    }

    // 이미지에서 텍스트 추출
    private fun performOcr() {
        image = loadBitmapFromUri(loadImg)?.let { aRGBBitmap(it) }
        datapath = "$filesDir/tesseract/"
        checkAndCopyLanguageFiles() // 언어 파일 존재 여부 확인 및 복사
        setupTesseract() // TessBaseAPI 설정
        processImage() // 이미지 처리 및 텍스트 추출
    }

    // URI를 비트맵으로 변환
    private fun loadBitmapFromUri(photoUri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT > 27) {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(this.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // 비트맵을 ARGB_8888 형식으로 변환
    private fun aRGBBitmap(img: Bitmap): Bitmap? {
        return img.copy(Bitmap.Config.ARGB_8888, true)
    }

    // TessBaseAPI 설정
    private fun setupTesseract() {
        mTess = TessBaseAPI().apply {
            init(datapath, "kor")
        }
        ocrTv = binding.getLocationTv
    }

    // 이미지에서 텍스트 추출 후 결과 표시
    private fun processImage() {
        mTess?.setImage(image)
        val ocrResult = mTess?.utF8Text ?: ""
        val resultLines = ocrResult.split("\n")
        if (resultLines.size > 3) {
            ocrTv.text = resultLines[3]
        }
    }

    //  언어 데이터 파일 존재 여부 확인 및 복사
    private fun checkAndCopyLanguageFiles() {
        val dir = File(datapath + "tessdata/")
        if (!dir.exists() && dir.mkdirs()) {
            copyLanguageFiles()
        } else if (dir.exists()) {
            val datafilepath = datapath + "tessdata/" + langFileName
            val datafile = File(datafilepath)
            if (!datafile.exists()) {
                copyLanguageFiles()
            }
        }
    }

    // 언어 데이터 파일 복사
    private fun copyLanguageFiles() {
        try {
            val filepath = datapath + "tessdata/" + langFileName
            assets.open(langFileName).use { instream ->
                FileOutputStream(filepath).use { outstream ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (instream.read(buffer).also { read = it } != -1) {
                        outstream.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun submitOcrResult() {
        if (image == null) {
            AppUtils.showToast(this, "이미지를 선택해주세요")
        } else {
            val page = intent.getStringExtra("page")
            val ocrText = ocrTv.text.toString()

            when (page) {
                "signup" -> navigateToSignUp(ocrText)
                "mypage" -> updateUserLocation(ocrText)
            }
        }
    }

    // 회원가입 페이지로 이동
    private fun navigateToSignUp(location: String) {

        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")
        val nickname = intent.getStringExtra("nickname")
        val age = intent.getStringExtra("age")

        val intent = Intent(this, SignUpActivity::class.java)
        intent.putExtra("location", location)
        intent.putExtra("email", email)
        intent.putExtra("password", password)
        intent.putExtra("nickname", nickname)
        intent.putExtra("age", age)
        startActivity(intent)
        finish()
    }


    private fun updateUserLocation(location: String) {
        userViewModel.changeLocation(location)

        lifecycleScope.launch {
            userViewModel.changeLocationState.collect {
                when (it) {
                    is Result.Success -> {
                        val intent = Intent(this@OcrActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                        AppUtils.showToast(this@OcrActivity, "${location}으로 변경되었습니다.")
                    }

                    is Result.Error -> Log.e("ERROR", "OcrActivity - ${it.message}")
                    is Result.Loading -> Log.e("Loading", "로딩중")

                }
            }
        }
    }

}