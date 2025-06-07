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
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityMain3Binding

class MainActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityMain3Binding
    private lateinit var adapter: CarAdapter

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

        // Directly test courses collection (which has public read access)
        testCoursesCollection()
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

    private fun testCoursesCollection() {
        Log.d(TAG, "=== TESTING COURSES COLLECTION ACCESS ===")

        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(COURSES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "✅ SUCCESS: Courses collection accessible")
                Log.d(TAG, "Found ${result.size()} documents in courses collection")

                if (result.isEmpty) {
                    Log.w(TAG, "⚠️ WARNING: Courses collection is empty")
                    createSampleCourse()
                } else {
                    // Log all documents found
                    for (doc in result) {
                        Log.d(TAG, "Document ID: ${doc.id}")
                        Log.d(TAG, "Document data: ${doc.data}")
                    }
                    fetchCoursesFromFirestore()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ FAILED: Cannot access courses collection")
                Log.e(TAG, "Error type: ${exception.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${exception.message}")
                showConnectionError(exception)

                // Load local data as fallback
                loadSampleDataLocally()
            }
    }

    private fun createSampleCourse() {
        Log.d(TAG, "Creating sample course for testing...")

        val firestore = FirebaseFirestore.getInstance()
        val sampleCourse = hashMapOf(
            "courseId" to "java_course",
            "courseName" to "Java Programming",
            "description" to "Learn Java basics to OOP",
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "enrolledUsers" to emptyList<String>()
        )

        firestore.collection(COURSES_COLLECTION)
            .add(sampleCourse)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "✅ Sample course created with ID: ${documentReference.id}")
                Toast.makeText(this, "Sample course created! Refreshing...", Toast.LENGTH_SHORT).show()

                // Now fetch courses
                fetchCoursesFromFirestore()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to create sample course: ${exception.message}")
                loadSampleDataLocally()
            }
    }

    private fun fetchCoursesFromFirestore() {
        Log.d(TAG, "Fetching courses from Firestore...")

        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(COURSES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Successfully fetched ${result.size()} documents")

                val courseList = mutableListOf<Car>()

                for (document in result) {
                    try {
                        Log.d(TAG, "Processing document: ${document.id}")

                        val name = document.getString("courseName")
                        val id = document.getString("courseId")
                        val description = document.getString("description")

                        Log.d(TAG, "courseName: $name")
                        Log.d(TAG, "courseId: $id")
                        Log.d(TAG, "description: $description")

                        if (name.isNullOrBlank() || id.isNullOrBlank()) {
                            Log.w(TAG, "Skipping document ${document.id} - missing required fields")
                            continue
                        }

                        val car = Car(
                            name = name,
                            courseId = id,
                            imageResId = getImageResourceId(id),
                            backgroundColor = getBackgroundColor(id),
                            description = description ?: "No description available"
                        )

                        courseList.add(car)
                        Log.d(TAG, "✅ Added course: $name (ID: $id)")

                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing document: ${document.id}", e)
                    }
                }

                adapter.updateCourses(courseList)
                Log.d(TAG, "Total courses loaded: ${courseList.size}")

                Toast.makeText(this, "Loaded ${courseList.size} courses from Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching courses from Firestore", exception)
                showConnectionError(exception)
                loadSampleDataLocally()
            }
    }

    private fun loadSampleDataLocally() {
        Log.d(TAG, "Loading sample data locally...")

        val sampleCourses = listOf(
            Car(
                name = "Java Programming",
                courseId = "java_course",
                imageResId = R.drawable.java,
                backgroundColor = CarColors.JAVA,
                description = "Learn Java basics to OOP (Local Sample)"
            ),
            Car(
                name = "Python Programming",
                courseId = "python_course",
                imageResId = R.drawable.python,
                backgroundColor = CarColors.PYTHON,
                description = "Learn Python basics (Local Sample)"
            )
        )

        adapter.updateCourses(sampleCourses)
        Toast.makeText(this, "Loaded local sample data", Toast.LENGTH_SHORT).show()
    }

    private fun showConnectionError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("PERMISSION_DENIED") == true -> {
                "❌ PERMISSION DENIED: Check Firestore security rules"
            }
            exception.message?.contains("UNAVAILABLE") == true -> {
                "❌ SERVICE UNAVAILABLE: Check internet connection"
            }
            exception.message?.contains("UNAUTHENTICATED") == true -> {
                "❌ UNAUTHENTICATED: User needs to be signed in"
            }
            exception.message?.contains("NOT_FOUND") == true -> {
                "❌ NOT FOUND: Collection or project doesn't exist"
            }
            else -> {
                "❌ CONNECTION ERROR: ${exception.message}"
            }
        }

        Log.e(TAG, errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun getImageResourceId(courseId: String): Int {
        return when (courseId.lowercase()) {
            "java_course" -> R.drawable.java
            "python_course" -> R.drawable.python
            "sql_course" -> R.drawable.sql
            else -> R.drawable.book
        }
    }

    private fun getBackgroundColor(courseId: String): Int {
        return when (courseId.lowercase()) {
            "java_course" -> CarColors.JAVA
            "python_course" -> CarColors.PYTHON
            "sql_course" -> CarColors.MYSQL
            else -> Color.GRAY
        }
    }
}
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