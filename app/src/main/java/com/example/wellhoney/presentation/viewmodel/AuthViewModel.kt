package com.example.wellhoney.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val sharedPrefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _loginState = MutableLiveData<Result<Unit>>()
    val loginState: LiveData<Result<Unit>> = _loginState

    init {
        if (auth.currentUser != null) {
            _loginState.value = Result.success(Unit)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = Result.failure(Exception("Поля не должны быть пустыми"))
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
                    _loginState.value = Result.success(Unit)
                } else {
                    _loginState.value = Result.failure(task.exception ?: Exception("Ошибка входа"))
                }
            }
    }

    fun logout() {
        auth.signOut()
        sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
        _loginState.value = Result.failure(Exception("Выход выполнен"))
    }
}