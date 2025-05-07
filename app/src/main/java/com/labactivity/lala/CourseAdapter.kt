package com.labactivity.lala

import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
        val context = holder.itemView.context

        holder.courseImage.setImageResource(course.imageResId)
        holder.courseTitle.text = course.name
        holder.text14.paintFlags = holder.text14.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Hide practice button and logo for SQL course
        if (course.name.contains("SQL", ignoreCase = true)) {
            holder.btnPractice.visibility = View.GONE
            holder.practiceLogo.visibility = View.GONE
        } else {
            holder.btnPractice.visibility = View.VISIBLE
            holder.practiceLogo.visibility = View.VISIBLE
        }

        // ✅ These listeners must always be set, even for SQL
        holder.btnContinue.setOnClickListener {
            val intent = when {
                course.name.contains("Python", ignoreCase = true) ->
                    Intent(context, CoreModule::class.java)
                course.name.contains("Java", ignoreCase = true) ->
                    Intent(context, JavaCoreModule::class.java)
                course.name.contains("SQL", ignoreCase = true) ->
                    Intent(context, SqlCoreModule::class.java)
                else -> null
            }
            intent?.let { context.startActivity(it) }
        }

        holder.btnPractice.setOnClickListener {
            val intent = Intent(context, MainActivity7::class.java)
            context.startActivity(intent)
        }

        holder.btnFlashcard.setOnClickListener {
            Toast.makeText(context, "Flashcard clicked", Toast.LENGTH_SHORT).show()
        }


    }

    override fun getItemCount(): Int = courseList.size
}
