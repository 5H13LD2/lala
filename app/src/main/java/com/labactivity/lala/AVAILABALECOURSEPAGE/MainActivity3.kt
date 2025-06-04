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
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: CarAdapter

    companion object {
        private const val TAG = "MainActivity3"
        private const val COURSES_COLLECTION = "Courses" // Changed to lowercase to match Firestore rules
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupRecyclerView()
        fetchCoursesFromFirestore()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = CarAdapter(mutableListOf()) { selectedCourse -> // ✅ TAMA
            Toast.makeText(this, "Selected: ${selectedCourse.name}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Course clicked: ${selectedCourse.name}")
        }

        binding.carRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity3)
            adapter = this@MainActivity3.adapter
        }
    }

    private fun fetchCoursesFromFirestore() {
        Log.d(TAG, "Fetching courses from Firestore...")

        firestore.collection(COURSES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Successfully fetched ${result.size()} documents")

                if (result.isEmpty) {
                    Log.w(TAG, "No courses found in Firestore collection: $COURSES_COLLECTION")
                    Toast.makeText(this, "No courses available", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val courseList = mutableListOf<Car>()

                for (document in result) {
                    try {
                        // Extract data from Firestore document
                        val name = document.getString("name")
                        val id = document.getString("id")
                        val description = document.getString("description")

                        // Validate required fields
                        if (name.isNullOrBlank() || id.isNullOrBlank()) {
                            Log.w(TAG, "Skipping document ${document.id} - missing required fields")
                            continue
                        }

                        // Create Car object with mapping logic
                        val car = Car(
                            name = name,
                            imageResId = getImageResourceId(id),
                            backgroundColor = getBackgroundColor(id),
                            description = description ?: "No description available"
                        )

                        courseList.add(car)
                        Log.d(TAG, "Added course: $name (ID: $id)")

                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing document: ${document.id}", e)
                    }
                }

                // Update adapter with new data
                adapter.updateCourses(courseList)
                Log.d(TAG, "Total courses loaded: ${courseList.size}")

                if (courseList.isEmpty()) {
                    Toast.makeText(this, "No valid courses found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching courses from Firestore", exception)
                Toast.makeText(this, "Failed to load courses: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getImageResourceId(courseId: String): Int {
        return when (courseId.lowercase()) {
            "java" -> R.drawable.java
            "python" -> R.drawable.python
            "sql" -> R.drawable.sql
            else -> R.drawable.java // Using existing drawable as fallback
        }
    }

    private fun getBackgroundColor(courseId: String): Int {
        return when (courseId.lowercase()) {
            "java" -> CarColors.JAVA
            "python" -> CarColors.PYTHON
            "sql" -> CarColors.MYSQL
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
