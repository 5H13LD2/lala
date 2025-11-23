package com.labactivity.lala.SQLCOMPILER.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R
import com.labactivity.lala.UNIFIEDCOMPILER.ui.UnifiedCompilerActivity
import com.labactivity.lala.SQLCOMPILER.models.SQLChallenge
import com.labactivity.lala.SQLCOMPILER.models.SQLChallengeProgress
import com.labactivity.lala.UTILS.DialogUtils

/**
 * RecyclerView Adapter for displaying SQL Challenges
 * Supports loading states and progress tracking
 */
class SQLChallengeAdapter(
    private val context: Context,
    private var challenges: List<SQLChallenge> = listOf(),
    private var progressMap: Map<String, SQLChallengeProgress> = mapOf(),
    var isLoading: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_ITEM = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_LOADING) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.item_sql_challenge_skeleton, parent, false)
            SkeletonViewHolder(view)
        } else {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.item_sql_challenge, parent, false)
            ChallengeViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SkeletonViewHolder) {
            // Start shimmer animation
            holder.startShimmerAnimation(context)
        } else if (holder is ChallengeViewHolder && !isLoading) {
            val challenge = challenges[position]
            val progress = progressMap[challenge.id]

            // Bind challenge data
            holder.titleTextView.text = challenge.title
            holder.descriptionTextView.text = challenge.descriptionPreview
            holder.difficultyTextView.text = challenge.difficulty
            holder.topicTextView.text = challenge.topic

            // Set difficulty color
            holder.difficultyTextView.setTextColor(Color.parseColor(challenge.difficultyColor))

            // Show progress indicator if challenge is completed or in progress
            when (progress?.status) {
                "completed" -> {
                    holder.statusTextView.text = "✓ Completed"
                    holder.statusTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.success_green)
                    )
                    holder.statusTextView.visibility = View.VISIBLE
                    holder.scoreTextView.text = "Best: ${progress.bestScore}%"
                    holder.scoreTextView.visibility = View.VISIBLE
                }
                "in_progress" -> {
                    holder.statusTextView.text = "⟳ In Progress"
                    holder.statusTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.primary_blue)
                    )
                    holder.statusTextView.visibility = View.VISIBLE
                    holder.scoreTextView.text = "Attempts: ${progress.attempts}"
                    holder.scoreTextView.visibility = View.VISIBLE
                }
                else -> {
                    holder.statusTextView.visibility = View.GONE
                    holder.scoreTextView.visibility = View.GONE
                }
            }

            // Set card background based on completion status
            val isCompleted = progress?.status == "completed"
            holder.cardView.alpha = if (isCompleted) 0.85f else 1f

            // Show/hide lock overlay for locked challenges
            if (!challenge.isUnlocked) {
                holder.lockOverlay?.visibility = View.VISIBLE
                holder.lockIcon?.visibility = View.VISIBLE
                holder.cardView.alpha = 0.6f
            } else {
                holder.lockOverlay?.visibility = View.GONE
                holder.lockIcon?.visibility = View.GONE
            }

            // Animate card entrance
            if (holder.itemView.animation == null) {
                val animation = android.view.animation.AnimationUtils.loadAnimation(
                    context,
                    R.anim.fade_slide_up
                )
                holder.itemView.startAnimation(animation)
            }

            // Set click listener
            holder.itemView.setOnClickListener {
                // Check if challenge is locked
                if (!challenge.isUnlocked) {
                    val message = when (challenge.difficulty.lowercase()) {
                        "medium" -> "Complete all Easy SQL challenges to unlock Medium difficulty."
                        "hard" -> "Complete all Easy and Medium SQL challenges to unlock Hard difficulty."
                        else -> "This SQL challenge is currently locked."
                    }
                    DialogUtils.showLockedDialog(
                        context = context,
                        title = "🔒 SQL Challenge Locked",
                        message = message
                    )
                    return@setOnClickListener
                }

                if (isCompleted && progress?.passed == true) {
                    showRetryDialog(challenge)
                } else {
                    openChallenge(challenge)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 6 else challenges.size
    }

    /**
     * Updates the adapter with new challenges
     */
    fun setChallenges(newChallenges: List<SQLChallenge>) {
        challenges = newChallenges
        isLoading = false
        notifyDataSetChanged()
    }

    /**
     * Updates the adapter with new challenges and progress data
     */
    fun setChallengesWithProgress(
        newChallenges: List<SQLChallenge>,
        newProgressMap: Map<String, SQLChallengeProgress>
    ) {
        challenges = newChallenges
        progressMap = newProgressMap
        isLoading = false
        notifyDataSetChanged()
    }

    /**
     * Updates only the progress data without changing challenges
     */
    fun updateProgress(newProgressMap: Map<String, SQLChallengeProgress>) {
        progressMap = newProgressMap
        notifyDataSetChanged()
    }

    /**
     * Updates the adapter with new challenges and progress after a delay
     * Mirrors PYTHONASSESMENT.TechnicalAssessmentAdapter behavior
     */
    fun setChallengesWithProgressAndDelay(
        newChallenges: List<SQLChallenge>,
        newProgressMap: Map<String, SQLChallengeProgress>,
        delayMillis: Long = 5000
    ) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            challenges = newChallenges
            progressMap = newProgressMap
            isLoading = false
            notifyDataSetChanged()
        }, delayMillis)
    }

    /**
     * Shows a dialog asking if user wants to retry a completed challenge
     */
    private fun showRetryDialog(challenge: SQLChallenge) {
        AlertDialog.Builder(context)
            .setTitle("Challenge Completed")
            .setMessage("You've already completed this challenge. Do you want to try again to improve your score?")
            .setPositiveButton("Retry") { _, _ ->
                openChallenge(challenge)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Opens the SQL Challenge Activity
     */
    private fun openChallenge(challenge: SQLChallenge) {
        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
            putExtra("CHALLENGE_ID", challenge.id)
            putExtra("COURSE_ID", "sql")
        }
        context.startActivity(intent)
    }

    /**
     * Filters challenges by difficulty
     */
    fun filterByDifficulty(difficulty: String?) {
        if (difficulty == null || difficulty == "All") {
            notifyDataSetChanged()
        } else {
            // This would require storing the original list
            // For now, the filtering should be done at the Activity level
            notifyDataSetChanged()
        }
    }

    /**
     * ViewHolder for challenge items
     */
    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.challengeCard)
        val titleTextView: TextView = itemView.findViewById(R.id.textChallengeTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textChallengeDescription)
        val difficultyTextView: TextView = itemView.findViewById(R.id.textDifficulty)
        val topicTextView: TextView = itemView.findViewById(R.id.textTopic)
        val statusTextView: TextView = itemView.findViewById(R.id.textStatus)
        val scoreTextView: TextView = itemView.findViewById(R.id.textScore)
        val lockOverlay: View? = itemView.findViewById(R.id.lockOverlay)
        val lockIcon: ImageView? = itemView.findViewById(R.id.lockIcon)
    }

    /**
     * ViewHolder for skeleton loading state
     */
    class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val skeletonViews: List<View> = listOf(
            itemView.findViewById(R.id.skeletonTitle),
            itemView.findViewById(R.id.skeletonDescription),
            itemView.findViewById(R.id.skeletonDifficulty),
            itemView.findViewById(R.id.skeletonTopic)
        )
        private var isAnimating = false

        fun startShimmerAnimation(context: Context) {
            if (!isAnimating) {
                isAnimating = true

                val animation = android.view.animation.AnimationUtils.loadAnimation(
                    context,
                    R.anim.shimmer
                )

                skeletonViews.forEach { view ->
                    view.startAnimation(animation)
                }
            }
        }

        fun stopShimmerAnimation() {
            skeletonViews.forEach { view ->
                view.clearAnimation()
            }
            isAnimating = false
        }
    }
}
