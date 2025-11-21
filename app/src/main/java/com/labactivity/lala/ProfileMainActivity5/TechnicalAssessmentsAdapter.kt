package com.labactivity.lala.ProfileMainActivity5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.labactivity.lala.R

class TechnicalAssessmentsAdapter(
    private val context: Context,
    private val assessments: MutableList<TechnicalAssessmentItem>,
    private val onAssessmentClick: (TechnicalAssessmentItem) -> Unit
) : RecyclerView.Adapter<TechnicalAssessmentsAdapter.AssessmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentViewHolder {
        android.util.Log.d("TechnicalAssessmentsAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technical_assessment, parent, false)
        return AssessmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssessmentViewHolder, position: Int) {
        android.util.Log.d("TechnicalAssessmentsAdapter", "onBindViewHolder called for position $position: ${assessments[position].title}")
        holder.bind(assessments[position])
    }

    override fun getItemCount() = assessments.size

    fun updateAssessments(newAssessments: List<TechnicalAssessmentItem>) {
        android.util.Log.d("TechnicalAssessmentsAdapter", "updateAssessments called with ${newAssessments.size} items")
        assessments.clear()
        assessments.addAll(newAssessments)
        notifyDataSetChanged()
        android.util.Log.d("TechnicalAssessmentsAdapter", "Adapter updated, itemCount = $itemCount")
    }

    inner class AssessmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val assessmentCard: MaterialCardView = itemView.findViewById(R.id.assessmentCard)
        private val assessmentIcon: ImageView = itemView.findViewById(R.id.assessmentIcon)
        private val assessmentTitle: TextView = itemView.findViewById(R.id.assessmentTitle)
        private val assessmentCategory: TextView = itemView.findViewById(R.id.assessmentCategory)
        private val assessmentDifficulty: TextView = itemView.findViewById(R.id.assessmentDifficulty)
        private val assessmentStatus: TextView = itemView.findViewById(R.id.assessmentStatus)
        private val lockIcon: ImageView = itemView.findViewById(R.id.lockIcon)

        fun bind(assessment: TechnicalAssessmentItem) {
            assessmentTitle.text = assessment.title
            assessmentCategory.text = assessment.category
            assessmentDifficulty.text = assessment.difficulty

            // Set icon based on category
            val iconResId = when {
                assessment.category.contains("Java", ignoreCase = true) -> R.drawable.java
                assessment.category.contains("Python", ignoreCase = true) -> R.drawable.python
                assessment.category.contains("SQL", ignoreCase = true) -> R.drawable.sql
                else -> R.drawable.book
            }
            assessmentIcon.setImageResource(iconResId)

            // Set difficulty badge color
            val difficultyColor = when (assessment.difficulty.lowercase()) {
                "easy" -> context.getColor(R.color.difficulty_beginner)
                "medium" -> context.getColor(R.color.difficulty_intermediate)
                "hard", "advanced" -> context.getColor(R.color.difficulty_advanced)
                else -> context.getColor(R.color.difficulty_beginner)
            }
            assessmentDifficulty.setTextColor(difficultyColor)

            // Set status text and visibility
            when {
                assessment.passed -> {
                    assessmentStatus.text = "✓ Completed (${assessment.bestScore}%)"
                    assessmentStatus.setTextColor(context.getColor(R.color.success_green))
                    assessmentStatus.visibility = View.VISIBLE
                    lockIcon.visibility = View.GONE
                }
                assessment.status == "in_progress" -> {
                    assessmentStatus.text = "⟳ In Progress (${assessment.attempts} attempts)"
                    assessmentStatus.setTextColor(context.getColor(R.color.primary_blue))
                    assessmentStatus.visibility = View.VISIBLE
                    lockIcon.visibility = View.GONE
                }
                !assessment.isUnlocked -> {
                    assessmentStatus.text = "🔒 Locked"
                    assessmentStatus.setTextColor(context.getColor(android.R.color.darker_gray))
                    assessmentStatus.visibility = View.VISIBLE
                    lockIcon.visibility = View.VISIBLE
                    assessmentCard.alpha = 0.6f
                }
                else -> {
                    assessmentStatus.text = "Available"
                    assessmentStatus.setTextColor(context.getColor(R.color.modern_primary))
                    assessmentStatus.visibility = View.VISIBLE
                    lockIcon.visibility = View.GONE
                }
            }

            assessmentCard.setOnClickListener {
                onAssessmentClick(assessment)
            }
        }
    }
}
