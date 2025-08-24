package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.LEADERBOARDPAGE.Leaderboard
import com.labactivity.lala.databinding.ActivityProfileMain5Binding
import com.labactivity.lala.homepage.MainActivity4

class ProfileMainActivity5 : AppCompatActivity() {

    private lateinit var binding: ActivityProfileMain5Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileMain5Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Click listener for imageView2
        binding.imageView2.setOnClickListener {
            Log.d("ProfileMainActivity5", "ImageView2 clicked")
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
            finish()
        }

        // Click listener for Leaderboard
        binding.Leaderboard.setOnClickListener {
            Log.d("ProfileMainActivity5", "Leaderboard clicked")
            val intent = Intent(this, Leaderboard::class.java)
            startActivity(intent)
            finish()
        }
    }
}
