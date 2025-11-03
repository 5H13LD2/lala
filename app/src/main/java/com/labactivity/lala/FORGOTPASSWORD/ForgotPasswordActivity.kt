package com.labactivity.lala.FORGOTPASSWORD

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.labactivity.lala.databinding.ActivityForgotPasswordBinding

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

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                setLoadingState(false)

                if (task.isSuccessful) {
                    // Show success message
                    showSuccessState()
                    Toast.makeText(
                        this,
                        "Password reset email sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Show error message
                    val errorMessage = when (task.exception?.message) {
                        "There is no user record corresponding to this identifier. The user may have been deleted." ->
                            "No account found with this email address"
                        "The email address is badly formatted." ->
                            "Invalid email format"
                        else ->
                            task.exception?.message ?: "Failed to send reset email"
                    }

                    binding.textInputLayoutEmail.error = errorMessage
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
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
