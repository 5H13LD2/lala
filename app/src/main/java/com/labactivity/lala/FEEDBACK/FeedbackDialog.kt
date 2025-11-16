package com.labactivity.lala.FEEDBACK

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.labactivity.lala.R
import java.text.SimpleDateFormat
import java.util.*

class FeedbackDialog(context: Context) : Dialog(context) {

    private lateinit var etFeedback: EditText
    private lateinit var tvCharCounter: TextView
    private lateinit var btnSendFeedback: MaterialButton
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_feedback)

        // Make dialog full width with rounded corners
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etFeedback = findViewById(R.id.etFeedback)
        tvCharCounter = findViewById(R.id.tvCharCounter)
        btnSendFeedback = findViewById(R.id.btnSendFeedback)
    }

    private fun setupListeners() {
        // Character counter
        etFeedback.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCounter.text = "${s?.length ?: 0} / 1000"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Send feedback button
        btnSendFeedback.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()

            if (feedback.isEmpty()) {
                Toast.makeText(context, "Please write your feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent multiple clicks
            btnSendFeedback.isEnabled = false

            // Save to Firebase and send email
            saveFeedbackToFirebase(feedback)
            sendFeedbackViaEmail(feedback)
        }
    }

    private fun saveFeedbackToFirebase(feedback: String) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(
                context,
                "Please login to send feedback",
                Toast.LENGTH_SHORT
            ).show()
            btnSendFeedback.isEnabled = true
            return
        }

        // Fetch user data from Firestore to get username
        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val username = userDoc.getString("username") ?: "Unknown User"
                val email = currentUser.email ?: "No Email"

                val feedbackData = hashMapOf(
                    "feedback" to feedback,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "userId" to currentUser.uid,
                    "userEmail" to email,
                    "username" to username,
                    "deviceInfo" to getDeviceInfo(),
                    "appVersion" to getAppVersion(),
                    "status" to "new"
                )

                firestore.collection("feedback")
                    .add(feedbackData)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(
                            context,
                            "Thank you for your feedback!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Failed to send feedback: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnSendFeedback.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                // If we can't fetch username, still send feedback with just email
                val email = currentUser.email ?: "No Email"

                val feedbackData = hashMapOf(
                    "feedback" to feedback,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "userId" to currentUser.uid,
                    "userEmail" to email,
                    "username" to "Unknown User",
                    "deviceInfo" to getDeviceInfo(),
                    "appVersion" to getAppVersion(),
                    "status" to "new"
                )

                firestore.collection("feedback")
                    .add(feedbackData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Thank you for your feedback!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            context,
                            "Failed to send feedback: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnSendFeedback.isEnabled = true
                    }
            }
    }

    private fun sendFeedbackViaEmail(feedback: String) {
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email ?: "No Email"
        val userId = currentUser?.uid ?: "anonymous"

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@techlaunch.com"))
            putExtra(Intent.EXTRA_SUBJECT, "App Feedback - ${getCurrentDateTime()}")
            putExtra(Intent.EXTRA_TEXT, buildEmailBody(feedback, userEmail, userId))
        }

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send feedback via..."))
        } catch (ex: Exception) {
            Toast.makeText(
                context,
                "No email client found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun buildEmailBody(feedback: String, userEmail: String, userId: String): String {
        return """
            User Feedback:
            $feedback

            ---
            User Information:
            Email: $userEmail
            User ID: $userId

            Device Information:
            ${getDeviceInfo()}

            App Version: ${getAppVersion()}
            Time: ${getCurrentDateTime()}
        """.trimIndent()
    }

    private fun getDeviceInfo(): String {
        return "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}, " +
                "Android: ${android.os.Build.VERSION.RELEASE}"
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}