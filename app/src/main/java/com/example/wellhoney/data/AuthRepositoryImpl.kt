package com.example.wellhoney.data

import com.example.wellhoney.data.models.User
import com.example.wellhoney.domain.AuthRepository

class AuthRepositoryImpl(
    private val authStorage: SharedPrefAuthStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): User? {
        return if (email == "test@example.com" && password == "1234") {
            val user = User("1", email, "Test User")
            authStorage.saveUser(user)
            user
        } else {
            null
        }
    }

    override suspend fun logout() {
        authStorage.clear()
    }

    override fun isLoggedIn(): Boolean {
        return authStorage.isLoggedIn()
    }
}
