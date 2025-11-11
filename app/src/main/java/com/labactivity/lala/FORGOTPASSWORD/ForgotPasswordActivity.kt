package com.labactivity.lala.FORGOTPASSWORD

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.labactivity.lala.databinding.ActivityForgotPasswordBinding
import com.labactivity.lala.UTILS.DialogUtils

/**
 * Activity for handling password reset requests
 * Sends password reset email to user's registered email address
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupToolbar()
        setupClickListeners()
    }

    /**
     * Setup toolbar with back navigation
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Setup click listeners for buttons
     */
    private fun setupClickListeners() {
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()

            if (validateEmail(email)) {
                sendPasswordResetEmail(email)
            }
        }

        binding.textBackToLogin.setOnClickListener {
            finish()
        }
    }

    /**
     * Validate email address
     */
    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.textInputLayoutEmail.error = "Email is required"
                binding.editTextEmail.requestFocus()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.textInputLayoutEmail.error = "Please enter a valid email address"
                binding.editTextEmail.requestFocus()
                false
            }
            else -> {
                binding.textInputLayoutEmail.error = null
                true
            }
        }
    }

    /**
     * Send password reset email using Firebase Auth
     */
    private fun sendPasswordResetEmail(email: String) {
        // Show loading state
        setLoadingState(true)

        Log.d(TAG, "Attempting to send password reset email to: $email")

        // Send password reset email directly - Firebase will check if user exists
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                setLoadingState(false)
                Log.d(TAG, "Password reset email sent successfully to: $email")

                // Show success message
                showSuccessState()
                DialogUtils.showSuccessDialog(
                    this,
                    "Email Sent",
                    "Password reset email sent to $email\nPlease check your inbox and spam folder."
                )
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)
                Log.e(TAG, "Failed to send password reset email", exception)

                // Handle specific error types
                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidUserException -> {
                        "No account found with this email address. Please check your email or sign up."
                    }
                    else -> {
                        when {
                            exception.message?.contains("There is no user record", ignoreCase = true) == true ->
                                "No account found with this email address"
                            exception.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                                "Invalid email format"
                            exception.message?.contains("network", ignoreCase = true) == true ->
                                "Network error. Please check your internet connection."
                            else -> {
                                Log.e(TAG, "Unknown error: ${exception.message}")
                                "Failed to send reset email: ${exception.message}"
                            }
                        }
                    }
                }

                binding.textInputLayoutEmail.error = errorMessage
                DialogUtils.showErrorDialog(this, "Error", errorMessage)
            }
    }

    /**
     * Set loading state (show/hide progress bar and disable inputs)
     */
    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonResetPassword.isEnabled = !isLoading
        binding.editTextEmail.isEnabled = !isLoading
        binding.textBackToLogin.isEnabled = !isLoading
    }

    /**
     * Show success state after email is sent
     */
    private fun showSuccessState() {
        binding.cardSuccess.visibility = View.VISIBLE
        binding.editTextEmail.text?.clear()
        binding.textInputLayoutEmail.error = null
    }
}
