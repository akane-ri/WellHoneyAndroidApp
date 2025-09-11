package com.example.wellhoney.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _registerState = MutableLiveData<Result<Unit>>()
    val registerState: LiveData<Result<Unit>> = _registerState

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _registerState.value = Result.failure(Exception("Register is fail"))
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registerState.value = Result.success(Unit)
                } else {
                    _registerState.value =
                        Result.failure(task.exception ?: Exception("Unknown error"))
                }
            }
    }
}