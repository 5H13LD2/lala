import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class MainActivity4 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        // Comment: Read enrolled course from SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val enrolledCourse = prefs.getString("enrolledCourse", null)

        if (enrolledCourse != null) {
            // Comment: Only show the enrolled course in the dashboard
            Log.d("EnrollDebug", "Loaded enrolled course: $enrolledCourse")
            // Example: If you use a RecyclerView, set its adapter to only this course
            val recyclerView: RecyclerView = findViewById(R.id.recyclerViewAssessments)
            recyclerView.adapter = CourseAdapter(this, listOf(enrolledCourse))
        } else {
            // Comment: No course found (should not happen)
            Log.d("EnrollDebug", "No enrolled course found in SharedPreferences!")
            // Optionally, show a message or redirect
        }
    }

    // ... existing code for logout, etc. ...

    fun logout() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.d("EnrollDebug", "User logged out. SharedPreferences cleared.")
        // ... proceed with logout logic ...
    }
} 