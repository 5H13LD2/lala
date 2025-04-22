package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.labactivity.lala.databinding.ActivityCoreModuleBinding
import com.labactivity.lala.databinding.ActivitySqlCoreModuleBinding

class SqlCoreModule : AppCompatActivity() {
    private lateinit var binding: ActivitySqlCoreModuleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivitySqlCoreModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the CourseFragment if it's the first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.sqlfragment_container, SqlCourseFragment())
                .commit()
        }

        // Button click to navigate to MainActivity4
        binding.button2.setOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
        }
    }
}