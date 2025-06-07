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
import com.labactivity.lala.R
import com.labactivity.lala.LEARNINGMATERIAL.SqlCoreModule

// Car colors as an object with constant values
object CarColors {
    val PYTHON = Color.parseColor("#E8A064")
    val JAVA = Color.parseColor("#4F8BEF")
    val MYSQL = Color.parseColor("#9D84C9")
}

// Updated Car data class to include courseId
data class Car(
    val name: String,
    val courseId: String = "", // Added courseId field
    val imageResId: Int,
    val backgroundColor: Int,
    val description: String = ""
)

// CarAdapter class
class CarAdapter(
    private var cars: MutableList<Car>,
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    // Firebase instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "CarAdapter"
        private const val USERS_COLLECTION = "users"
        private const val COURSE_TAKEN_FIELD = "courseTaken"
        private const val COURSES_COLLECTION = "courses"
    }

    fun updateCourses(newCourses: List<Car>) {
        cars.clear()
        cars.addAll(newCourses)
        notifyDataSetChanged()
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
        private val descriptionTextView: TextView =
            itemView.findViewById(R.id.carDescriptionTextView)

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
                Log.d("CourseEnrollment", "User confirmed enrollment for course: ${car.name}")

                // Save course to Firebase first
                saveEnrolledCourseToFirebase(car) { success ->
                    if (success) {
                        // Show success toast
                        Toast.makeText(
                            context,
                            "Successfully enrolled in ${car.name}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Create the appropriate intent based on the course ID
                        val intent = getIntentForCourse(context, car)
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
         * Gets the appropriate intent based on the course ID
         */
        private fun getIntentForCourse(context: android.content.Context, car: Car): Intent {
            return when (car.courseId.lowercase()) {
                "java_course" -> {
                    Intent(context, JavaCoreModule::class.java).apply {
                        putExtra("CAR_NAME", car.name)
                        putExtra("COURSE_ID", car.courseId)
                    }
                }

                "sql_course" -> {
                    Intent(context, SqlCoreModule::class.java).apply {
                        putExtra("CAR_NAME", car.name)
                        putExtra("COURSE_ID", car.courseId)
                    }
                }
                // Handle legacy course names
                "java" -> {
                    Intent(context, JavaCoreModule::class.java).apply {
                        putExtra("CAR_NAME", car.name)
                        putExtra("COURSE_ID", car.courseId)
                    }
                }

                "mysql", "sql" -> {
                    Intent(context, SqlCoreModule::class.java).apply {
                        putExtra("CAR_NAME", car.name)
                        putExtra("COURSE_ID", car.courseId)
                    }
                }

                else -> {
                    Intent(context, CoreModule::class.java).apply {
                        putExtra("CAR_NAME", car.name)
                        putExtra("COURSE_ID", car.courseId)
                    }
                }
            }
        }
    }

    /**
     * Saves the enrolled course to Firebase
     * Now saves both course name and courseId for better tracking
     */
    private fun saveEnrolledCourseToFirebase(car: Car, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            callback(false)
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Saving course ${car.name} (${car.courseId}) for user: $userId")

        // Update the enrolledUsers array in the course document
        updateCourseEnrollment(car.courseId, userId) { courseUpdateSuccess ->
            if (courseUpdateSuccess) {
                // Update the user's courseTaken array
                updateUserCourseTaken(userId, car) { userUpdateSuccess ->
                    callback(userUpdateSuccess)
                }
            } else {
                callback(false)
            }
        }
    }

    /**
     * Updates the enrolledUsers array in the course document
     */
    private fun updateCourseEnrollment(
        courseId: String,
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        firestore.collection(COURSES_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val courseDoc = documents.first()
                    courseDoc.reference
                        .update("enrolledUsers", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully added user to course enrolledUsers")
                            callback(true)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error updating course enrolledUsers", exception)
                            callback(false)
                        }
                } else {
                    Log.e(TAG, "Course document not found for courseId: $courseId")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error finding course document", exception)
                callback(false)
            }
    }

    private fun createUserDocumentWithCourse(
        userId: String,
        courseInfo: HashMap<String, Any?>,
        callback: (Boolean) -> Unit
    ) {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)

        val userData = hashMapOf<String, Any?>(
            COURSE_TAKEN_FIELD to listOf(courseInfo),
            "createdAt" to FieldValue.serverTimestamp()
        )

        userDocRef.set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "Created user document with course info")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to create user document", exception)
                callback(false)
            }
    }

    private fun updateUserCourseTaken(
        userId: String,
        car: Car,
        callback: (Boolean) -> Unit
    ) {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)

        val courseInfo = hashMapOf<String, Any?>(
            "courseId" to car.courseId,
            "courseName" to car.name,
            "enrolledAt" to null
        )

        userDocRef.update(COURSE_TAKEN_FIELD, FieldValue.arrayUnion(courseInfo))
            .addOnSuccessListener {
                userDocRef.update("lastEnrollmentTime", FieldValue.serverTimestamp())
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Successfully added ${car.name} to user's courseTaken with timestamp"
                        )
                        callback(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Course added but timestamp update failed", exception)
                        callback(true)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error adding course to courseTaken", exception)

                if (exception.message?.contains("No document to update") == true) {
                    createUserDocumentWithCourse(userId, courseInfo, callback)
                } else {
                    callback(false)
                }
            }
    }
}