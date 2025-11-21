package com.labactivity.lala.UTILS

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.labactivity.lala.LOGINPAGE.MainActivity
import com.labactivity.lala.homepage.MainActivity4

/**
 * Centralized authentication manager for handling login/logout state
 */
object AuthManager {
    private const val TAG = "AuthManager"
    private val auth = FirebaseAuth.getInstance()

    /**
     * Check if user is currently logged in
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get current logged in user
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Navigate to appropriate screen based on authentication state
     */
    fun navigateBasedOnAuthState(context: Context, finishCurrent: Boolean = true) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d(TAG, "User logged in: ${currentUser.email}, navigating to home")
            navigateToHome(context, finishCurrent)
        } else {
            Log.d(TAG, "User not logged in, navigating to login")
            navigateToLogin(context, finishCurrent)
        }
    }

    /**
     * Navigate to home screen
     */
    fun navigateToHome(context: Context, finishCurrent: Boolean = true) {
        val intent = Intent(context, MainActivity4::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        if (context is android.app.Activity && finishCurrent) {
            context.finish()
        }
    }

    /**
     * Navigate to login screen
     */
    fun navigateToLogin(context: Context, finishCurrent: Boolean = true) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        if (context is android.app.Activity && finishCurrent) {
            context.finish()
        }
    }

    /**
     * Logout current user
     */
    fun logout(context: Context) {
        try {
            val userEmail = auth.currentUser?.email
            Log.d(TAG, "Logging out user: $userEmail")

            // Sign out from Firebase
            auth.signOut()

            // Navigate to login
            navigateToLogin(context, true)

            Log.d(TAG, "Logout successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            throw e
        }
    }

    /**
     * Get user display name or email
     */
    fun getUserDisplayName(): String {
        val user = auth.currentUser
        return user?.displayName ?: user?.email?.split("@")?.firstOrNull() ?: "User"
    }

    /**
     * Get user email
     */
    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }
}
