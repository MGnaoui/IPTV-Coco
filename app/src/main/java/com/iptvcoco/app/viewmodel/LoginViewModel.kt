package com.iptvcoco.app.viewmodel

import com.iptvcoco.app.IPTVCocoApplication
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptvcoco.app.model.M3UAccount
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IPTVCocoApplication.instance.repository

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(url: String, username: String, password: String, type: M3UAccount.AccountType) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val account = M3UAccount(url, username, password, type)
            val result = repository.login(account)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
