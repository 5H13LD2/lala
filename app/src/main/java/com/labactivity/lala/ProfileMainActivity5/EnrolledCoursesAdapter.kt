package com.labactivity.lala.ProfileMainActivity5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.labactivity.lala.R

class EnrolledCoursesAdapter(
    private val context: Context,
    private val courses: MutableList<EnrolledCourseItem>,
    private val onCourseClick: (EnrolledCourseItem) -> Unit
) : RecyclerView.Adapter<EnrolledCoursesAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        android.util.Log.d("EnrolledCoursesAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_enrolled_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        android.util.Log.d("EnrolledCoursesAdapter", "onBindViewHolder called for position $position: ${courses[position].courseName}")
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size

    fun updateCourses(newCourses: List<EnrolledCourseItem>) {
        android.util.Log.d("EnrolledCoursesAdapter", "updateCourses called with ${newCourses.size} items")
        courses.clear()
        courses.addAll(newCourses)
        notifyDataSetChanged()
        android.util.Log.d("EnrolledCoursesAdapter", "Adapter updated, itemCount = $itemCount")
    }

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val courseCard: MaterialCardView = itemView.findViewById(R.id.courseCard)
        private val courseIcon: ImageView = itemView.findViewById(R.id.courseIcon)
        private val courseName: TextView = itemView.findViewById(R.id.courseName)
        private val courseCategory: TextView = itemView.findViewById(R.id.courseCategory)
        private val courseDifficulty: TextView = itemView.findViewById(R.id.courseDifficulty)
        private val courseProgress: ProgressBar = itemView.findViewById(R.id.courseProgress)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)

        fun bind(course: EnrolledCourseItem) {
            courseName.text = course.courseName
            courseCategory.text = course.category
            courseDifficulty.text = course.difficulty
            courseProgress.progress = course.progress
            progressText.text = "${course.progress}% Complete"

            // Set icon based on course name
            val iconResId = when {
                course.courseName.contains("Java", ignoreCase = true) -> R.drawable.java
                course.courseName.contains("Python", ignoreCase = true) -> R.drawable.python
                course.courseName.contains("SQL", ignoreCase = true) ||
                    course.courseName.contains("Database", ignoreCase = true) -> R.drawable.sql
                else -> R.drawable.book
            }
            courseIcon.setImageResource(iconResId)

            // Set difficulty badge color
            val difficultyColor = when (course.difficulty.lowercase()) {
                "beginner" -> context.getColor(R.color.difficulty_beginner)
                "intermediate" -> context.getColor(R.color.difficulty_intermediate)
                "advanced" -> context.getColor(R.color.difficulty_advanced)
                else -> context.getColor(R.color.difficulty_beginner)
            }
            courseDifficulty.setTextColor(difficultyColor)

            courseCard.setOnClickListener {
                onCourseClick(course)
            }
        }
    }
}
