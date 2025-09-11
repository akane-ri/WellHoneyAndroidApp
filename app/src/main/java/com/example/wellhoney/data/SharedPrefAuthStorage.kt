package com.example.wellhoney.data

import android.content.Context
import androidx.core.content.edit
import com.example.wellhoney.data.models.User

class SharedPrefAuthStorage(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit {
            putString("user_id", user.id)
                .putString("user_email", user.email)
                .putString("user_name", user.name)
        }
    }

    private fun getUser(): User? {
        val id = prefs.getString("user_id", null) ?: return null
        val email = prefs.getString("user_email", null) ?: return null
        val name = prefs.getString("user_name", null) ?: return null

        return User(id, email, name)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getUser() != null
}
