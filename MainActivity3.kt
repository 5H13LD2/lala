import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... existing setContentView and setup code ...

        // Check SharedPreferences for enrollment status
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val hasEnrolled = prefs.getBoolean("hasEnrolled", false)

        // Log and act based on enrollment status
        if (hasEnrolled) {
            // User already enrolled, skip to dashboard
            Log.d("EnrollDebug", "User already enrolled. Skipping course selection and opening MainActivity4.")
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
            finish()
            return
        } else {
            // User not enrolled, show available courses
            Log.d("EnrollDebug", "User not enrolled. Displaying available courses.")
            // ... existing code to set up adapter and show courses ...
        }
    }

    // ... existing code ...
} 