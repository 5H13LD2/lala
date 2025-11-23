package com.labactivity.lala.MYLIBRARY

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.labactivity.lala.R
import com.labactivity.lala.UNIFIEDCOMPILER.ui.UnifiedCompilerActivity

class LibraryCourseAdapter(
    private val courses: List<UserCourseProgress>,
    private val context: Context
) : RecyclerView.Adapter<LibraryCourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseIcon: ImageView = view.findViewById(R.id.courseIcon)
        val courseTitle: TextView = view.findViewById(R.id.courseTitle)
        val progressText: TextView = view.findViewById(R.id.progressText)
        val progressIndicator: LinearProgressIndicator = view.findViewById(R.id.progressIndicator)
        val practiceButton: LinearLayout = view.findViewById(R.id.practiceButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]

        // Set course data
        holder.courseIcon.setImageResource(course.iconResId)
        holder.courseTitle.text = course.title
        holder.progressText.text = "${course.progress}% Complete"
        holder.progressIndicator.progress = course.progress

        // Change progress color based on completion
        when {
            course.progress >= 100 -> {
                holder.progressIndicator.setIndicatorColor(context.getColor(R.color.success_green))
            }
            course.progress >= 50 -> {
                holder.progressIndicator.setIndicatorColor(context.getColor(R.color.primary))
            }
            else -> {
                holder.progressIndicator.setIndicatorColor(context.getColor(R.color.accent))
            }
        }

        // Practice button click - Launch unified compiler
        holder.practiceButton.setOnClickListener {
            val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
                // Use course ID to automatically detect the right compiler
                putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
            }
            context.startActivity(intent)
        }

        // Card click also navigates to practice
        holder.itemView.setOnClickListener {
            holder.practiceButton.performClick()
        }
    }

    override fun getItemCount() = courses.size
}
