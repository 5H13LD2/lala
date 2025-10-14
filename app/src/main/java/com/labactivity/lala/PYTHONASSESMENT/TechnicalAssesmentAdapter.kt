package com.labactivity.lala.PYTHONASSESMENT

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.PYTHONCOMPILER.CompilerActivity
import com.labactivity.lala.R

class TechnicalAssessmentAdapter(
    private val context: Context,
    private var challenges: List<Challenge> = listOf(),
    private var isLoading: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_ITEM = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_LOADING) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.item_assessment_skeleton, parent, false)
            SkeletonViewHolder(view)
        } else {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.item_assesment_card, parent, false)
            ChallengeViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SkeletonViewHolder) {
            // Start shimmer animation only once when view is created
            holder.startShimmerAnimation(context)
        } else if (holder is ChallengeViewHolder && !isLoading) {
            val challenge = challenges[position]
            holder.titleTextView.text = challenge.title
            holder.difficultyTextView.text = challenge.difficulty

            if (challenge.codePreview.isNotEmpty()) {
                holder.codePreviewTextView.text = challenge.codePreview
                holder.codePreviewTextView.visibility = View.VISIBLE
            } else {
                holder.codePreviewTextView.visibility = View.GONE
            }

            val difficultyColor = when (challenge.difficulty) {
                "Easy" -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
                "Medium" -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
            }
            holder.difficultyTextView.setTextColor(difficultyColor)

            val isTaken = challenge.status == "taken"
            holder.itemView.alpha = if (isTaken) 0.7f else 1f
            holder.itemView.setBackgroundColor(
                if (isTaken)
                    ContextCompat.getColor(context, R.color.primary_dark)
                else
                    ContextCompat.getColor(context, R.color.success_green)
            )

            // Animate only if not already animated
            if (holder.itemView.animation == null) {
                val animation = android.view.animation.AnimationUtils.loadAnimation(
                    context,
                    R.anim.fade_slide_up
                )
                holder.itemView.startAnimation(animation)
            }

            holder.itemView.setOnClickListener {
                if (isTaken) {
                    AlertDialog.Builder(context)
                        .setTitle("Assessment Already Taken")
                        .setMessage("This assessment is already taken. Do you want to retry?")
                        .setPositiveButton("Retry") { _, _ ->
                            openCompiler(challenge)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    openCompiler(challenge)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else challenges.size
    }

    fun setChallenges(newChallenges: List<Challenge>) {
        challenges = newChallenges
        isLoading = false
        notifyDataSetChanged()
    }

    fun setChallengesWithDelay(newChallenges: List<Challenge>, delayMillis: Long = 5000) {
        // Keep showing skeleton for specified delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            challenges = newChallenges
            isLoading = false
            notifyDataSetChanged()
        }, delayMillis)
    }

    private fun openCompiler(challenge: Challenge) {
        val intent = Intent(context, CompilerActivity::class.java).apply {
            putExtra("CHALLENGE_TITLE", challenge.title)
            putExtra("CHALLENGE_CODE", challenge.brokenCode)
            putExtra("CORRECT_OUTPUT", challenge.correctOutput)
            putExtra("HINT", challenge.hint)
            putExtra("COURSE_ID", challenge.courseId)
        }
        context.startActivity(intent)
    }

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textAssessmentTitle)
        val difficultyTextView: TextView = itemView.findViewById(R.id.textDifficulty)
        val codePreviewTextView: TextView = itemView.findViewById(R.id.textCodePreview)
    }

    class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val skeletonTitle: View = itemView.findViewById(R.id.skeletonTitle)
        val skeletonDifficulty: View = itemView.findViewById(R.id.skeletonDifficulty)
        val skeletonCodePreview: View = itemView.findViewById(R.id.skeletonCodePreview)
        private var isAnimating = false

        fun startShimmerAnimation(context: Context) {
            // Only start animation once
            if (!isAnimating) {
                isAnimating = true

                val animation = android.view.animation.AnimationUtils.loadAnimation(
                    context,
                    R.anim.shimmer
                )

                skeletonTitle.startAnimation(animation)
                skeletonDifficulty.startAnimation(animation)
                skeletonCodePreview.startAnimation(animation)
            }
        }

        fun stopShimmerAnimation() {
            skeletonTitle.clearAnimation()
            skeletonDifficulty.clearAnimation()
            skeletonCodePreview.clearAnimation()
            isAnimating = false
        }
    }
}