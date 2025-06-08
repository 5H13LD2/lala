package com.labactivity.lala.LOGINPAGE

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.labactivity.lala.REGISTERPAGE.MainActivity2
import com.labactivity.lala.AVAILABALECOURSEPAGE.MainActivity3
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

        Toast.makeText(this, "Firebase is connected!", Toast.LENGTH_SHORT).show()

        // ✅ Login Button
        binding.btnSubmit.setOnClickListener {
            val email = binding.username.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkIfUserExists(email)
        }
        binding.btnSubmit.setOnClickListener {
            val email = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Authentication Login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login success
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity4::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Login failed
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // ✅ Sign Up Button
        binding.text7.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }



    }

    // ✅ Function to Check If User Exists
    private fun checkIfUserExists(email: String) {
        auth.fetchSignInMethodsForEmail(email) // Check if email exists
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList()

                    if (signInMethods.isNotEmpty()) {
                        // Email exists
                        Toast.makeText(this, "Welcome Back TechLauncher!", Toast.LENGTH_SHORT).show()

                        // Show the email of the logged-in user
                        val userEmail = email
                        Toast.makeText(this, "$userEmail logged in", Toast.LENGTH_SHORT).show()

                        // Proceed to next activity
                        val intent = Intent(this, MainActivity3::class.java)
                        startActivity(intent)
                    } else {
                        // Email not registered
                        Toast.makeText(this, "Email not registered.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
