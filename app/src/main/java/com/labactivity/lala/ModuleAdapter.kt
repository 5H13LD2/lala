package com.labactivity.lala

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.labactivity.lala.quiz.DynamicQuizActivity

class ModuleAdapter(
    private val context: Context,
    private val modules: List<Module>,
    private val completedLessonIds: MutableSet<String>,
    private val onLessonCompleted: (String) -> Unit,
    private val onLessonClick: (Lesson) -> Unit = { /* Default empty implementation */ },
    private val onQuizClick: (Module) -> Unit = { module ->
        // Default implementation to launch DynamicQuizActivity
        val intent = Intent(context, DynamicQuizActivity::class.java).apply {
            putExtra("module_id", module.id)
            putExtra("module_title", module.title)
        }
        context.startActivity(intent)
    }
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    private val quizScoreManager = QuizScoreManager(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        holder.bind(module)
    }

    override fun getItemCount(): Int = modules.size

    inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvModuleTitle: TextView = itemView.findViewById(R.id.tvModuleTitle)
        private val tvModuleDescription: TextView = itemView.findViewById(R.id.tvModuleDescription)
        private val moduleHeader: FrameLayout = itemView.findViewById(R.id.moduleHeader)
        private val ivModuleExpand: ImageView = itemView.findViewById(R.id.ivModuleExpand)
        private val rvLessons: RecyclerView = itemView.findViewById(R.id.rvLessons)
        private val moduleProgress: LinearProgressIndicator = itemView.findViewById(R.id.moduleProgress)
        private val btnTakeQuizzes: Button = itemView.findViewById(R.id.btnTakeQuizzes)
        private val tvQuizScore: TextView = itemView.findViewById(R.id.tvQuizScore)

        fun bind(module: Module) {
            // Set module title with score indicator if available
            setModuleTitleWithScoreIndicator(module)
            
            tvModuleDescription.text = module.description

            // Set up progress indicator
            val progressPercentage = module.getProgressPercentage(completedLessonIds)
            Log.d("Module", "Module Progress: $progressPercentage%")
            moduleProgress.progress = progressPercentage

            // Set up lessons RecyclerView
            rvLessons.layoutManager = LinearLayoutManager(context)
            val lessonAdapter = LessonAdapter(
                context, 
                module.lessons, 
                completedLessonIds,
                onLessonClick = onLessonClick, 
                onLessonCompleted = { lessonId ->
                    onLessonCompleted(lessonId)  // Notify parent fragment of the completed lesson
                    notifyItemChanged(adapterPosition)  // Refresh the progress for this module
                    Log.d("Module", "Completed Lessons: $completedLessonIds")
                }
            )
            rvLessons.adapter = lessonAdapter

            // Set up the Take Quiz button
            btnTakeQuizzes.setOnClickListener {
                Log.d("ModuleAdapter", "Quiz button clicked for module: ${module.id} - ${module.title}")
                onQuizClick(module)
            }

            // Initialize expanded state
            updateExpandState(module.isExpanded)

            // Set click listener for expanding/collapsing
            moduleHeader.setOnClickListener {
                module.isExpanded = !module.isExpanded
                updateExpandState(module.isExpanded)
            }
        }

        private fun setModuleTitleWithScoreIndicator(module: Module) {
            // Check if we have a score for this module
            val scorePair = quizScoreManager.getQuizScore(module.id)
            
            if (scorePair != null) {
                val (score, total) = scorePair
                val isPassing = quizScoreManager.isPassing(module.id)
                
                // Create a SpannableString for colored square indicator
                val statusEmoji = if (isPassing) "🟩 " else "🟥 "
                val scoreText = " — ${if (isPassing) "✅" else "❌"} Score: $score/$total"
                
                // Set the module title with status indicator
                tvModuleTitle.text = statusEmoji + module.title + scoreText
                
                // Apply color to the score text
                val textColor = if (isPassing) 
                    ContextCompat.getColor(context, R.color.success_green)
                else 
                    ContextCompat.getColor(context, R.color.error_red)
                
                tvQuizScore.text = "Score: $score/$total"
                tvQuizScore.setTextColor(textColor)
                tvQuizScore.visibility = View.VISIBLE
            } else {
                // No score yet, just show the title
                tvModuleTitle.text = module.title
                tvQuizScore.visibility = View.GONE
            }
        }

        private fun updateExpandState(isExpanded: Boolean) {
            ivModuleExpand.rotation = if (isExpanded) 180f else 0f
            if (isExpanded) {
                rvLessons.visibility = View.VISIBLE
                btnTakeQuizzes.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
                rvLessons.startAnimation(animation)
            } else {
                rvLessons.visibility = View.GONE
                btnTakeQuizzes.visibility = View.GONE
            }
        }
    }
}
