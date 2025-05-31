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

class JavaLessonAdapter(
    private val context: Context,
    private val lessons: List<Lesson>,
    private val completedLessonIds: MutableSet<String>,
    private val onLessonCompleted: (String) -> Unit
) : RecyclerView.Adapter<JavaLessonAdapter.LessonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.java_itemlesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(lessons[position])
    }

    override fun getItemCount(): Int = lessons.size

    inner class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLessonNumber: TextView = itemView.findViewById(R.id.javaTvLessonNumber)
        private val tvLessonTitle: TextView = itemView.findViewById(R.id.javaTvLessonTitle)
        private val lessonHeader: FrameLayout = itemView.findViewById(R.id.javaLessonHeader)
        private val ivLessonExpand: ImageView = itemView.findViewById(R.id.javaIvLessonExpand)
        private val lessonContent: LinearLayout = itemView.findViewById(R.id.javaLessonContent)
        private val videoCard: MaterialCardView = itemView.findViewById(R.id.javaVideoCard)
        private val tvLessonExplanation: TextView = itemView.findViewById(R.id.javaTvLessonExplanation)
        private val tvCodeExample: TextView = itemView.findViewById(R.id.javaTvCodeExample)
        private val btnMarkAsDone: MaterialButton = itemView.findViewById(R.id.javaBtnMarkAsDone)
        private val ivCheckmark: ImageView = itemView.findViewById(R.id.javaIvCheckmark)

        fun bind(lesson: Lesson) {
            tvLessonNumber.text = lesson.number
            tvLessonTitle.text = lesson.title
            tvLessonExplanation.text = lesson.explanation
            tvCodeExample.text = lesson.codeExample

            val isCompleted = completedLessonIds.contains(lesson.id)
            updateCompletionUI(isCompleted)

            btnMarkAsDone.setOnClickListener {
                val isCompleted = completedLessonIds.contains(lesson.id)

                if (isCompleted) {
                    completedLessonIds.remove(lesson.id)
                    Log.d("Lesson", "Lesson unmarked: ${lesson.id}")
                } else {
                    completedLessonIds.add(lesson.id)
                    Log.d("Lesson", "Lesson marked as done: ${lesson.id}")
                }

                updateCompletionUI(!isCompleted)
                Log.d("Completed Lessons", "Updated list: $completedLessonIds")
                notifyItemChanged(adapterPosition)
                onLessonCompleted(lesson.id)
            }

            lessonHeader.setOnClickListener {
                lesson.isExpanded = !lesson.isExpanded
                updateExpandState(lesson.isExpanded)
            }

            updateExpandState(lesson.isExpanded)

            videoCard.setOnClickListener {
                if (lesson.videoUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lesson.videoUrl))
                    context.startActivity(intent)
                }
            }
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
