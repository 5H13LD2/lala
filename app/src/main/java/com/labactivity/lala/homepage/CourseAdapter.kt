package com.labactivity.lala.homepage

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.FLASHCARD.FlashcardActivity
import com.labactivity.lala.LEARNINGMATERIAL.CoreModule
import com.labactivity.lala.PYTHONCOMPILER.MainActivity7
import com.labactivity.lala.R
import com.labactivity.lala.SQLCOMPILER.sqlcompiler
import com.labactivity.lala.AVAILABLECOURSEPAGE.Course  // Use the full Course class from AVAILABLECOURSEPAGE

class CourseAdapter(
    private var courses: MutableList<Course> = mutableListOf()
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val TAG = "HomepageCourseAdapter"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadEnrolledCourses()
    }

    private fun loadEnrolledCourses() {
        auth.currentUser?.let { user ->
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val enrolledCourses = document.get("courseTaken") as? List<Map<String, Any>> ?: listOf()
                    Log.d(TAG, "Found ${enrolledCourses.size} enrolled courses")

                    courses.clear()
                    enrolledCourses.forEach { courseData ->
                        courses.add(
                            Course(
                                courseId = courseData["courseId"] as? String ?: "",
                                name = courseData["courseName"] as? String ?: "",
                                imageResId = getImageForCourse(courseData["courseId"] as? String ?: ""),
                                description = courseData["description"] as? String ?: "",
                                category = courseData["category"] as? String ?: "General",
                                difficulty = courseData["difficulty"] as? String ?: "Beginner"
                            )
                        )
                    }
                    notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading enrolled courses", e)
                }
        }
    }

    private fun getImageForCourse(courseId: String): Int {
        return when {
            courseId.contains("python", ignoreCase = true) -> R.drawable.python
            courseId.contains("java", ignoreCase = true) -> R.drawable.java
            courseId.contains("sql", ignoreCase = true) -> R.drawable.sql
            else -> R.drawable.book
        }
    }

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseImage: ImageView = view.findViewById(R.id.courseImage)
        val courseTitle: TextView = view.findViewById(R.id.courseTitle)
        val btnContinue: Button = view.findViewById(R.id.btnContinueLearning)
        val btnFlashcard: Button = view.findViewById(R.id.btnFlashcard)
        val btnPractice: Button = view.findViewById(R.id.btnPractice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        val context = holder.itemView.context

        holder.courseImage.setImageResource(course.imageResId)
        holder.courseTitle.text = course.name

        // Continue Learning button
        holder.btnContinue.setOnClickListener {
            try {
                val intent = Intent(context, CoreModule::class.java).apply {
                    putExtra("COURSE_ID", course.courseId)
                    putExtra("COURSE_NAME", course.name)
                    putExtra("COURSE_CATEGORY", course.category)
                    putExtra("COURSE_DIFFICULTY", course.difficulty)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Navigating to CoreModule for course: ${course.courseId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to CoreModule", e)
                Toast.makeText(context, "Unable to open course", Toast.LENGTH_SHORT).show()
            }
        }

        // Flashcard button
        holder.btnFlashcard.setOnClickListener {
            try {
                val intent = when {
                    course.courseId.contains("python", ignoreCase = true) -> 
                        Intent(context, FlashcardActivity::class.java)
                    else -> null
                }

                if (intent != null) {
                    intent.putExtra("COURSE_ID", course.courseId)
                    context.startActivity(intent)
                    Log.d(TAG, "Opening flashcards for course: ${course.courseId}")
                } else {
                    Toast.makeText(context, 
                        "Flashcards not available for this course yet", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error opening flashcards", e)
                Toast.makeText(context, "Unable to open flashcards", Toast.LENGTH_SHORT).show()
            }
        }

        // Practice button
        holder.btnPractice.setOnClickListener {
            try {
                val intent = when {
                    course.courseId.contains("python", ignoreCase = true) -> 
                        Intent(context, MainActivity7::class.java)
                    course.courseId.contains("sql", ignoreCase = true) -> 
                        Intent(context, sqlcompiler::class.java)
                    else -> null
                }

                if (intent != null) {
                    intent.putExtra("COURSE_ID", course.courseId)
                    context.startActivity(intent)
                    Log.d(TAG, "Opening practice for course: ${course.courseId}")
                } else {
                    Toast.makeText(context, 
                        "Practice not available for this course yet", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error opening practice", e)
                Toast.makeText(context, "Unable to open practice", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = courses.size

    fun refreshCourses() {
        loadEnrolledCourses()
    }
}
