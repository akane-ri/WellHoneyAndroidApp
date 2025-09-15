package com.example.wellhoney.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.wellhoney.databinding.ActivityLoginBinding
import com.example.wellhoney.presentation.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверяем, авторизован ли пользователь
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, CatalogActivity::class.java))
            finish()
            return
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

//        binding.buttonGoogle.setOnClickListener {
//
//        }

        viewModel.loginState.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Вход успешен", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CatalogActivity::class.java))
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, "Ошибка: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}