package com.labactivity.lala

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import android.widget.Toast

object CarColors {
    val PYTHON = Color.parseColor("#E8A064")
    val JAVA = Color.parseColor("#4F8BEF")
    val MYSQL = Color.parseColor("#9D84C9")

}

data class Car(
    val name: String,
    val imageResId: Int,
    val backgroundColor: Int,
    val description: String = ""
)

class CarAdapter(
    private val cars: List<Car>,
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(cars[position])
    }

    override fun getItemCount() = cars.size

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.carNameTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.carImageView)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)

        fun bind(car: Car) {
            nameTextView.text = car.name
            imageView.setImageResource(car.imageResId)
            cardView.setCardBackgroundColor(car.backgroundColor)

            // ✅ Ginawang clickable ang buong CardView
            cardView.setOnClickListener {
                Toast.makeText(itemView.context, "${car.name}", Toast.LENGTH_SHORT).show()
                onItemClick(car)
            }
        }
    }
}
