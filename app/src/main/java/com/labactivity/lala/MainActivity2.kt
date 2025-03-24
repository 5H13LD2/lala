package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // ✅ Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // ✅ Google Sign-In Button (Updated with btn_google)
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        binding.progressBar.visibility = View.VISIBLE

        // I-check muna kung may existing account
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods != null && signInMethods.isNotEmpty()) {
                    // May existing account na
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Email already in use. Please login instead.", Toast.LENGTH_SHORT).show()
                } else {
                    // Wala pang existing account, proceed sa registration
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            binding.progressBar.visibility = View.GONE

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
            } else {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun saveUserToFirestore(userId: String, email: String, username: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "username" to username
        )

        firestore.collection("Users").document(userId)
            .set(user)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                runOnUiThread {
                    Toast.makeText(this, "User Added!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity3::class.java)) // ✅ Diretso sa MainActivity3
                    finish()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ✅ Google Sign-In Function
    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    // ✅ Activity Result API Implementation
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign in failed", e)
            Toast.makeText(this, "Google Sign-In Failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        binding.progressBar.visibility = View.VISIBLE

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    val email = user?.email
                    val username = user?.displayName ?: "Google User"

                    if (userId != null && email != null) {
                        saveUserToFirestore(userId, email, username)
                    }
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
