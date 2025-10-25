package com.labactivity.lala.MYLIBRARY

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.R

class MyLibraryActivity : AppCompatActivity() {

    private val TAG = "MyLibraryActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var adapter: LibraryCourseAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userCourses = mutableListOf<UserCourseProgress>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        // Hide the action bar since we have custom toolbar
        supportActionBar?.hide()

        initializeViews()
        loadUserCourses()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewLibrary)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)

        // Setup back button
        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Setup RecyclerView with GridLayoutManager (2 columns)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = LibraryCourseAdapter(userCourses, this)
        recyclerView.adapter = adapter
    }

    private fun loadUserCourses() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "No authenticated user")
            showEmptyState("Please log in to view your courses")
            return
        }

        Log.d(TAG, "Loading courses for user: $userId")
        progressBar.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE

        userCourses.clear()

        // Load user's course progress from course-level documents
        firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .get()
            .addOnSuccessListener { courseSnapshot ->
                Log.d(TAG, "Found ${courseSnapshot.size()} courses")

                if (courseSnapshot.isEmpty) {
                    showEmptyState("No courses started yet.\nStart learning to see them here!")
                    return@addOnSuccessListener
                }

                var processedCourses = 0
                val totalCourses = courseSnapshot.size()

                courseSnapshot.documents.forEach { courseProgressDoc ->
                    val courseId = courseProgressDoc.id

                    // Get course-level statistics
                    val overallProgress = courseProgressDoc.getLong("overall_progress")?.toInt() ?: 0
                    val totalLessons = courseProgressDoc.getLong("total_lessons")?.toInt() ?: 0
                    val completedLessons = courseProgressDoc.getLong("completed_lessons")?.toInt() ?: 0
                    val totalModules = courseProgressDoc.getLong("total_modules")?.toInt() ?: 0
                    val completedModules = courseProgressDoc.getLong("completed_modules")?.toInt() ?: 0

                    Log.d(TAG, "Course $courseId: $overallProgress% ($completedLessons/$totalLessons lessons, $completedModules/$totalModules modules)")

                    // Only show courses with progress data
                    if (totalLessons == 0 && totalModules == 0) {
                        // This course document exists but has no stats yet, skip it
                        processedCourses++
                        if (processedCourses == totalCourses) {
                            updateUI()
                        }
                        return@forEach
                    }

                    // Load course details from main courses collection
                    firestore.collection("courses")
                        .document(courseId)
                        .get()
                        .addOnSuccessListener { courseDoc ->
                            if (courseDoc.exists()) {
                                val title = courseDoc.getString("title") ?: "Unknown Course"
                                val description = courseDoc.getString("description") ?: ""

                                val userCourse = UserCourseProgress(
                                    courseId = courseId,
                                    title = title,
                                    description = description,
                                    progress = overallProgress,
                                    iconResId = getCourseIcon(courseId)
                                )

                                userCourses.add(userCourse)
                                Log.d(TAG, "Added course: $title with $overallProgress% progress")
                            }

                            processedCourses++
                            if (processedCourses == totalCourses) {
                                updateUI()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error loading course details for $courseId", e)
                            processedCourses++
                            if (processedCourses == totalCourses) {
                                updateUI()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user courses", e)
                showEmptyState("Error loading courses: ${e.message}")
            }
    }

    private fun updateUI() {
        progressBar.visibility = View.GONE

        if (userCourses.isEmpty()) {
            showEmptyState("No courses in your library yet")
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            adapter.notifyDataSetChanged()
            Log.d(TAG, "Displaying ${userCourses.size} courses")
        }
    }

    private fun showEmptyState(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyStateText.visibility = View.VISIBLE
        emptyStateText.text = message
    }

    private fun getCourseIcon(courseId: String): Int {
        return when {
            courseId.contains("python", ignoreCase = true) -> R.drawable.python
            courseId.contains("java", ignoreCase = true) -> R.drawable.java
            courseId.contains("sql", ignoreCase = true) -> R.drawable.sql
            else -> R.drawable.ic_book // Default icon
        }
    }
}

/**
 * Data class representing a user's course progress
 */
data class UserCourseProgress(
    val courseId: String,
    val title: String,
    val description: String,
    val progress: Int,
    val iconResId: Int
)
