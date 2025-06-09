// Enhanced CarAdapter with Dynamic Course Handling

package com.labactivity.lala.AVAILABALECOURSEPAGE

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.labactivity.lala.LEARNINGMATERIAL.JavaCoreModule
import com.labactivity.lala.LEARNINGMATERIAL.CoreModule
import com.labactivity.lala.LEARNINGMATERIAL.SqlCoreModule
import com.labactivity.lala.R

// Enhanced Car colors with more options
object CarColors {
    val PYTHON = Color.parseColor("#E8A064")
    val JAVA = Color.parseColor("#4F8BEF")
    val MYSQL = Color.parseColor("#9D84C9")
    val JAVASCRIPT = Color.parseColor("#F7DF1E")
    val REACT = Color.parseColor("#61DAFB")
    val ANDROID = Color.parseColor("#3DDC84")
    val WEB = Color.parseColor("#FF6B6B")
    val MOBILE = Color.parseColor("#4ECDC4")
    val DATA_SCIENCE = Color.parseColor("#45B7D1")
    val DESIGN = Color.parseColor("#E74C3C")
    val BUSINESS = Color.parseColor("#8E44AD")
    val MARKETING = Color.parseColor("#F39C12")
}

// Enhanced Car data class
data class Car(
    val name: String,
    val courseId: String = "",
    val imageResId: Int,
    val backgroundColor: Int,
    val description: String = "",
    val category: String = "General",
    val difficulty: String = "Beginner"
)

class CarAdapter(
    private var cars: MutableList<Car>,
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "CarAdapter"
        private const val USERS_COLLECTION = "users"
        private const val COURSE_TAKEN_FIELD = "courseTaken"
        private const val COURSES_COLLECTION = "courses"
    }

    fun updateCourses(newCourses: List<Car>) {
        val oldSize = cars.size
        cars.clear()
        cars.addAll(newCourses)

        // Notify adapter of changes for better performance
        if (oldSize == 0) {
            notifyDataSetChanged()
        } else {
            notifyDataSetChanged() // For simplicity, you can optimize this with DiffUtil
        }

        Log.d(TAG, "📱 Adapter updated with ${newCourses.size} courses")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(cars[position])
    }

    override fun getItemCount() = cars.size

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.carNameTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.carImageView)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val enrollButton: Button = itemView.findViewById(R.id.button)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.carDescriptionTextView)

        fun bind(car: Car) {
            nameTextView.text = car.name
            imageView.setImageResource(car.imageResId)
            cardView.setCardBackgroundColor(car.backgroundColor)
            descriptionTextView.text = car.description

            // Set up card click listener
            cardView.setOnClickListener {
                onItemClick(car)
            }

            // Set up the Enroll Course button click listener
            enrollButton.setOnClickListener {
                showEnrollmentDialog(car)
            }
        }

        private fun showEnrollmentDialog(car: Car) {
            val context = itemView.context

            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val btnYes = dialog.findViewById<Button>(R.id.btnYes)
            val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
            val dialogMessage = dialog.findViewById<TextView>(R.id.dialogMessage)

            dialogTitle.text = "Enroll Course"
            dialogMessage.text = "Do you want to add ${car.name} to your account?"

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnYes.setOnClickListener {
                Log.d(TAG, "🎓 User confirmed enrollment for course: ${car.name}")

                saveEnrolledCourseToFirebase(car) { success ->
                    if (success) {
                        Toast.makeText(
                            context,
                            "Successfully enrolled in ${car.name}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to appropriate course module
                        val intent = getDynamicIntentForCourse(context, car)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to enroll in ${car.name}. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                dialog.dismiss()
            }

            dialog.show()
        }

        /**
         * Dynamic intent creation based on course type and availability
         */
        private fun getDynamicIntentForCourse(context: android.content.Context, car: Car): Intent {
            val courseIdLower = car.courseId.lowercase()
            val courseNameLower = car.name.lowercase()

            // Try to match with existing specific modules first
            val specificIntent = when {
                courseIdLower.contains("java") || courseNameLower.contains("java") -> {
                    Intent(context, JavaCoreModule::class.java)
                }
                courseIdLower.contains("sql") || courseNameLower.contains("sql") || courseNameLower.contains("mysql") -> {
                    Intent(context, SqlCoreModule::class.java)
                }
                else -> null
            }

            // If specific module exists, use it; otherwise use generic CoreModule
            val intent = specificIntent ?: Intent(context, CoreModule::class.java)

            // Add course data to intent
            intent.apply {
                putExtra("CAR_NAME", car.name)
                putExtra("COURSE_ID", car.courseId)
                putExtra("COURSE_DESCRIPTION", car.description)
                putExtra("COURSE_CATEGORY", car.category)
                putExtra("COURSE_DIFFICULTY", car.difficulty)
                putExtra("IS_DYNAMIC_COURSE", specificIntent == null) // Flag for generic handling
            }

            Log.d(TAG, "🎯 Created intent for course: ${car.name} -> ${intent.component?.className}")

            return intent
        }
    }

    /**
     * Enhanced Firebase enrollment with better error handling
     */
    private fun saveEnrolledCourseToFirebase(car: Car, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.e(TAG, "❌ No authenticated user found")
            callback(false)
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "💾 Saving course ${car.name} for user: $userId")

        // Update course enrollment first
        updateCourseEnrollment(car.courseId, userId) { courseSuccess ->
            if (courseSuccess) {
                // Then update user's course list
                updateUserCourseTaken(userId, car) { userSuccess ->
                    if (userSuccess) {
                        Log.d(TAG, "✅ Successfully enrolled user in ${car.name}")
                    } else {
                        Log.e(TAG, "❌ Failed to update user course list for ${car.name}")
                    }
                    callback(userSuccess)
                }
            } else {
                Log.e(TAG, "❌ Failed to update course enrollment for ${car.name}")
                callback(false)
            }
        }
    }

    /**
     * Update course document with enrolled user
     */
    private fun updateCourseEnrollment(courseId: String, userId: String, callback: (Boolean) -> Unit) {
        // First try to find by courseId field
        firestore.collection(COURSES_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val courseDoc = documents.first()
                    updateCourseDocument(courseDoc.reference, userId, callback)
                } else {
                    // Fallback: try to find by document ID
                    val docRef = firestore.collection(COURSES_COLLECTION).document(courseId)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                updateCourseDocument(docRef, userId, callback)
                            } else {
                                Log.e(TAG, "❌ Course document not found for ID: $courseId")
                                callback(false)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "❌ Error checking course document", exception)
                            callback(false)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Error finding course document", exception)
                callback(false)
            }
    }

    private fun updateCourseDocument(
        documentRef: com.google.firebase.firestore.DocumentReference,
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        documentRef.update("enrolledUsers", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Log.d(TAG, "✅ Successfully added user to course enrolledUsers")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Error updating course enrolledUsers", exception)
                callback(false)
            }
    }

    /**
     * Update user's enrolled courses list
     */
    private fun updateUserCourseTaken(userId: String, car: Car, callback: (Boolean) -> Unit) {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)

        val courseInfo = hashMapOf<String, Any?>(
            "courseId" to car.courseId,
            "courseName" to car.name,
            "category" to car.category,
            "difficulty" to car.difficulty,
            "enrolledAt" to FieldValue.serverTimestamp()
        )

        userDocRef.update(COURSE_TAKEN_FIELD, FieldValue.arrayUnion(courseInfo))
            .addOnSuccessListener {
                // Also update last enrollment timestamp
                userDocRef.update("lastEnrollmentTime", FieldValue.serverTimestamp())
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Successfully updated user course list and timestamp")
                        callback(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "⚠️ Course added but timestamp update failed", exception)
                        callback(true) // Still consider it successful
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Error updating user courseTaken", exception)

                // If user document doesn't exist, create it
                if (exception.message?.contains("No document to update") == true) {
                    createUserDocumentWithCourse(userId, courseInfo, callback)
                } else {
                    callback(false)
                }
            }
    }

    /**
     * Create new user document with course info
     */
    private fun createUserDocumentWithCourse(
        userId: String,
        courseInfo: HashMap<String, Any?>,
        callback: (Boolean) -> Unit
    ) {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)

        val userData = hashMapOf<String, Any?>(
            COURSE_TAKEN_FIELD to listOf(courseInfo),
            "createdAt" to FieldValue.serverTimestamp(),
            "lastEnrollmentTime" to FieldValue.serverTimestamp()
        )

        userDocRef.set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Created new user document with course info")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to create user document", exception)
                callback(false)
            }
    }
}