package com.example.community.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.community.databinding.ActivitySignupBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.*

class SignUpActivity:AppCompatActivity() {
    private lateinit var binding:ActivitySignupBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        //등록
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.submitBtn.setOnClickListener {  // 제출하기
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.currentLocationIv.setOnClickListener { // 현재 위치 가져오기
            binding.setLocationTv.text=""
            checkLocationPermission()
        }
    }

    //퍼미션 체크 및 권한 요청 함수
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkLocationPermission() {

        //주소 초기화
        var address: List<String> = listOf("부산광역시", "수영구", "대연동")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->

                    // 최근에 알려진 위치
                    if (location != null) {
                        Log.d("location","${location.latitude}${location.longitude}")

                        Geocoder(this, Locale.KOREA)
                            .getAddress(
                                location.latitude,
                                location.longitude
                            ) { add: android.location.Address? ->
                                if (add != null) {
                                   address=add.getAddressLine(0).split(" ")
                                    }
                                //울산광역시 남구 달동
                                binding.setLocationTv
                                    .append("${address[1]} ${address[2]} ${address[3]}")
                            }
                    }
                }
        } else {
            // 권한이 없으므로 권한 요청 알림 보내기
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    @Suppress("DEPRECATION") // api 33 이상에서 getFromLocation deprecated
    fun Geocoder.getAddress(
        latitude: Double,
        longitude: Double,
        address: (android.location.Address?) -> Unit
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getFromLocation(latitude, longitude, 1) { address(it.firstOrNull()) }
            return
        }
        try {
            address(getFromLocation(latitude, longitude, 1)?.firstOrNull())
        } catch(e: Exception) {
            //will catch if there is an internet problem
            address(null)
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Snackbar.make(binding.root, "위치 권한이 동의 되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
            else{
                Snackbar.make(binding.root, "권한에 동의하지 않을 경우 이용할 수 없습니다.", Snackbar.LENGTH_SHORT).show()
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