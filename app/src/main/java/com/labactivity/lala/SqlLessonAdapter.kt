package com.labactivity.lala

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

class SqlLessonAdapter(
    private val context: Context,
    private val lessons: List<Lesson>,
    private val completedLessonIds: MutableSet<String>,
    private val onLessonCompleted: (String) -> Unit
) : RecyclerView.Adapter<SqlLessonAdapter.LessonViewHolder>() { // Fixed to use SqlLessonAdapter.LessonViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.sql_itemlesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(lessons[position])
    }

    override fun getItemCount(): Int = lessons.size

    inner class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSqlLessonNumber: TextView = itemView.findViewById(R.id.sqlTvLessonNumber)  // Changed prefix to sql
        private val tvSqlLessonTitle: TextView = itemView.findViewById(R.id.sqlTvLessonTitle)  // Changed prefix to sql
        private val sqlLessonHeader: FrameLayout = itemView.findViewById(R.id.sqlLessonHeader)  // Changed prefix to sql
        private val ivSqlLessonExpand: ImageView = itemView.findViewById(R.id.sqlIvLessonExpand)  // Changed prefix to sql
        private val sqlLessonContent: LinearLayout = itemView.findViewById(R.id.sqlLessonContent)  // Changed prefix to sql
        private val sqlVideoCard: MaterialCardView = itemView.findViewById(R.id.sqlVideoCard)  // Changed prefix to sql
        private val tvSqlLessonExplanation: TextView = itemView.findViewById(R.id.sqlTvLessonExplanation)  // Changed prefix to sql
        private val tvSqlCodeExample: TextView = itemView.findViewById(R.id.sqlTvCodeExample)  // Changed prefix to sql
        private val btnSqlMarkAsDone: MaterialButton = itemView.findViewById(R.id.sqlBtnMarkAsDone)  // Changed prefix to sql
        private val ivSqlCheckmark: ImageView = itemView.findViewById(R.id.sqlIvCheckmark)  // Changed prefix to sql

        fun bind(lesson: Lesson) {
            tvSqlLessonNumber.text = lesson.number
            tvSqlLessonTitle.text = lesson.title
            tvSqlLessonExplanation.text = lesson.explanation
            tvSqlCodeExample.text = lesson.codeExample

            // Get current completion state of this lesson
            val isCompleted = completedLessonIds.contains(lesson.id)

            // Update the button and checkmark based on completion state
            updateCompletionUI(isCompleted)

            btnSqlMarkAsDone.setOnClickListener {
                // Alamin ang kasalukuyang completion state
                val isCompleted = completedLessonIds.contains(lesson.id)

                // I-toggle ang completion state
                if (isCompleted) {
                    completedLessonIds.remove(lesson.id) // I-mark as not completed
                    Log.d("Lesson", "Lesson unmarked: ${lesson.id}")
                } else {
                    completedLessonIds.add(lesson.id) // I-mark as completed
                    Log.d("Lesson", "Lesson marked as done: ${lesson.id}")
                }

                // I-update ang button UI batay sa bagong state
                updateCompletionUI(!isCompleted)

                // Log the updated state of completedLessonIds
                Log.d("Completed Lessons", "Updated list: $completedLessonIds")

                // Ipapaalam sa adapter na ang item ay nagbago (para ma-refresh ang UI)
                notifyItemChanged(adapterPosition)

                // Tawagan ang completion callback (optional)
                onLessonCompleted(lesson.id)
            }

            sqlLessonHeader.setOnClickListener {
                lesson.isExpanded = !lesson.isExpanded
                updateExpandState(lesson.isExpanded)
            }

            // Set the expanded state based on the lesson's current state
            updateExpandState(lesson.isExpanded)

            // Handle video card clicks
            sqlVideoCard.setOnClickListener {
                if (lesson.videoUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lesson.videoUrl))
                    context.startActivity(intent)
                }
            }
        }

        private fun updateCompletionUI(isCompleted: Boolean) {
            ivSqlCheckmark.visibility = if (isCompleted) View.VISIBLE else View.GONE
            btnSqlMarkAsDone.text = if (isCompleted) "Completed" else "Mark as Done"
        }

        private fun updateExpandState(isExpanded: Boolean) {
            ivSqlLessonExpand.rotation = if (isExpanded) 180f else 0f
            if (isExpanded) {
                sqlLessonContent.visibility = View.VISIBLE
                sqlLessonContent.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down))
            } else {
                sqlLessonContent.visibility = View.GONE
            }
        }
    }
}
