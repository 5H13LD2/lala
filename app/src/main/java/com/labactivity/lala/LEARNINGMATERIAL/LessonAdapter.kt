package com.labactivity.lala.LEARNINGMATERIAL

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.labactivity.lala.R

class LessonAdapter(
    private val context: Context,
    private val lessons: List<Lesson>,
    private val completedLessonIds: MutableSet<String>,
    private val onLessonClick: (Lesson) -> Unit,
    private val onLessonCompleted: (String) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size

    inner class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLessonNumber: TextView = itemView.findViewById(R.id.tvLessonNumber)
        private val tvLessonTitle: TextView = itemView.findViewById(R.id.tvLessonTitle)
        private val lessonHeader: FrameLayout = itemView.findViewById(R.id.lessonHeader)
        private val ivLessonExpand: ImageView = itemView.findViewById(R.id.ivLessonExpand)
        private val lessonContent: LinearLayout = itemView.findViewById(R.id.lessonContent)
        private val videoCard: MaterialCardView = itemView.findViewById(R.id.videoCard)
        private val tvLessonExplanation: TextView = itemView.findViewById(R.id.tvLessonExplanation)
        private val tvCodeExample: TextView = itemView.findViewById(R.id.tvCodeExample)
        private val btnMarkAsDone: MaterialButton = itemView.findViewById(R.id.btnMarkAsDone)
        private val ivCheckmark: ImageView = itemView.findViewById(R.id.ivCheckmark)

        fun bind(lesson: Lesson) {
            tvLessonNumber.text = lesson.number
            tvLessonTitle.text = lesson.title
            tvLessonExplanation.text = lesson.explanation
            tvCodeExample.text = lesson.codeExample

            // Get current completion state of this lesson
            val isCompleted = completedLessonIds.contains(lesson.id)

            // Update the button and checkmark based on completion state
            updateCompletionUI(isCompleted)

            btnMarkAsDone.setOnClickListener {
                toggleLessonCompletion(lesson)
            }

            lessonHeader.setOnClickListener {
                lesson.isExpanded = !lesson.isExpanded
                updateExpandState(lesson.isExpanded)
            }

            // Set the expanded state based on the lesson's current state
            updateExpandState(lesson.isExpanded)

            // Handle video card clicks
            videoCard.setOnClickListener {
                if (lesson.videoUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lesson.videoUrl))
                    context.startActivity(intent)
                }
            }
        }

        private fun toggleLessonCompletion(lesson: Lesson) {
            val isCompleted = completedLessonIds.contains(lesson.id)
            if (isCompleted) {
                completedLessonIds.remove(lesson.id)  // Unmark as completed
                Log.d("Lesson", "Lesson unmarked: ${lesson.id}")
            } else {
                completedLessonIds.add(lesson.id)  // Mark as completed
                Log.d("Lesson", "Lesson marked as done: ${lesson.id}")
            }

            // Update the UI based on the new completion state
            updateCompletionUI(!isCompleted)

            // Log the updated state of completed lessons
            Log.d("Completed Lessons", "Updated list: $completedLessonIds")

            // Notify the adapter to refresh the UI
            notifyItemChanged(adapterPosition)

            // Call the completion callback
            onLessonCompleted(lesson.id)
        }

        private fun updateCompletionUI(isCompleted: Boolean) {
            ivCheckmark.visibility = if (isCompleted) View.VISIBLE else View.GONE
            btnMarkAsDone.text = if (isCompleted) "Completed" else "Mark as Done"
        }

        private fun updateExpandState(isExpanded: Boolean) {
            ivLessonExpand.rotation = if (isExpanded) 180f else 0f
            if (isExpanded) {
                lessonContent.visibility = View.VISIBLE
                lessonContent.startAnimation(AnimationUtils.loadAnimation(context,
                    R.anim.slide_down
                ))
            } else {
                lessonContent.visibility = View.GONE
            }
        }
    }
}
