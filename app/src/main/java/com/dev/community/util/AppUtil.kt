package com.dev.community.util


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dev.community.ui.other.LoadingDialog
import com.google.android.material.snackbar.Snackbar

object AppUtils {
    private var loadingDialog: LoadingDialog? = null

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.window.decorView.applicationWindowToken, 0)
    }

    // 권한 요청 및 이미지 선택
    fun getPermission(
        context: Context,
        getImage: ActivityResultLauncher<Intent>,
        activity: Activity
    ) {
        if (isPermissionGranted(context)) {
            openImagePicker(getImage)
        } else {
            requestPermissions(activity)
        }
    }

    // 외부 저장소 접근 권한 확인
    private fun isPermissionGranted(context: Context): Boolean {
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return writePermission == PackageManager.PERMISSION_GRANTED &&
                readPermission == PackageManager.PERMISSION_GRANTED
    }

    // 외부 저장소 접근 권한 요청
    private fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            200
        )
    }

    // 갤러리 열기
    @SuppressLint("IntentReset")
    private fun openImagePicker(getImage: ActivityResultLauncher<Intent>) {

        val intent = Intent(Intent.ACTION_PICK).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        intent.type = "image/*"
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)   //다중 이미지 가져오기


        getImage.launch(intent)
    }


    fun hideStatusBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.insetsController
            insetsController?.let {
                it.hide(WindowInsets.Type.statusBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API 30 미만
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }


    fun showLoadingDialog(context: Context) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(context)
            loadingDialog?.show()
        }
    }

    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}