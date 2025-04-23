package com.labactivity.lala


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity7 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main7)

        // Set up the button to navigate to the Python Compiler
        val startCompilerButton = findViewById<Button>(R.id.startCompilerButton)
        startCompilerButton.setOnClickListener {
            val intent = Intent(this, PythonCompilerActivity::class.java)
            startActivity(intent)
        }
    }
}