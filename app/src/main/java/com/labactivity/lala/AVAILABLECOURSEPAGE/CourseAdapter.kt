package com.labactivity.lala.AVAILABLECOURSEPAGE

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.LEARNINGMATERIAL.CoreModule
import com.labactivity.lala.R

class CourseAdapter(
    private var courses: MutableList<Course>,
    private val onItemClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AvailableCourseAdapter"
        private const val USERS_COLLECTION = "users"
        private const val COURSE_TAKEN_FIELD = "courseTaken"
    }

    fun updateCourses(newCourses: List<Course>) {
        // Filter out courses that user is already enrolled in
        auth.currentUser?.let { user ->
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val enrolledCourses = document.get(COURSE_TAKEN_FIELD) as? List<Map<String, Any>> ?: listOf()
                    val enrolledCourseIds = enrolledCourses.map { it["courseId"] as String }.toSet()
                    
                    val availableCourses = newCourses.filterNot { 
                        enrolledCourseIds.contains(it.courseId) 
                    }
                    
                    courses.clear()
                    courses.addAll(availableCourses)
                    notifyDataSetChanged()
                    Log.d(TAG, "Available courses updated. Showing ${courses.size} courses")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching enrolled courses", e)
                    // Still show all courses if we can't fetch enrolled ones
                    courses.clear()
                    courses.addAll(newCourses)
                    notifyDataSetChanged()
                }
        } ?: run {
            // If no user is logged in, show all courses
            courses.clear()
            courses.addAll(newCourses)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size

    private fun saveEnrolledCourseToFirebase(course: Course, callback: (Boolean) -> Unit) {
        val user = auth.currentUser ?: run {
            Log.e(TAG, "No user logged in")
            callback(false)
            return
        }

        // First check if already enrolled
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val enrolledCourses = document.get(COURSE_TAKEN_FIELD) as? List<Map<String, Any>> ?: listOf()
                if (enrolledCourses.any { it["courseId"] == course.courseId }) {
                    Log.d(TAG, "User already enrolled in ${course.name}")
                    callback(true)
                    return@addOnSuccessListener
                }

                val enrollmentData = hashMapOf(
                    "courseId" to course.courseId,
                    "courseName" to course.name,
                    "category" to course.category,
                    "difficulty" to course.difficulty,
                    "enrolledAt" to System.currentTimeMillis()
                )

                val userRef = firestore.collection(USERS_COLLECTION).document(user.uid)
                
                if (document.exists()) {
                    userRef.update(COURSE_TAKEN_FIELD, FieldValue.arrayUnion(enrollmentData))
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully enrolled in ${course.name}")
                            removeEnrolledCourse(course)
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update enrollment", e)
                            callback(false)
                        }
                } else {
                    userRef.set(hashMapOf(COURSE_TAKEN_FIELD to listOf(enrollmentData)))
                        .addOnSuccessListener {
                            Log.d(TAG, "Created new user document and enrolled in ${course.name}")
                            removeEnrolledCourse(course)
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to create user document", e)
                            callback(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to check enrollment status", e)
                callback(false)
            }
    }

    private fun removeEnrolledCourse(course: Course) {
        val position = courses.indexOfFirst { it.courseId == course.courseId }
        if (position != -1) {
            courses.removeAt(position)
            notifyItemRemoved(position)
            Log.d(TAG, "Removed enrolled course from available courses list")
        }
    }

    inner class CourseViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.carNameTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.carImageView)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val enrollButton: Button = itemView.findViewById(R.id.button)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.carDescriptionTextView)

        fun bind(course: Course) {
            nameTextView.text = course.name
            imageView.setImageResource(course.imageResId)
            descriptionTextView.text = course.description

            cardView.setOnClickListener {
                Log.d(TAG, "Card clicked for course: ${course.courseId}")
                navigateToCourse(course)
            }

            enrollButton.setOnClickListener {
                Log.d(TAG, "Enroll clicked for course: ${course.courseId}")
                showEnrollmentDialog(course)
            }
        }

        private fun navigateToCourse(course: Course) {
            val context = itemView.context
            val intent = Intent(context, CoreModule::class.java).apply {
                putExtra("COURSE_ID", course.courseId)
                putExtra("COURSE_NAME", course.name)
                putExtra("COURSE_DESC", course.description)
                putExtra("COURSE_CATEGORY", course.category)
                putExtra("COURSE_DIFFICULTY", course.difficulty)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        private fun showEnrollmentDialog(course: Course) {
            val context = itemView.context
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val btnYes = dialog.findViewById<Button>(R.id.btnYes)
            val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
            val dialogMessage = dialog.findViewById<TextView>(R.id.dialogMessage)

            dialogTitle.text = "Enroll Course"
            dialogMessage.text = "Do you want to enroll in ${course.name}?"

            btnCancel.setOnClickListener { dialog.dismiss() }
            btnYes.setOnClickListener {
                saveEnrolledCourseToFirebase(course) { success ->
                    if (success) {
                        Toast.makeText(context, "Successfully enrolled in ${course.name}!", Toast.LENGTH_SHORT).show()
                        navigateToCourse(course)
                    } else {
                        Toast.makeText(context, "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
