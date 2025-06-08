package com.labactivity.lala

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardMainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var spinnerCourseSelection: Spinner
    private lateinit var btnRefresh: Button
    private lateinit var firestore: FirebaseFirestore

    private var currentFragment: LeaderboardFragment? = null
    private val courseList = mutableListOf<CourseItem>()

    // Default courses - you can modify this or load from Firestore
    private val defaultCourses = listOf(
        CourseItem("python_course", "Python Programming"),
        CourseItem("java_course", "Java Programming"),
        CourseItem("kotlin_course", "Kotlin Development"),
        CourseItem("android_course", "Android Development"),
        CourseItem("web_course", "Web Development")
    )

    companion object {
        private const val TAG = "LeaderboardMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard_main)

        initializeFirestore()
        initializeViews()
        setupToolbar()
        setupCourseSpinner()
        setupRefreshButton()
        loadCourses()
    }

    private fun initializeFirestore() {
        firestore = FirebaseFirestore.getInstance()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        spinnerCourseSelection = findViewById(R.id.spinnerCourseSelection)
        btnRefresh = findViewById(R.id.btnRefresh)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Leaderboard"
    }

    private fun setupCourseSpinner() {
        spinnerCourseSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (courseList.isNotEmpty() && position < courseList.size) {
                    val selectedCourse = courseList[position]
                    loadLeaderboardForCourse(selectedCourse)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupRefreshButton() {
        btnRefresh.setOnClickListener {
            currentFragment?.refreshLeaderboard()
            Toast.makeText(this, "Refreshing leaderboard...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCourses() {
        // First, load default courses
        courseList.clear()
        courseList.addAll(defaultCourses)
        updateCourseSpinner()

        // Then try to load courses from Firestore
        loadCoursesFromFirestore()
    }

    private fun loadCoursesFromFirestore() {
        firestore.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                val firestoreCourses = mutableListOf<CourseItem>()

                for (document in documents) {
                    val courseId = document.id
                    val courseName = document.getString("courseName") ?: courseId
                    firestoreCourses.add(CourseItem(courseId, courseName))
                }

                if (firestoreCourses.isNotEmpty()) {
                    courseList.clear()
                    courseList.addAll(firestoreCourses)
                    updateCourseSpinner()
                    Log.d(TAG, "Loaded ${firestoreCourses.size} courses from Firestore")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading courses from Firestore", exception)
                // Keep using default courses
                Toast.makeText(this, "Using default courses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCourseSpinner() {
        val courseNames = courseList.map { it.courseName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourseSelection.adapter = adapter

        // Load first course by default
        if (courseList.isNotEmpty()) {
            loadLeaderboardForCourse(courseList[0])
        }
    }

    private fun loadLeaderboardForCourse(course: CourseItem) {
        currentFragment = LeaderboardFragment.newInstance(course.courseId, course.courseName)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, currentFragment!!)
            .commit()

        Log.d(TAG, "Loading leaderboard for course: ${course.courseName}")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Data class for course items
    data class CourseItem(
        val courseId: String,
        val courseName: String
    )
}