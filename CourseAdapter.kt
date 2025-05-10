import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(
    private val context: Context,
    private val courseList: List<String>
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseNameText: TextView = itemView.findViewById(R.id.textCourseName)
        val enrollButton: Button = itemView.findViewById(R.id.btnEnrollCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val courseName = courseList[position]
        holder.courseNameText.text = courseName

        holder.enrollButton.setOnClickListener {
            // Show confirmation dialog before enrolling
            AlertDialog.Builder(context)
                .setTitle("Enroll Course")
                .setMessage("Do you want to add this course to your account?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Save enrolled course to SharedPreferences
                    // Comment: Save the selected course and enrollment status
                    val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("enrolledCourse", courseName)
                        .putBoolean("hasEnrolled", true)
                        .apply()
                    Log.d("EnrollDebug", "Enrolled in course: $courseName. Saved to SharedPreferences.")

                    // Redirect to MainActivity4 (dashboard)
                    Log.d("EnrollDebug", "Navigating to MainActivity4 after enrollment.")
                    val intent = Intent(context, MainActivity4::class.java)
                    context.startActivity(intent)
                    // Optionally, if this is an Activity, finish() it here
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Comment: User cancelled enrollment, just close dialog
                    Log.d("EnrollDebug", "Enrollment cancelled for course: $courseName.")
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun getItemCount(): Int = courseList.size
} 