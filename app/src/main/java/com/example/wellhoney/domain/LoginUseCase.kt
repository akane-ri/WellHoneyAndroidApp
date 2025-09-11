package com.example.wellhoney.domain

import com.example.wellhoney.data.models.User

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend fun execute(email: String, password: String): User? {
        return authRepository.login(email, password)
    }
}