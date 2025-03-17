package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.view.View // ✅ Import this
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnContinue.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.signinpass.text.toString().trim()
            val confirmPassword = binding.consign.text.toString().trim()
            val username = binding.signuser.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, username)
        }

        binding.textView2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        binding.progressBar.visibility = View.VISIBLE // ✅ Show loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE // ✅ Hide loading

                if (task.isSuccessful) {
                    auth.currentUser?.reload()?.addOnSuccessListener {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            saveUserToFirestore(userId, email, username)
                        } else {
                            Toast.makeText(this, "User ID is null!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, email: String, username: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "username" to username
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                runOnUiThread {
                    Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE // ✅ Hide ProgressBar on error
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
