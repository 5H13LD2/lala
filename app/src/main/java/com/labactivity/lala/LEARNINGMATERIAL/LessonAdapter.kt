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
import android.widget.Toast
import androidx.core.content.ContextCompat

class LessonAdapter(
    private val context: Context,
    private val lessons: MutableList<Lesson>,  // make this mutable
    private val completedLessonIds: MutableSet<String>,
    private val onLessonClick: (Lesson) -> Unit,
    private val onLessonCompleted: (String) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    private val TAG = "LessonAdapter"

    fun updateLessons(newLessons: List<Lesson>) {
        lessons.clear()
        lessons.addAll(newLessons)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(lessons[position])
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

        private var currentLesson: Lesson? = null
        private var isExpanded = false

        fun bind(lesson: Lesson) {
            currentLesson = lesson
            
            setupBasicInfo(lesson)
            setupCompletionState(lesson)
            setupExpandableContent(lesson)
            setupClickListeners(lesson)
        }

        private fun setupBasicInfo(lesson: Lesson) {
            tvLessonNumber.text = lesson.number
            tvLessonTitle.text = lesson.title
            tvLessonExplanation.text = lesson.explanation
            tvCodeExample.text = lesson.codeExample
        }

        private fun setupCompletionState(lesson: Lesson) {
            val isCompleted = completedLessonIds.contains(lesson.id)
            updateCompletionUI(isCompleted)
        }

        private fun setupExpandableContent(lesson: Lesson) {
            isExpanded = lesson.isExpanded
            updateExpandState(isExpanded)
        }

        private fun setupClickListeners(lesson: Lesson) {
            btnMarkAsDone.setOnClickListener { toggleLessonCompletion(lesson) }
            
            lessonHeader.setOnClickListener {
                isExpanded = !isExpanded
                lesson.isExpanded = isExpanded
                updateExpandState(isExpanded)
            }

            videoCard.setOnClickListener {
                if (lesson.videoUrl.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lesson.videoUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error opening video URL", e)
                        Toast.makeText(context, "Unable to open video", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun toggleLessonCompletion(lesson: Lesson) {
            val isCompleted = completedLessonIds.contains(lesson.id)
            if (isCompleted) {
                completedLessonIds.remove(lesson.id)
            } else {
                completedLessonIds.add(lesson.id)
            }
            
            updateCompletionUI(!isCompleted)
            onLessonCompleted(lesson.id)
            Log.d(TAG, "Lesson ${lesson.id} completion toggled. Completed: ${!isCompleted}")
        }

        private fun updateCompletionUI(isCompleted: Boolean) {
            ivCheckmark.visibility = if (isCompleted) View.VISIBLE else View.GONE
            btnMarkAsDone.text = if (isCompleted) "Completed" else "Mark as Done"
            btnMarkAsDone.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isCompleted) R.color.success_green else R.color.primary
                )
            )
        }

        private fun updateExpandState(isExpanded: Boolean) {
            try {
                ivLessonExpand.rotation = if (isExpanded) 180f else 0f
                lessonContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
                
                if (isExpanded) {
                    lessonContent.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.slide_down)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating expand state", e)
                // Fallback to basic visibility
                lessonContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
            }
        }
    }
}
