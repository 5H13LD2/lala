package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.labactivity.lala.databinding.ActivityMainBinding

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

        // ✅ Sign Up Button
        binding.text7.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        // ✅ Guest Login Button (Direct to MainActivity3)
        binding.btnGuest.setOnClickListener {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }
    }

    // ✅ Function to Check If User Exists
    private fun checkIfUserExists(email: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList()

                    if (signInMethods.isNotEmpty()) {
                        Toast.makeText(this, "Email exists! Proceeding...", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity3::class.java))
                    } else {
                        Toast.makeText(this, "Email not registered.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
