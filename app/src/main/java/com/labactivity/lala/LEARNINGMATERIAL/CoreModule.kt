package com.labactivity.lala.LEARNINGMATERIAL

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.labactivity.lala.UTILS.DialogUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityCoreModuleBinding
import com.labactivity.lala.homepage.MainActivity4

class CoreModule : AppCompatActivity() {
    private var _binding: ActivityCoreModuleBinding? = null
    private val binding get() = _binding!!
    private val TAG = "CoreModule"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityCoreModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        // Show loading state
        binding.progressBar?.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.INVISIBLE

        val courseId = intent.getStringExtra("COURSE_ID")?.trim()
        Log.d(TAG, "Received courseId: $courseId")

        if (courseId.isNullOrEmpty()) {
            handleError("Error: Course ID is required")
            return
        }

        // Check if user is enrolled in this course before loading
        checkEnrollmentAndLoadCourse(courseId, savedInstanceState)

        // Add button click logic to go to MainActivity4
        binding.button2?.setOnClickListener {
            startActivity(Intent(this, MainActivity4::class.java))
        }
    }

    /**
     * Check if the user is enrolled in the course before loading the learning materials
     */
    private fun checkEnrollmentAndLoadCourse(courseId: String, savedInstanceState: Bundle?) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            handleError("Error: User not authenticated")
            return
        }

        Log.d(TAG, "Checking enrollment for courseId: $courseId")

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                if (!userDoc.exists()) {
                    handleError("Error: User profile not found")
                    return@addOnSuccessListener
                }

                // Get enrolled courses
                val courseTaken = userDoc.get("courseTaken") as? List<Map<String, Any>> ?: emptyList()
                val enrolledCourseIds = courseTaken.mapNotNull { it["courseId"] as? String }

                Log.d(TAG, "User enrolled courses: $enrolledCourseIds")

                // Check if user is enrolled in this course
                if (courseId in enrolledCourseIds) {
                    Log.d(TAG, "✅ User is enrolled in course: $courseId")
                    loadCourse(courseId, savedInstanceState)
                } else {
                    Log.w(TAG, "⚠️ User is NOT enrolled in course: $courseId")
                    DialogUtils.showErrorDialog(
                        this,
                        "Access Denied",
                        "You must enroll in this course to access the learning materials."
                    )
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking enrollment", e)
                handleError("Error verifying enrollment")
            }
    }

    private fun loadCourse(courseId: String, savedInstanceState: Bundle?) {
        firestore.collection("courses")
            .document(courseId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    handleError("Error: Course not found")
                    return@addOnSuccessListener
                }

                if (!isFinishing && !isDestroyed) {
                    Log.d(TAG, "Course document exists, creating fragment")
                    if (savedInstanceState == null) {
                        // Create and add fragment
                        val fragment = CourseFragment.newInstance(courseId)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commitNow() // Use commitNow for synchronous execution

                        // Show content after fragment is added
                        binding.progressBar?.visibility = View.GONE
                        binding.fragmentContainer.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error verifying course existence", e)
                handleError("Error loading course")
            }
    }

    private fun handleError(message: String) {
        Log.e(TAG, message)
        DialogUtils.showErrorDialog(this, "Error", message)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
