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

// Car data class
data class Car(
    val name: String,
    val imageResId: Int,
    val backgroundColor: Int,
    val description: String = ""
)

// CarAdapter class - Fixed to accept MutableList and added updateCourses method
class CarAdapter(
    private var cars: MutableList<Car>, // Changed to MutableList and var
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    // Firebase instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "CarAdapter"
        private const val USERS_COLLECTION = "users"
        private const val COURSE_TAKEN_FIELD = "courseTaken"
    }

    // Add updateCourses method that MainActivity3 is calling
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
        private val descriptionTextView: TextView = itemView.findViewById(R.id.carDescriptionTextView) // Added this

        fun bind(car: Car) {
            nameTextView.text = car.name
            imageView.setImageResource(car.imageResId)
            cardView.setCardBackgroundColor(car.backgroundColor)
            descriptionTextView.text = car.description // Added this line to bind description

            // Set up card click listener - This triggers the navigation from MainActivity3
            cardView.setOnClickListener {
                onItemClick(car)
            }

            // Set up the Enroll Course button click listener
            enrollButton.setOnClickListener {
                // Show the custom enrollment confirmation dialog
                showEnrollmentDialog(car)
            }
        }

        /**
         * Shows a custom styled enrollment confirmation dialog
         * Uses our custom dialog layout for styling with blue and white theme
         */
        private fun showEnrollmentDialog(car: Car) {
            val context = itemView.context

            // Create custom dialog using our custom layout
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog)

            // Make dialog background transparent to see our custom layout's background
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Find dialog views
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val btnYes = dialog.findViewById<Button>(R.id.btnYes)
            val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
            val dialogMessage = dialog.findViewById<TextView>(R.id.dialogMessage)

            // Set dialog title and message with current course name
            dialogTitle.text = "Enroll Course"
            dialogMessage.text = "Do you want to add ${car.name} to your account?"

            // Set up cancel button click listener
            btnCancel.setOnClickListener {
                // Simply dismiss the dialog when Cancel is clicked
                dialog.dismiss()
            }

            // Set up yes button click listener
            btnYes.setOnClickListener {
                // Add debug logging for when user confirms enrollment
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

                        // Create the appropriate intent based on the course name
                        val intent = when (car.name) {
                            "Java" -> {
                                Intent(context, JavaCoreModule::class.java).apply {
                                    putExtra("CAR_NAME", car.name)
                                }
                            }
                            "MySQL" -> {
                                Intent(context, SqlCoreModule::class.java).apply {
                                    putExtra("CAR_NAME", car.name)
                                }
                            }
                            else -> {
                                Intent(context, CoreModule::class.java).apply {
                                    putExtra("CAR_NAME", car.name)
                                }
                            }
                        }

                        // Start the appropriate activity
                        context.startActivity(intent)
                    } else {
                        // Show error toast
                        Toast.makeText(
                            context,
                            "Failed to enroll in ${car.name}. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Dismiss the dialog
                dialog.dismiss()
            }

            // Show the dialog
            dialog.show()
        }
    }

    /**
     * Saves the enrolled course to the user's courseTaken field in Firebase
     * @param car The course to enroll in
     * @param callback Callback with success/failure result
     */
    private fun saveEnrolledCourseToFirebase(car: Car, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")

            callback(false)
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Saving course ${car.name} for user: $userId")

        // Reference to the user's document
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)

        // Add the course to the courseTaken array field
        userDocRef.update(COURSE_TAKEN_FIELD, FieldValue.arrayUnion(car.name))
            .addOnSuccessListener {
                Log.d(TAG, "Successfully added ${car.name} to user's courseTaken")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error adding course to courseTaken", exception)

                // If the document doesn't exist, create it with the course
                if (exception.message?.contains("No document to update") == true) {
                    createUserDocumentWithCourse(userId, car.name, callback)
                } else {
                    callback(false)
                }
            }
    }

    /**
     * Creates a new user document with the enrolled course
     * This handles the case where the user document doesn't exist yet
     */
    private fun createUserDocumentWithCourse(userId: String, courseName: String, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Creating new user document for: $userId")

        val userData = hashMapOf(
            COURSE_TAKEN_FIELD to listOf(courseName)
        )

        firestore.collection(USERS_COLLECTION).document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully created user document with course: $courseName")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error creating user document", exception)
                callback(false)
            }
    }
}