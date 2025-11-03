package com.labactivity.lala.LOGINPAGE

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.labactivity.lala.AVAILABLECOURSEPAGE.MainActivity3
import com.labactivity.lala.FORGOTPASSWORD.ForgotPasswordActivity
import com.labactivity.lala.REGISTERPAGE.MainActivity2
import com.labactivity.lala.databinding.ActivityMainBinding
import com.labactivity.lala.homepage.MainActivity4

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 🔄 Clear password error on typing
        binding.password.addTextChangedListener {
            binding.passwordInputLayout.error = null
        }

        // ✅ Login Button logic
        binding.btnSubmit.setOnClickListener {
            val email = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()

            // Validation
            if (email.isEmpty() || password.isEmpty()) {
                binding.passwordInputLayout.error = null
                binding.emailInputLayout.error = if (email.isEmpty()) "Email is required" else null
                binding.passwordInputLayout.error = if (password.isEmpty()) "Password is required" else null
                return@setOnClickListener
            }

            // Firebase Authentication Login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // ✅ Login success
                        binding.passwordInputLayout.error = null
                        binding.emailInputLayout.error = null
                        val intent = Intent(this, MainActivity4::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // ❌ Login failed
                        binding.passwordInputLayout.error = "Incorrect email or password"
                    }
                }
        }

        // ✅ Sign Up Redirect
        binding.text7.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        // ✅ Forgot Password Redirect
        binding.textForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    // ✅ Optional: Check if user exists (currently unused)
    private fun checkIfUserExists(email: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList()
                    if (signInMethods.isNotEmpty()) {
                        val intent = Intent(this, MainActivity3::class.java)
                        startActivity(intent)
                    } else {
                        binding.emailInputLayout.error = "Email not registered"
                    }
                } else {
                    binding.emailInputLayout.error = "Error checking email"
                }
            }
    }
}
