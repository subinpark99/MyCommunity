package com.dev.community.ui.other

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.dev.community.util.AppUtils
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.ActivityOcrBinding
import com.dev.community.ui.start.MainActivity
import com.dev.community.ui.start.SignUpActivity
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.*

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.R)
class OcrActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOcrBinding
    private lateinit var ocrTv: TextView // OCR 결과뷰

    private val userViewModel: UserViewModel by viewModels()

    private var image: Bitmap? = null  // 사용되는 이미지
    private var datapath = ""  // 언어 데이터가 있는 경로
    private val langFileName = "kor.traineddata"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        datapath = "$filesDir/tesseract/"
        ocrTv = binding.getLocationTv

        checkAndCopyLanguageFiles() // 언어 파일 존재 여부 확인 및 복사
        setupListeners() // 버튼 클릭 리스너 설정

        AppUtils.hideStatusBar(window)
    }

    // 언어 데이터 파일 존재 여부 확인 및 복사
    private fun checkAndCopyLanguageFiles() {
        val dir = File("$datapath/tessdata/")
        val languageFile = File(dir, langFileName)

        if (!languageFile.exists()) {
            dir.mkdirs() // 디렉토리가 존재하지 않으면 생성
            copyLanguageFiles(languageFile)
        }
    }

    // 언어 데이터 파일 복사
    private fun copyLanguageFiles(destinationFile: File) {
        try {
            assets.open(langFileName).use { instream ->
                FileOutputStream(destinationFile).use { outstream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (instream.read(buffer).also { bytesRead = it } != -1) {
                        outstream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
            val loadImg = intent.data!!
            displayImage(loadImg) // 선택한 이미지 표시
        }
    }


    private fun displayImage(photoUri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(photoUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) { }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?,
                ) {
                    image = resource
                    binding.ocrImgIv.setImageBitmap(image)

                    setupTesseract() // TessBaseAPI 설정
                }
            })
    }


    // TessBaseAPI 설정 및 이미지에서 텍스트 추출
    private fun setupTesseract() {
        TessBaseAPI().apply {
            init(datapath, "kor")
            setImage(image)

            val ocrResult = utF8Text.split("\n")
            ocrTv.text= ocrResult[3]
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
            userViewModel.changeLocationState.flowWithLifecycle(
                lifecycle,
                Lifecycle.State.STARTED
            )
                .collectLatest {
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