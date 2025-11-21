package com.labactivity.lala.REGISTERPAGE

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.labactivity.lala.UTILS.DialogUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.labactivity.lala.LOGINPAGE.MainActivity
import com.labactivity.lala.AVAILABLECOURSEPAGE.MainActivity3
import com.labactivity.lala.R
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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Go to Login Page
        binding.textView2.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Sign Up Button
        binding.btnContinue.setOnClickListener {
            val username = binding.signuser.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.signinpass.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                DialogUtils.showWarningDialog(this, "Empty Fields", "Please fill all fields")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val user = hashMapOf(
                                "username" to username,
                                "email" to email,
                                "isEnrolled" to false,  // ✅ Explicitly set to false upon registration
                                "createdAt" to FieldValue.serverTimestamp()  // ✅ Add timestamp
                            )

                            firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    DialogUtils.showSuccessDialog(this, "Success", "Account created!")
                                    startActivity(Intent(this, MainActivity3::class.java)) // courses page
                                    finish()
                                }
                                .addOnFailureListener {
                                    DialogUtils.showErrorDialog(this, "Error", "Failed to save user data.")
                                }
                        }
                    } else {
                        DialogUtils.showErrorDialog(this, "Signup Failed", "Signup failed: ${task.exception?.message}")
                    }
                }
        }

        // Google Sign-In Button
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    // Google Sign-In Launcher
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        val user = hashMapOf(
                            "username" to account.displayName,
                            "email" to account.email,
                            "isEnrolled" to false, // ✅ Set to false for new Google users
                            "createdAt" to FieldValue.serverTimestamp()  // ✅ Add timestamp
                        )

                        if (userId != null) {
                            firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    DialogUtils.showSuccessDialog(this, "Success", "Google Sign-In successful")
                                    startActivity(Intent(this, MainActivity3::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    DialogUtils.showErrorDialog(this, "Error", "Failed to save Google user data")
                                }
                        }
                    } else {
                        DialogUtils.showErrorDialog(this, "Error", "Google Sign-In failed")
                    }
                }
        } catch (e: ApiException) {
            Log.e("SignIn", "Google sign in failed", e)
            DialogUtils.showErrorDialog(this, "Error", "Google Sign-In failed")
        }
    }
}
