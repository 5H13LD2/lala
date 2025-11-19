package com.labactivity.lala.JAVACOMPILER.adapters

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
import com.labactivity.lala.JAVACOMPILER.JavaChallengeActivity
import com.labactivity.lala.JAVACOMPILER.models.JavaChallenge
import com.labactivity.lala.JAVACOMPILER.models.JavaChallengeProgress
import com.labactivity.lala.UTILS.DialogUtils
import android.os.Handler
import android.os.Looper

/**
 * RecyclerView Adapter for displaying Java Challenges
 * Supports loading states and progress tracking
 */
class JavaChallengeAdapter(
    private val context: Context,
    private var challenges: List<JavaChallenge> = listOf(),
    private var progressMap: Map<String, JavaChallengeProgress> = mapOf(),
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
                .inflate(R.layout.item_assesment_card, parent, false)
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
            holder.descriptionTextView.text = challenge.brokenCode.take(50) + "..." // Show code preview
            holder.difficultyTextView.text = challenge.difficulty

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
                    holder.scoreTextView?.text = "Score: ${progress.bestScore}%"
                    holder.scoreTextView?.visibility = View.VISIBLE
                }
                "in_progress" -> {
                    holder.statusTextView.text = "⟳ In Progress"
                    holder.statusTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.primary_blue)
                    )
                    holder.statusTextView.visibility = View.VISIBLE
                    holder.scoreTextView?.text = "Attempts: ${progress.attempts}"
                    holder.scoreTextView?.visibility = View.VISIBLE
                }
                else -> {
                    holder.statusTextView.visibility = View.GONE
                    holder.scoreTextView?.visibility = View.GONE
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
                        "medium" -> "Complete all Easy Java challenges to unlock Medium difficulty."
                        "hard" -> "Complete all Easy and Medium Java challenges to unlock Hard difficulty."
                        else -> "This Java challenge is currently locked."
                    }
                    DialogUtils.showLockedDialog(
                        context = context,
                        title = "🔒 Java Challenge Locked",
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
    fun setChallenges(newChallenges: List<JavaChallenge>) {
        challenges = newChallenges
        isLoading = false
        notifyDataSetChanged()
    }

    /**
     * Updates challenges with progress tracking
     */
    fun setChallengesWithProgress(
        newChallenges: List<JavaChallenge>,
        newProgressMap: Map<String, JavaChallengeProgress>
    ) {
        challenges = newChallenges
        progressMap = newProgressMap
        isLoading = false
        notifyDataSetChanged()
    }

    /**
     * Updates challenges with progress and shows skeleton for a minimum duration
     */
    fun setChallengesWithProgressAndDelay(
        newChallenges: List<JavaChallenge>,
        newProgressMap: Map<String, JavaChallengeProgress>,
        delayMillis: Long = 5000
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            setChallengesWithProgress(newChallenges, newProgressMap)
        }, delayMillis)
    }

    /**
     * Opens the Java challenge activity
     */
    private fun openChallenge(challenge: JavaChallenge) {
        val intent = Intent(context, JavaChallengeActivity::class.java).apply {
            putExtra("CHALLENGE_ID", challenge.id)
            putExtra("CHALLENGE_TITLE", challenge.title)
            putExtra("CHALLENGE_DIFFICULTY", challenge.difficulty)
        }
        context.startActivity(intent)
    }

    /**
     * Shows dialog asking if user wants to retry a completed challenge
     */
    private fun showRetryDialog(challenge: JavaChallenge) {
        AlertDialog.Builder(context)
            .setTitle("Challenge Completed")
            .setMessage("You've already completed this challenge. Do you want to retry it?")
            .setPositiveButton("Retry") { dialog, _ ->
                openChallenge(challenge)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * ViewHolder for skeleton loading state
     */
    class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmerAnimation(context: Context) {
            // Shimmer effect is already in the layout's background
        }
    }

    /**
     * ViewHolder for actual challenge items
     */
    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView as CardView
        val titleTextView: TextView = itemView.findViewById(R.id.textAssessmentTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textCodePreview)
        val difficultyTextView: TextView = itemView.findViewById(R.id.textDifficulty)
        val statusTextView: TextView = itemView.findViewById(R.id.textStatus)
        val categoryTextView: TextView? = null // Not in this layout
        val scoreTextView: TextView? = itemView.findViewById(R.id.textScore)
        val lockOverlay: View? = itemView.findViewById(R.id.lockOverlay)
        val lockIcon: ImageView? = itemView.findViewById(R.id.lockIcon)
    }
}
