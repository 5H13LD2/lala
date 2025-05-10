// 2. Now, let's update your TechnicalAssessmentAdapter
package com.labactivity.lala

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TechnicalAssessmentAdapter(
    private val context: Context,
    private val challenges: List<Challenge>
) : RecyclerView.Adapter<TechnicalAssessmentAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_assesment_card, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]

        holder.titleTextView.text = challenge.title
        holder.difficultyTextView.text = challenge.difficulty

        // Only set code preview if it exists
        if (challenge.codePreview.isNotEmpty()) {
            holder.codePreviewTextView.text = challenge.codePreview
            holder.codePreviewTextView.visibility = View.VISIBLE
        } else {
            holder.codePreviewTextView.visibility = View.GONE
        }

        // Set color based on difficulty
        val difficultyColor = when (challenge.difficulty) {
            "Easy" -> context.getColor(android.R.color.holo_green_dark)
            "Medium" -> context.getColor(android.R.color.holo_orange_dark)
            else -> context.getColor(android.R.color.holo_red_dark)
        }
        holder.difficultyTextView.setTextColor(difficultyColor)

        // Set click listener to navigate to the compiler activity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, CompilerActivity::class.java).apply {
                putExtra("CHALLENGE_ID", challenge.id)
                putExtra("CHALLENGE_TITLE", challenge.title)
                putExtra("CHALLENGE_CODE", challenge.brokenCode)
                putExtra("CORRECT_OUTPUT", challenge.correctOutput)
                putExtra("HINT", challenge.hint)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textAssessmentTitle)
        val difficultyTextView: TextView = itemView.findViewById(R.id.textDifficulty)
        val codePreviewTextView: TextView = itemView.findViewById(R.id.textCodePreview)
    }
}
