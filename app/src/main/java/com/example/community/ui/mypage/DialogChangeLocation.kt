package com.example.community.ui.mypage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.community.R
import com.example.community.data.entity.User
import com.example.community.data.local.MyApplication
import com.example.community.data.viewModel.AuthViewModel
import com.example.community.databinding.DialogChangeLocationBinding
import com.example.community.ui.other.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.util.*

class DialogChangeLocation
    : DialogFragment(), View.OnClickListener {

    lateinit var binding: DialogChangeLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val userViewModel: AuthViewModel by viewModels()
    private val gson: Gson = Gson()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogChangeLocationBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  //배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  //dialog 모서리 둥글게

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity()) // 등록

        if (arguments != null) {
            binding.setLocationTv.text = requireArguments().getString("ocr", "")
        }


        bind()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun bind() {
        val userUid = MyApplication.prefs.getUid("uid", "")
        val userJson = MyApplication.prefs.getUser("user", "")
        val user = gson.fromJson(userJson, User::class.java)

        val setlocation = binding.setLocationTv.text.toString()
        binding.currentLocationIv.setOnClickListener { // 현재 위치 가져오기
            binding.setLocationTv.text = ""
            checkLocationPermission()
        }

        binding.identicLocationIv.setOnClickListener { // 사진 인식

            findNavController().navigate(R.id.action_dialogChangeLocation_to_dialogOcr)
        }

        binding.doneIv.setOnClickListener {

            if (setlocation == "지역을 설정해주세요") {
                Toast.makeText(requireContext(), "변경될 값이 없습니다.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            changeLocationPref(user, setlocation)
            userViewModel.changeLocation(userUid, setlocation)

            Toast.makeText(requireContext(), "${setlocation}로 설정됨", Toast.LENGTH_SHORT).show()

            val navController = findNavController()
            navController.popBackStack(R.id.homeFragment, false)

            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }

    }

    private fun changeLocationPref(user: User, location: String) {
        val changedUser = User(
            user.email, user.password, user.nickname,
            location, user.age, user.alarm, user.fcmToken
        )
        val userJson = gson.toJson(changedUser)
        MyApplication.prefs.setUser("user", userJson)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkLocationPermission() {

        //주소 초기화
        var address: List<String> = listOf("부산광역시", "수영구", "대연동")

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->

                    // 최근에 알려진 위치
                    if (location != null) {
                        Log.d("location", "${location.latitude}${location.longitude}")

                        Geocoder(requireContext(), Locale.KOREA)
                            .getAddress(
                                location.latitude,
                                location.longitude
                            ) { add: android.location.Address? ->
                                if (add != null) {
                                    address = add.getAddressLine(0).split(" ")
                                }
                                //울산광역시 남구 달동
                                binding.setLocationTv
                                    .append("${address[1]} ${address[2]}")
                            }
                    }
                }
        } else {
            // 권한이 없으므로 권한 요청 알림 보내기
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
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
        } catch (e: Exception) {
            address(null)
        }
    }

    // 권한 요청 결과 처리
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(binding.root, "위치 권한이 동의 되었습니다.", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "권한에 동의하지 않을 경우 이용할 수 없습니다.", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onClick(p0: View?) {
        dismiss()
    }

}