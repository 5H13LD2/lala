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
    private val challenges: List<Challenge>
) : RecyclerView.Adapter<TechnicalAssessmentAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_assesment_card, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]

        holder.titleTextView.text = challenge.title
        holder.difficultyTextView.text = challenge.difficulty

        // Show preview line
        if (challenge.codePreview.isNotEmpty()) {
            holder.codePreviewTextView.text = challenge.codePreview
            holder.codePreviewTextView.visibility = View.VISIBLE
        } else {
            holder.codePreviewTextView.visibility = View.GONE
        }

        // Color by difficulty
        val difficultyColor = when (challenge.difficulty) {
            "Easy" -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
            "Medium" -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            else -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
        }
        holder.difficultyTextView.setTextColor(difficultyColor)

        // 🔹 Change color if already taken
        // 🔹 Change color if already taken
        val isTaken = challenge.status == "taken"
        if (isTaken) {
            holder.itemView.alpha = 0.7f
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.primary_dark) // darker tone
            )
        } else {
            holder.itemView.alpha = 1f
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.success_green) // available/active color
            )
        }


        // 🔹 Click behavior
        holder.itemView.setOnClickListener {
            if (isTaken) {
                // Show dialog if assessment is already taken
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

    override fun getItemCount(): Int = challenges.size

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
}
