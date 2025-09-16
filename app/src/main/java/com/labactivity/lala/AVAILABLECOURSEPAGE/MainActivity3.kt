package com.labactivity.lala.AVAILABLECOURSEPAGE

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.labactivity.lala.LEARNINGMATERIAL.CoreModule
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityMain3Binding

class MainActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityMain3Binding
    private lateinit var adapter: CourseAdapter
    private var coursesListener: ListenerRegistration? = null

    companion object {
        private const val TAG = "MainActivity3"
        private const val COURSES_COLLECTION = "courses"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupRecyclerView()
        loadCoursesFromFirestore()
    }

    override fun onDestroy() {
        super.onDestroy()
        coursesListener?.remove()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = CourseAdapter(mutableListOf()) { selectedCourse ->
            Log.d(TAG, "Selected course: id=${selectedCourse.courseId}, name=${selectedCourse.name}")
            
            // Start CoreModule with course data
            startCoreModule(selectedCourse)
        }

        binding.carRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity3)
            adapter = this@MainActivity3.adapter
        }
    }

    private fun startCoreModule(course: Course) {
        Log.d(TAG, "Starting CoreModule for course: ${course.courseId}")
        
        val intent = Intent(this, CoreModule::class.java).apply {
            putExtra("COURSE_ID", course.courseId)
            putExtra("COURSE_NAME", course.name)
            putExtra("COURSE_DESC", course.description)
        }
        
        // Log all extras for debugging
        Log.d(TAG, "Intent extras:")
        intent.extras?.keySet()?.forEach { key ->
            Log.d(TAG, "$key: ${intent.extras?.get(key)}")
        }
        
        startActivity(intent)
    }

    private fun loadCoursesFromFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        Log.d(TAG, "Starting to load courses from Firestore")

        coursesListener = firestore.collection(COURSES_COLLECTION)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Firestore error: ${e.message}", e)
                    showEmptyState()
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d(TAG, "No courses found in Firestore")
                    showEmptyState()
                    return@addSnapshotListener
                }

                val courseList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val courseId = doc.id
                        Log.d(TAG, "Processing course document: $courseId")
                        
                        Course(
                            courseId = courseId,
                            name = doc.getString("title") ?: throw Exception("No title found"),
                            description = doc.getString("description") ?: "No description",
                            imageResId = getDynamicImage(courseId, doc.getString("title") ?: ""),
                            category = doc.getString("category") ?: "General",
                            difficulty = doc.getString("difficulty") ?: "Beginner"
                        ).also {
                            Log.d(TAG, "Created Course object: id=${it.courseId}, name=${it.name}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing course ${doc.id}: ${e.message}")
                        null
                    }
                }

                Log.d(TAG, "Loaded ${courseList.size} courses")
                adapter.updateCourses(courseList)

                if (courseList.isEmpty()) {
                    showEmptyState()
                } else {
                    showSuccessState(courseList.size)
                }
            }
    }

    private fun getDynamicImage(courseId: String, courseName: String): Int {
        val id = courseId.lowercase()
        val name = courseName.lowercase()
        return when {
            id.contains("java") || name.contains("java") -> R.drawable.java
            id.contains("python") || name.contains("python") -> R.drawable.python
            id.contains("sql") || name.contains("database") -> R.drawable.sql
            else -> R.drawable.book
        }
    }

    private fun showEmptyState() {
        Toast.makeText(this, "No courses available.", Toast.LENGTH_LONG).show()
    }

    private fun showSuccessState(count: Int) {
        Toast.makeText(this, "Loaded $count courses", Toast.LENGTH_SHORT).show()
    }
}
