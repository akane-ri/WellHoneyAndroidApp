package com.example.wellhoney.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellhoney.R
import com.example.wellhoney.databinding.ActivityRegisterBinding
import com.example.wellhoney.presentation.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.viewModels

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            viewModel.register(email, password)
        }

        viewModel.registerState.observe(this) { result ->
            result.onSuccess {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Toast.makeText(this, "Welcome, ${user.email}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, CatalogActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}