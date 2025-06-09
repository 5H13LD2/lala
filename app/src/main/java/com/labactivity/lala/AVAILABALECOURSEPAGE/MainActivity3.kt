// Enhanced MainActivity3 with Dynamic Course Loading

package com.labactivity.lala.AVAILABALECOURSEPAGE

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityMain3Binding

class MainActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityMain3Binding
    private lateinit var adapter: CarAdapter
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
        setupDynamicCourseLoading()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener when activity is destroyed
        coursesListener?.remove()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = CarAdapter(mutableListOf()) { selectedCourse ->
            Toast.makeText(this, "Selected: ${selectedCourse.name}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Course clicked: ${selectedCourse.name}")
        }

        binding.carRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity3)
            adapter = this@MainActivity3.adapter
        }
    }

    /**
     * Sets up real-time dynamic course loading from Firebase
     * This will automatically update when courses are added/removed from your LMS
     */
    private fun setupDynamicCourseLoading() {
        Log.d(TAG, "=== SETTING UP DYNAMIC COURSE LOADING ===")

        val firestore = FirebaseFirestore.getInstance()

        // Set up real-time listener for courses collection
        coursesListener = firestore.collection(COURSES_COLLECTION)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "❌ Error listening to courses collection", exception)
                    handleFirebaseError(exception)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "✅ Received ${snapshot.size()} courses from Firebase")

                    // Track changes for better logging
                    for (docChange in snapshot.documentChanges) {
                        when (docChange.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                Log.d(TAG, "📄 New course added: ${docChange.document.id}")
                            }
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                Log.d(TAG, "📝 Course modified: ${docChange.document.id}")
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                Log.d(TAG, "🗑️ Course removed: ${docChange.document.id}")
                            }
                        }
                    }

                    processDynamicCourses(snapshot.documents)
                } else {
                    Log.w(TAG, "⚠️ Snapshot is null")
                }
            }
    }

    /**
     * Processes courses dynamically - handles any course structure from your LMS
     */
    private fun processDynamicCourses(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val courseList = mutableListOf<Car>()

        for (document in documents) {
            try {
                Log.d(TAG, "Processing document: ${document.id}")

                // Try multiple field name variations for flexibility
                val courseName = document.getString("courseName")
                    ?: document.getString("name")
                    ?: document.getString("title")
                    ?: document.getString("course_name")

                val courseId = document.getString("courseId")
                    ?: document.getString("id")
                    ?: document.getString("course_id")
                    ?: document.id // Use document ID as fallback

                val description = document.getString("description")
                    ?: document.getString("desc")
                    ?: document.getString("summary")
                    ?: "No description available"

                // Handle category for better organization
                val category = document.getString("category")
                    ?: document.getString("subject")
                    ?: "General"

                // Handle difficulty level
                val difficulty = document.getString("difficulty")
                    ?: document.getString("level")
                    ?: "Beginner"

                Log.d(TAG, "Course data - Name: $courseName, ID: $courseId, Category: $category")

                if (courseName.isNullOrBlank()) {
                    Log.w(TAG, "⚠️ Skipping document ${document.id} - missing course name")
                    continue
                }

                // Create course with dynamic data
                val car = Car(
                    name = courseName,
                    courseId = courseId,
                    imageResId = getDynamicImageResource(courseId, category, courseName),
                    backgroundColor = getDynamicBackgroundColor(courseId, category, courseName),
                    description = "$description • $category • $difficulty"
                )

                courseList.add(car)
                Log.d(TAG, "✅ Added course: $courseName (Category: $category)")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error processing document ${document.id}: ${e.message}", e)
            }
        }

        // Update the adapter with all courses
        adapter.updateCourses(courseList)

        Log.d(TAG, "📊 Total courses loaded: ${courseList.size}")

        if (courseList.isEmpty()) {
            showEmptyState()
        } else {
            showSuccessState(courseList.size)
        }
    }

    /**
     * Dynamic image resource selection based on course content
     */
    private fun getDynamicImageResource(courseId: String, category: String, courseName: String): Int {
        val idLower = courseId.lowercase()
        val categoryLower = category.lowercase()
        val nameLower = courseName.lowercase()

        return when {
            // Programming Languages
            idLower.contains("java") || nameLower.contains("java") -> R.drawable.java
            idLower.contains("python") || nameLower.contains("python") -> R.drawable.python
            idLower.contains("javascript") || nameLower.contains("javascript") || nameLower.contains("js") ->
                getDrawableResourceSafely("javascript", R.drawable.book)
            idLower.contains("react") || nameLower.contains("react") ->
                getDrawableResourceSafely("react", R.drawable.book)
            idLower.contains("android") || nameLower.contains("android") ->
                getDrawableResourceSafely("android", R.drawable.book)

            // Database
            idLower.contains("sql") || nameLower.contains("sql") || nameLower.contains("mysql") -> R.drawable.sql
            idLower.contains("database") || nameLower.contains("database") -> R.drawable.sql

            // Web Development
            idLower.contains("html") || nameLower.contains("html") ->
                getDrawableResourceSafely("html", R.drawable.book)
            idLower.contains("css") || nameLower.contains("css") ->
                getDrawableResourceSafely("css", R.drawable.book)
            idLower.contains("web") || categoryLower.contains("web") ->
                getDrawableResourceSafely("web", R.drawable.book)

            // Mobile Development
            categoryLower.contains("mobile") || idLower.contains("mobile") ->
                getDrawableResourceSafely("mobile", R.drawable.book)

            // Data Science
            categoryLower.contains("data") || nameLower.contains("data science") ->
                getDrawableResourceSafely("data", R.drawable.book)

            // Default book icon for all other courses
            else -> R.drawable.book
        }
    }

    /**
     * Dynamic background color selection
     */
    private fun getDynamicBackgroundColor(courseId: String, category: String, courseName: String): Int {
        val idLower = courseId.lowercase()
        val categoryLower = category.lowercase()
        val nameLower = courseName.lowercase()

        return when {
            // Programming Languages
            idLower.contains("java") || nameLower.contains("java") -> CarColors.JAVA
            idLower.contains("python") || nameLower.contains("python") -> CarColors.PYTHON
            idLower.contains("javascript") || nameLower.contains("javascript") -> Color.parseColor("#F7DF1E")
            idLower.contains("react") || nameLower.contains("react") -> Color.parseColor("#61DAFB")
            idLower.contains("android") || nameLower.contains("android") -> Color.parseColor("#3DDC84")

            // Database
            idLower.contains("sql") || nameLower.contains("sql") || nameLower.contains("database") -> CarColors.MYSQL

            // Web Development
            categoryLower.contains("web") || idLower.contains("web") -> Color.parseColor("#FF6B6B")

            // Mobile Development
            categoryLower.contains("mobile") || idLower.contains("mobile") -> Color.parseColor("#4ECDC4")

            // Data Science
            categoryLower.contains("data") || nameLower.contains("data") -> Color.parseColor("#45B7D1")

            // Category-based colors
            categoryLower.contains("design") -> Color.parseColor("#E74C3C")
            categoryLower.contains("business") -> Color.parseColor("#8E44AD")
            categoryLower.contains("marketing") -> Color.parseColor("#F39C12")

            // Default colors based on course name hash for consistency
            else -> generateConsistentColor(courseName)
        }
    }

    /**
     * Safely get drawable resource, fallback to default if not found
     */
    private fun getDrawableResourceSafely(resourceName: String, defaultResource: Int): Int {
        return try {
            val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
            if (resourceId != 0) resourceId else defaultResource
        } catch (e: Exception) {
            Log.w(TAG, "Drawable '$resourceName' not found, using default")
            defaultResource
        }
    }

    /**
     * Generate consistent color based on course name
     */
    private fun generateConsistentColor(courseName: String): Int {
        val colors = listOf(
            Color.parseColor("#3498DB"), // Blue
            Color.parseColor("#2ECC71"), // Green
            Color.parseColor("#E67E22"), // Orange
            Color.parseColor("#9B59B6"), // Purple
            Color.parseColor("#1ABC9C"), // Turquoise
            Color.parseColor("#E74C3C"), // Red
            Color.parseColor("#F39C12"), // Yellow
            Color.parseColor("#34495E")  // Dark Gray
        )

        return colors[courseName.hashCode().absoluteValue % colors.size]
    }

    private fun handleFirebaseError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("PERMISSION_DENIED") == true -> {
                "❌ Permission denied. Check Firebase security rules."
            }
            exception.message?.contains("UNAVAILABLE") == true -> {
                "❌ Firebase unavailable. Check internet connection."
            }
            exception.message?.contains("UNAUTHENTICATED") == true -> {
                "❌ Authentication required."
            }
            else -> {
                "❌ Connection error: ${exception.message}"
            }
        }

        Log.e(TAG, errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

        // Show empty state with retry option
        showEmptyState()
    }

    private fun showEmptyState() {
        Toast.makeText(this, "No courses available. Check your LMS or try again.", Toast.LENGTH_LONG).show()
    }

    private fun showSuccessState(courseCount: Int) {
        Toast.makeText(this, "✅ Loaded $courseCount courses from your LMS", Toast.LENGTH_SHORT).show()
    }
}

// Extension property for absolute value
private val Int.absoluteValue: Int
    get() = if (this < 0) -this else this


/*package com.labactivity.lala.AVAILABALECOURSEPAGE




import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityMain3Binding

class MainActivity3 : AppCompatActivity() {
    private lateinit var binding: ActivityMain3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val carRecyclerView: RecyclerView = binding.carRecyclerView
        carRecyclerView.layoutManager = LinearLayoutManager(this)

        //Change Var name
        val cars = listOf(
            Car("Python", R.drawable.python, CarColors.PYTHON),
            Car("Java", R.drawable.java, CarColors.JAVA),
            Car("MySQL", R.drawable.sql, CarColors.MYSQL),
        )

        carRecyclerView.adapter = CarAdapter(cars) { selectedCar ->

        }
    }
}*/