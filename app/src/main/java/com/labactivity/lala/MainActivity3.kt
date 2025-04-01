package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.databinding.ActivityMain3Binding

class MainActivity3 : AppCompatActivity() {
    private lateinit var binding: ActivityMain3Binding  // ✅ Declare View Binding properly

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Initialize View Binding
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val carRecyclerView: RecyclerView = binding.carRecyclerView //
        carRecyclerView.layoutManager = LinearLayoutManager(this)

        val cars = listOf(
            Car("Classic Car", R.drawable.user, CarColors.CLASSIC),
            Car("Sport Car", R.drawable.user, CarColors.SPORT),
            Car("Flying Car", R.drawable.user, CarColors.FLYING),
            Car("Electric Car", R.drawable.user, CarColors.ELECTRIC)
        )

        // ✅ Fix onClickListener for classic button
        binding.titleTextView.setOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
        }

        carRecyclerView.adapter = CarAdapter(cars) { selectedCar ->
            Toast.makeText(
                this,
                "${selectedCar.name}\n${selectedCar.description}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
