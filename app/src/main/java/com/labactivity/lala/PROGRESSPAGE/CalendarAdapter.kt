package com.labactivity.lala.PROGRESSPAGE

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R

class CalendarAdapter(
    private val days: List<CalendarDay?>
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayCircle: View = itemView.findViewById(R.id.dayCircle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]

        if (day == null) {
            // Empty cell for alignment
            holder.dayCircle.visibility = View.INVISIBLE
        } else {
            holder.dayCircle.visibility = View.VISIBLE
            holder.dayCircle.setBackgroundResource(
                if (day.isActive) R.drawable.circle_active else R.drawable.circle_inactive
            )
        }
    }

    override fun getItemCount(): Int = days.size
}
