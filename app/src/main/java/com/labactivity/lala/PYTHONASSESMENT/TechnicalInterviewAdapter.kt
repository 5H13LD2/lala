package com.labactivity.lala.PYTHONASSESMENT

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.FLASHCARD.FlashcardTopic
import com.labactivity.lala.R
import com.labactivity.lala.TECHNICALREVIEWER.TechnicalFlashcardActivity

class TechnicalInterviewAdapter(
    private val context: Context,
    private val topics: List<FlashcardTopic>
) : RecyclerView.Adapter<TechnicalInterviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardViewTopic)
        val titleTextView: TextView = view.findViewById(R.id.textTopicTitle)
        val difficultyTextView: TextView = view.findViewById(R.id.textTopicDifficulty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technical_interview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val topic = topics[position]

        holder.titleTextView.text = topic.title
        holder.difficultyTextView.text = topic.difficulty

        holder.cardView.setOnClickListener {
            // Launch the flashcard activity when clicked
            val intent = Intent(context, TechnicalFlashcardActivity::class.java).apply {
                putExtra("TOPIC_TITLE", topic.title)
                putExtra("TOPIC_ID", topic.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = topics.size
}