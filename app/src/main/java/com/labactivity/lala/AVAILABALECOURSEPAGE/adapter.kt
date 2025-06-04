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

                // Show a success toast
                Toast.makeText(
                    context,
                    "Successfully enrolled in ${car.name}!",
                    Toast.LENGTH_SHORT
                ).show()

                // Create the appropriate intent based on the course name
                val intent = when (car.name) {
                    "Java" -> {
                        // If Java course, navigate to JavaCoreModule
                        Intent(context, JavaCoreModule::class.java).apply {
                            putExtra("CAR_NAME", car.name)
                        }
                    }
                    "MySQL" -> {
                        // If MySQL course, navigate to SqlCoreModule
                        Intent(context, SqlCoreModule::class.java).apply {
                            putExtra("CAR_NAME", car.name)
                        }
                    }
                    else -> {
                        // For Python or any other course, navigate to CoreModule
                        Intent(context, CoreModule::class.java).apply {
                            putExtra("CAR_NAME", car.name)
                        }
                    }
                }

                // Start the appropriate activity
                context.startActivity(intent)

                // Dismiss the dialog
                dialog.dismiss()
            }

            // Show the dialog
            dialog.show()
        }
    }
}