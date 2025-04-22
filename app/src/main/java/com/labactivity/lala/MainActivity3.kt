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
    private lateinit var binding: ActivityMain3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val carRecyclerView: RecyclerView = binding.carRecyclerView
        carRecyclerView.layoutManager = LinearLayoutManager(this)

        val cars = listOf(
            Car("Python", R.drawable.python, CarColors.PYTHON),
            Car("Java", R.drawable.java, CarColors.JAVA),
            Car("MySQL", R.drawable.sql, CarColors.MYSQL),
        )

        carRecyclerView.adapter = CarAdapter(cars) { selectedCar ->
            // ✅ Kapag na-click ang card, mag-oopen ng bagong activity
            val intent = when (selectedCar.name) {
                "Java" -> {
                    // Kung Java ang na-click, pumunta sa JavaCoreModule
                    Intent(this, JavaCoreModule::class.java).apply {
                        putExtra("CAR_NAME", selectedCar.name)
                    }
                }
                "MySQL" -> {
                    // Kung MySQL ang na-click, pumunta sa SqlCoreModule
                    Intent(this, SqlCoreModule::class.java).apply {
                        putExtra("CAR_NAME", selectedCar.name)
                    }
                }
                else -> {
                    // Otherwise, go to CoreModule for Python
                    Intent(this, CoreModule::class.java).apply {
                        putExtra("CAR_NAME", selectedCar.name)
                    }
                }
            }
            startActivity(intent)
        }
    }
}
