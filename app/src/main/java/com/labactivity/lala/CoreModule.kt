package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.databinding.ActivityCoreModuleBinding

class CoreModule : AppCompatActivity() {

    private lateinit var binding: ActivityCoreModuleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivityCoreModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the CourseFragment if it's the first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CourseFragment())
                .commit()
        }

        // Button click to navigate to MainActivity4
        binding.button2.setOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
        }
    }
}
