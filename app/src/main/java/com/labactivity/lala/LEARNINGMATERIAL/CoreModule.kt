package com.labactivity.lala.LEARNINGMATERIAL

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.R
import com.labactivity.lala.databinding.ActivityCoreModuleBinding
import com.labactivity.lala.homepage.MainActivity4

class CoreModule : AppCompatActivity() {
    private lateinit var binding: ActivityCoreModuleBinding
    private val TAG = "CoreModule"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCoreModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log all received extras
        Log.d(TAG, "Received intent extras:")
        intent.extras?.keySet()?.forEach { key ->
            Log.d(TAG, "$key: ${intent.extras?.get(key)}")
        }

        val courseId = intent.getStringExtra("COURSE_ID")
        Log.d(TAG, "Received courseId: $courseId")

        if (courseId == null || courseId.isEmpty()) {
            Log.e(TAG, "No course ID provided or empty!")
            Toast.makeText(this, "Error: Course not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Verify the course exists in Firestore before proceeding
        FirebaseFirestore.getInstance().collection("courses")
            .document(courseId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.e(TAG, "Course document does not exist: $courseId")
                    Toast.makeText(this, "Error: Course not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                Log.d(TAG, "Course document exists, creating fragment")
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            CourseFragment.newInstance(courseId)
                        )
                        .commit()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error verifying course existence", e)
                Toast.makeText(this, "Error loading course", Toast.LENGTH_SHORT).show()
                finish()
            }

        // Add button click logic to go to MainActivity4
        binding.button2.setOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
        }
    }
}
