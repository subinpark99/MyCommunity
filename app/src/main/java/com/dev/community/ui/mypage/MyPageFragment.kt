package com.dev.community.ui.mypage


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dev.community.R
import com.dev.community.util.AppUtils
import com.dev.community.app.MyApplication
import com.dev.community.util.Result
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.FragmentMypageBinding
import com.dev.community.ui.other.MapActivity
import com.dev.community.ui.other.OcrActivity
import com.dev.community.ui.start.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        setupNavigation()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getAlarmState()
        observeState()
    }

    private fun setupNavigation() {
        setupNavigationClickListeners()
        setupToggleListener()
    }


    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch { // 로그아웃
                    userViewModel.logoutState.collect { result ->
                        when (result) {
                            is Result.Success -> if (result.data) setUserSuccess()
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }


                launch { // 회원탈퇴
                    userViewModel.withdrawState.collect { result ->
                        when (result) {
                            is Result.Success -> if (result.data) setUserSuccess()
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }


                launch { // 알람 토글 상태
                    userViewModel.getAlarmState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> binding.noticeToggleBtn.isChecked = result.data
                            is Result.Error -> handleError(result.message)
                            is Result.Loading -> handleLoading()
                        }
                    }
                }
            }
        }
    }

    // 네비게이션 클릭 리스너 설정
    private fun setupNavigationClickListeners() {
        binding.mycontentsTv.setOnClickListener { navigateToMyContents("contents") }
        binding.replyTv.setOnClickListener { navigateToMyContents("comments") }
        binding.logoutTv.setOnClickListener { userViewModel.logout() }  // 로그아웃
        binding.withdrawTv.setOnClickListener { userViewModel.withdraw() }
        binding.changePwTv.setOnClickListener { navigateToChangePassword() }
        binding.changelocationTv.setOnClickListener { showLocationDialog(requireContext()) }
    }


    private fun setupToggleListener() {
        binding.noticeToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            userViewModel.setAlarmState(isChecked)
        }
    }

    // 내가 쓴 글/댓글 화면으로 이동
    private fun navigateToMyContents(page: String) {
        val action = MyPageFragmentDirections.actionMyPageFragmentToMyContentsFragment(page)
        findNavController().navigate(action)
    }


    //  비밀번호 변경 화면으로 이동
    private fun navigateToChangePassword() {
        view?.findNavController()?.navigate(R.id.action_myPageFragment_to_changePwActivity)
    }

    // 위치 변경 다이얼로그 표시
    private fun showLocationDialog(context: Context) {
        val dataList = arrayOf("현재 위치로 설정", "주민등록증으로 설정")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("선택하세요")
        builder.setItems(dataList) { _, which ->
            val intent = when (which) {
                0 -> Intent(requireContext(), MapActivity::class.java).apply {
                    putExtra("page", "mypage")
                }

                else -> Intent(requireContext(), OcrActivity::class.java).apply {
                    putExtra("page", "mypage")
                }
            }
            startActivity(intent)
        }
        builder.setNegativeButton("취소", null)
        builder.show()
    }


    private fun setUserSuccess() {

        AppUtils.showToast(requireContext(), "완료")
        MyApplication.prefs.setAutoLogin(false)

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun handleError(exception: String) {
        Log.e("ERROR", "MyPageFragment - $exception")
    }

    private fun handleLoading() {
        Log.d("loading", "loading...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
