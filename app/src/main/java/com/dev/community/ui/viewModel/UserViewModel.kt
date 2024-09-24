package com.dev.community.ui.viewModel


import androidx.lifecycle.*
import com.dev.community.data.model.User
import com.dev.community.data.repository.UserRepository
import com.dev.community.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Result<User>>(Result.Loading)
    val userState = _userState.asStateFlow()

    private val _goToLoginState = MutableSharedFlow<Result<Boolean>>()
    val goToLoginState = _goToLoginState.asSharedFlow()  // 로그인화면으로 이동

    private val _loginState = MutableSharedFlow<Result<Boolean>>()
    val loginState = _loginState.asSharedFlow() // 메인으로 이동

    private val _changeLocationState = MutableSharedFlow<Result<Boolean>>()
    val changeLocationState = _changeLocationState.asSharedFlow()

    private val _changePasswordState = MutableSharedFlow<Result<Boolean>>()
    val changePasswordState = _changePasswordState.asSharedFlow()

    private val _getAlarmState = MutableSharedFlow<Result<Boolean>>()
    val getAlarmState = _getAlarmState.asSharedFlow()


    fun registerUser(
        email: String,
        password: String,
        nickname: String,
        location: String,
        age: Int
    ) {
        viewModelScope.launch {
            val result = userRepo.registerUser(email, password, nickname, location, age)
            _goToLoginState.emit(result)
        }
    }


    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepo.login(email, password)
            _loginState.emit(result)
        }
    }


    fun logout() {
        viewModelScope.launch {
            val result = userRepo.logout()
            _goToLoginState.emit(result)
        }
    }


    fun withdraw() {
        viewModelScope.launch {
            val result = userRepo.withdraw()
            _goToLoginState.emit(result)

        }
    }

    fun getUser() {
        viewModelScope.launch {
            val result = userRepo.getUser()
            _userState.value = result
        }
    }

    fun changeLocation(location: String) {
        viewModelScope.launch {
            val result = userRepo.changeLocation(location)
            _changeLocationState.emit(result)
        }
    }


    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            val result = userRepo.changePassword(newPassword)
            _changePasswordState.emit(result)
        }
    }


    fun setAlarmState(state: Boolean) {
        viewModelScope.launch {
            if (state) userRepo.setSwitchOn() else userRepo.setSwitchOff()
        }
    }


    fun getAlarmState() {
        viewModelScope.launch {
            val result = userRepo.getSwitch()
            _getAlarmState.emit(result)
        }
    }

    fun updateFcmToken(current: String) {
        viewModelScope.launch {
            userRepo.updateFcmToken(current)
        }
    }

}