package com.labactivity.lala.PYTHONASSESMENT

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.UNIFIEDCOMPILER.ui.UnifiedCompilerActivity
import com.labactivity.lala.R
import com.labactivity.lala.UTILS.DialogUtils

class TechnicalAssessmentAdapter(
    private val context: Context,
    private var challenges: List<Challenge> = listOf(),
    private var progressMap: Map<String, TechnicalAssessmentProgress> = mapOf(),
    var isLoading: Boolean = true  // Changed to 'var' and made public for AllAssessmentsActivity
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
            val progress = progressMap[challenge.id]

            holder.titleTextView.text = challenge.title
            holder.difficultyTextView.text = challenge.difficulty

            if (challenge.codePreview.isNotEmpty()) {
                holder.codePreviewTextView.text = challenge.codePreview
                holder.codePreviewTextView.visibility = View.VISIBLE
            } else {
                holder.codePreviewTextView.visibility = View.GONE
            }

            val difficultyColor = when (challenge.difficulty) {
                "Easy" -> ContextCompat.getColor(context, android.R.color.white)
                "Medium" -> ContextCompat.getColor(context, android.R.color.white)
                else -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
            }
            holder.difficultyTextView.setTextColor(difficultyColor)

            // Show progress indicator if challenge is completed or in progress
            when (progress?.status) {
                "completed" -> {
                    holder.statusTextView?.text = "✓ Completed"
                    holder.statusTextView?.setTextColor(
                        ContextCompat.getColor(context, R.color.success_green)
                    )
                    holder.statusTextView?.visibility = View.VISIBLE
                    holder.scoreTextView?.text = "Score: ${progress.bestScore}%"
                    holder.scoreTextView?.visibility = View.VISIBLE
                }
                "in_progress" -> {
                    holder.statusTextView?.text = "⟳ In Progress"
                    holder.statusTextView?.setTextColor(
                        ContextCompat.getColor(context, R.color.primary_blue)
                    )
                    holder.statusTextView?.visibility = View.VISIBLE
                    holder.scoreTextView?.text = "Attempts: ${progress.attempts}"
                    holder.scoreTextView?.visibility = View.VISIBLE
                }
                else -> {
                    holder.statusTextView?.visibility = View.GONE
                    holder.scoreTextView?.visibility = View.GONE
                }
            }

            // Set card appearance based on completion status
            val isCompleted = progress?.status == "completed"
            holder.itemView.alpha = if (isCompleted) 0.85f else 1f

            // Show/hide lock overlay for locked challenges
            if (!challenge.isUnlocked) {
                holder.lockOverlay?.visibility = View.VISIBLE
                holder.lockIcon?.visibility = View.VISIBLE
                holder.itemView.alpha = 0.6f
            } else {
                holder.lockOverlay?.visibility = View.GONE
                holder.lockIcon?.visibility = View.GONE
            }

            // Animate only if not already animated
            if (holder.itemView.animation == null) {
                val animation = android.view.animation.AnimationUtils.loadAnimation(
                    context,
                    R.anim.fade_slide_up
                )
                holder.itemView.startAnimation(animation)
            }

            holder.itemView.setOnClickListener {
                // Check if challenge is locked
                if (!challenge.isUnlocked) {
                    val message = when (challenge.difficulty.lowercase()) {
                        "medium" -> "Complete all Easy challenges to unlock Medium difficulty."
                        "hard" -> "Complete all Easy and Medium challenges to unlock Hard difficulty."
                        else -> "This challenge is currently locked."
                    }
                    DialogUtils.showLockedDialog(
                        context = context,
                        title = "🔒 Challenge Locked",
                        message = message
                    )
                    return@setOnClickListener
                }

                if (isCompleted && progress?.passed == true) {
                    AlertDialog.Builder(context)
                        .setTitle("Assessment Completed")
                        .setMessage("You've already completed this assessment with a score of ${progress.bestScore}%. Do you want to retry?")
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

    /**
     * Updates the adapter with new challenges and progress data
     */
    fun setChallengesWithProgress(
        newChallenges: List<Challenge>,
        newProgressMap: Map<String, TechnicalAssessmentProgress>
    ) {
        challenges = newChallenges
        progressMap = newProgressMap
        isLoading = false
        notifyDataSetChanged()
    }

    /**
     * Updates only the progress data without changing challenges
     */
    fun updateProgress(newProgressMap: Map<String, TechnicalAssessmentProgress>) {
        progressMap = newProgressMap
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

    /**
     * Updates the adapter with new challenges and progress after a delay
     */
    fun setChallengesWithProgressAndDelay(
        newChallenges: List<Challenge>,
        newProgressMap: Map<String, TechnicalAssessmentProgress>,
        delayMillis: Long = 5000
    ) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            challenges = newChallenges
            progressMap = newProgressMap
            isLoading = false
            notifyDataSetChanged()
        }, delayMillis)
    }

    private fun openCompiler(challenge: Challenge) {
        // Ensure compiler type is not empty, default to "python"
        // Normalize: trim and lowercase to match CompilerFactory registry
        val compilerType = challenge.compilerType
            .trim()
            .lowercase()
            .takeIf { it.isNotEmpty() } ?: "python"

        Log.d("TechnicalAssessmentAdapter", "Opening compiler for challenge: ${challenge.title}")
        Log.d("TechnicalAssessmentAdapter", "Compiler type from challenge: '${challenge.compilerType}'")
        Log.d("TechnicalAssessmentAdapter", "Final compiler type: '$compilerType'")

        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
            // UnifiedCompilerActivity expected extras
            putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, compilerType)
            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, challenge.courseId)
            putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, challenge.brokenCode)

            // Additional challenge data for validation
            putExtra("CHALLENGE_ID", challenge.id)
            putExtra("CHALLENGE_TITLE", challenge.title)
            putExtra("CORRECT_OUTPUT", challenge.correctOutput)
            putExtra("HINT", challenge.hint)
        }
        context.startActivity(intent)
    }

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textAssessmentTitle)
        val difficultyTextView: TextView = itemView.findViewById(R.id.textDifficulty)
        val codePreviewTextView: TextView = itemView.findViewById(R.id.textCodePreview)
        val statusTextView: TextView? = itemView.findViewById(R.id.textStatus)
        val scoreTextView: TextView? = itemView.findViewById(R.id.textScore)
        val lockOverlay: View? = itemView.findViewById(R.id.lockOverlay)
        val lockIcon: ImageView? = itemView.findViewById(R.id.lockIcon)
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