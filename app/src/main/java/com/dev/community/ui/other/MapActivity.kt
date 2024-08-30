package com.dev.community.ui.other

import com.dev.community.R
import com.dev.community.util.AppUtils
import com.dev.community.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import java.util.Locale
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.ui.start.MainActivity
import com.dev.community.ui.start.SignUpActivity
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.R)
class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    private var kakaoMap: KakaoMap? = null
    private val locationRequestCode = 1000

    private val userViewModel: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppUtils.hideStatusBar(window)
        setupListeners()
        getLocation()
        showMapView()

    }


    private fun setupListeners() {
        binding.retryIv.setOnClickListener { requestLocationUpdates() }  // 위치 다시 찾기
        binding.doneIv.setOnClickListener { submitMapResult() } // OCR 결과 제출
        binding.cancelButton.setOnClickListener { finish() }
    }


    private fun submitMapResult() {
        val location = binding.getLocationTv.text.toString()
        val page = intent.getStringExtra("page")

        when (page) {
            "signup" -> navigateToSignUp(location)
            "mypage" -> updateUserLocation(location)
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

    // 사용자 위치 정보 업데이트

    private fun updateUserLocation(location: String) {
        userViewModel.changeLocation(location)

        lifecycleScope.launch {
            userViewModel.changeLocationState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collectLatest {
                    when (it) {
                        is Result.Success -> {
                            val intent = Intent(this@MapActivity, MainActivity::class.java).apply {
                                flags = FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(intent)
                            finish()
                            AppUtils.showToast(this@MapActivity, "${location}으로 변경되었습니다.")
                        }

                        is Result.Error -> Log.e("ERROR", "MapActivity - ${it.message}")
                        is Result.Loading -> Log.e("Loading", "로딩중")
                    }
                }
        }
    }


    /**
     * 위치 권한을 체크하고 현재 위치를 가져오는 함수
     */
    private fun getLocation() {
        if (checkLocationPermissions()) {
            requestLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    /**
     * 위치 권한을 확인하는 함수
     */
    private fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 위치 권한을 요청하는 함수
     */
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationRequestCode
        )
    }

    /**
     * 권한 요청 결과 처리
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                AppUtils.showSnackbar(binding.root, "권한에 동의하지 않을 경우 이용할 수 없습니다.")
            }
        }
    }


    /**
     * 위치 업데이트를 요청하는 함수
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {

        val cancellationTokenSource = CancellationTokenSource()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            location?.let {
                // 위치 정보를 성공적으로 가져옴
                val latitude = it.latitude
                val longitude = it.longitude

                updateMapWithLocation(latitude, longitude)
            } ?: run {
                // 위치 정보를 가져오지 못함
                AppUtils.showSnackbar(binding.root, "위치 정보를 가져올 수 없습니다.")
            }
        }
    }


    /**
     * 지도 초기화 및 클릭 리스너를 설정하는 함수
     */
    private fun showMapView() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("KakaoMap", "onMapDestroy")
            }

            override fun onMapError(p0: Exception?) {
                Log.e("KakaoMap", "onMapError")
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaomap: KakaoMap) {
                kakaoMap = kakaomap
                setupMapListeners(kakaomap)
            }

            override fun getZoomLevel() = 8
        })
    }

    /**
     * 지도 클릭 리스너를 설정하는 함수
     */
    private fun setupMapListeners(map: KakaoMap) {
        map.apply {
            setGestureEnable(GestureType.OneFingerDoubleTap, false)
            setGestureEnable(GestureType.Zoom, false)
            setGestureEnable(GestureType.OneFingerZoom, false)

            setOnMapClickListener { _, position, _, _ ->
                map.labelManager?.clearAll()
                val latitude = position.latitude
                val longitude = position.longitude
                updateMapWithLocation(latitude, longitude)
            }
        }
    }

    /**
     * 지도에 마커를 추가하고 위치를 업데이트하는 함수
     */
    private fun updateMapWithLocation(latitude: Double, longitude: Double) {
        kakaoMap?.let { addMapMarker(it, latitude, longitude) }
        updateLocationText(latitude, longitude)
    }

    /**
     * 지도에 마커를 추가하는 함수
     */
    private fun addMapMarker(map: KakaoMap, latitude: Double, longitude: Double) {

        // 줌 레벨을 설정하여 카메라 업데이트 객체 생성 (줌 레벨 16으로 설정)
        val cameraUpdate = CameraUpdateFactory.newCenterPosition(
            LatLng.from(latitude, longitude), 16
        )

        // 카메라 이동 및 애니메이션 설정
        kakaoMap?.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))

        val styles =
            map.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.icon_cur_location)))
        styles?.let {
            val options = LabelOptions.from(LatLng.from(latitude, longitude)).setStyles(it)
            val layer = map.labelManager!!.layer
            layer?.addLabel(options)
        } ?: Log.e("kakaoMap", "LabelStyles null값 에러")
    }

    /**
     * 현재 위치 텍스트를 업데이트하는 함수
     */
    @SuppressLint("SetTextI18n")
    private fun updateLocationText(latitude: Double, longitude: Double) {
        Geocoder(this, Locale.KOREA).getAddress(latitude, longitude) { address ->

            val adminArea = address?.adminArea.toString()
            val subLocality = address?.subLocality.toString()

            binding.getLocationTv.text = "$adminArea $subLocality"
        }
    }

    /**
     * Geocoder를 사용하여 주어진 좌표에서 주소를 가져오는 함수
     */
    @Suppress("DEPRECATION")
    private fun Geocoder.getAddress(
        latitude: Double,
        longitude: Double,
        address: (android.location.Address?) -> Unit,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getFromLocation(latitude, longitude, 1) { address(it.firstOrNull()) }
            return
        }
        try {
            address(getFromLocation(latitude, longitude, 1)?.firstOrNull())
        } catch (e: Exception) {
            address(null)
        }
    }


}

