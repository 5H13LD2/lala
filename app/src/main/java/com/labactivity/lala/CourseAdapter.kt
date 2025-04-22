package com.labactivity.lala

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter(private val courseList: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseImage: ImageView = view.findViewById(R.id.courseImage)
        val courseTitle: TextView = view.findViewById(R.id.courseTitle)
        val btnContinue: Button = view.findViewById(R.id.btnContinueLearning)
        val btnFlashcard: Button = view.findViewById(R.id.btnFlashcard)
        val btnPractice: Button = view.findViewById(R.id.btnPractice)
        val practiceLogo: ImageView = view.findViewById(R.id.practicelogo)
        val text14: TextView = view.findViewById(R.id.text14)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.courseImage.setImageResource(course.imageResId)
        holder.courseTitle.text = course.name

        // Underline text14
        holder.text14.paintFlags = holder.text14.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Accessibility
        holder.itemView.contentDescription = "Course: ${course.name}"

        // Show/Hide Practice button and dumbbell icon
        if (course.name.contains("SQL", ignoreCase = true)) {
            holder.btnPractice.visibility = View.GONE
            holder.practiceLogo.visibility = View.GONE
        } else {
            holder.btnPractice.visibility = View.VISIBLE
            holder.practiceLogo.visibility = View.VISIBLE
        }

        // Button click listeners
        holder.btnContinue.setOnClickListener {
            // TODO: Action for Continue Learning
        }

        holder.btnFlashcard.setOnClickListener {
            // TODO: Action for Quizzes
        }

        holder.btnPractice.setOnClickListener {
            // TODO: Action for Practice (only shown when NOT SQL)
        }
    }

    override fun getItemCount(): Int = courseList.size
}
