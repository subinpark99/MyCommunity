package com.example.community.data.viewModel


import androidx.lifecycle.*
import com.example.community.data.entity.User

import com.example.community.data.repository.AuthRepository
import kotlinx.coroutines.launch


class AuthViewModel : ViewModel() {


    private var _registerState = MutableLiveData<Boolean>()
    val registerState: LiveData<Boolean> = _registerState

    private var _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> = _loginState

    private var _withDrawState = MutableLiveData<Boolean>()
    val withDrawState: LiveData<Boolean> = _withDrawState

    private var _changePwState = MutableLiveData<Boolean>()
    val changePwState: LiveData<Boolean> = _changePwState


    private val authRepo = AuthRepository()

    fun registerUser(
        nickname: String, email: String, password: String,
        location: String, age: Int, alarm: Boolean
    ) =
        viewModelScope.launch {
            if (checkRegisterNull(email, password, location, age)) {
                val user = User(email, password, nickname, location, age, alarm)
                authRepo.registerUser(email, password, user) { success ->
                    if (success) {
                        _registerState.postValue(true)
                    } else {
                        _registerState.postValue(false)
                    }
                }
            } else {
                throw IllegalArgumentException("모든 값을 입력해주세요")
            }
        }

    private fun checkRegisterNull(
        email: String,
        password: String,
        location: String,
        age: Int
    ): Boolean {
        return !(email.isEmpty() && password.isEmpty() &&
                location.isEmpty() && age.toString().isEmpty())
    }


    fun getFcmToken(userUid: String) {
        return authRepo.getFcmToken(userUid)
    }

    fun loginUser(email: String, password: String) =
        viewModelScope.launch {
            if (checkLoginNull(email, password)) {
                authRepo.loginUser(email, password) { success ->
                    if (success) {
                        _loginState.postValue(true)
                    } else {
                        _loginState.postValue(false)
                    }
                }

            } else {
                throw IllegalArgumentException("모든 값을 입력해주세요")
            }
        }

    private fun checkLoginNull(email: String, password: String): Boolean {
        return !(email.isEmpty() && password.isEmpty())
    }

    fun getUser(userUid: String): MutableLiveData<User?> {
        return authRepo.getUser(userUid)
    }

    fun logout() {
        return authRepo.logout()
    }

    fun withdraw(userUid: String) {
        return authRepo.withdraw(userUid) { success ->
            if (success) {
                _withDrawState.postValue(true)
            } else {
                _withDrawState.postValue(false)
            }
        }
    }

    fun deleteAllMyComment(uid: String) {
        return authRepo.deleteAllMyComment(uid)
    }

    fun deleteAllMyPost(uid: String) {
        return authRepo.deleteAllMyPost(uid)
    }

    fun deleteAllMyReply(uid: String) {
        return authRepo.deleteAllMyReply(uid)
    }

    fun changeLocation(userUid: String, setLocation: String) {
        viewModelScope.launch {
            authRepo.changeLocation(userUid, setLocation)
        }
    }

    fun changePassword(userUid: String, newPw: String) {
        viewModelScope.launch {
            authRepo.changePassword(userUid, newPw) { success ->
                if (success) {
                    _changePwState.postValue(true)
                } else {
                    _changePwState.postValue(false)
                }
            }
        }
    }


    fun setSwitchOn(userUid: String) {
        viewModelScope.launch {
            authRepo.setSwitchOn(userUid)
        }
    }

    fun setSwitchOff(userUid: String) {
        viewModelScope.launch {
            authRepo.setSwitchOff(userUid)
        }
    }

    fun setToggle(userUid: String): MutableLiveData<Boolean?> {
        return authRepo.setToggle(userUid)
    }

}


