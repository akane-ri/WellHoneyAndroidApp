package com.example.wellhoney.domain

import com.example.wellhoney.data.models.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User?
    suspend fun logout()
    fun isLoggedIn(): Boolean
}