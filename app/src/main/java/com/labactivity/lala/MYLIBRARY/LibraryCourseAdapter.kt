package com.labactivity.lala.MYLIBRARY

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.labactivity.lala.R
import com.labactivity.lala.SQLCOMPILER.AllSQLChallengesActivity
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

        // Practice button click - Launch appropriate activity based on course type
        holder.practiceButton.setOnClickListener {
            try {
                val intent = when {
                    course.courseId.contains("sql", ignoreCase = true) ->
                        Intent(context, AllSQLChallengesActivity::class.java)
                    course.courseId.contains("python", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    course.courseId.contains("java", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    course.courseId.contains("php", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    course.courseId.contains("ruby", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    course.courseId.contains("kotlin", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    course.courseId.contains("javascript", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    course.courseId.contains("c_programming", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    course.courseId.contains("cpp", ignoreCase = true) ->
                        Intent(context, UnifiedCompilerActivity::class.java).apply {
                            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, course.courseId)
                        }
                    else -> null
                }

                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "Practice not available for this course yet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error opening practice: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Card click also navigates to practice
        holder.itemView.setOnClickListener {
            holder.practiceButton.performClick()
        }
    }

    override fun getItemCount() = courses.size
}