package com.labactivity.lala.LEARNINGMATERIAL

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.labactivity.lala.R
import com.labactivity.lala.quiz.DynamicQuizActivity
import com.labactivity.lala.quiz.QuizScoreManager
import com.labactivity.lala.UTILS.DialogUtils

class ModuleAdapter(
    private val context: Context,
    private val modules: List<Module>,
    private val completedLessonIds: MutableSet<String>,
    private val onLessonCompleted: (lessonId: String, moduleId: String, isCompleted: Boolean) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    private val TAG = "ModuleAdapter"
    private val quizScoreManager = QuizScoreManager(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        holder.bind(module)

        // 🔹 Cascading animation
        holder.itemView.alpha = 0f
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_slide_up)
        animation.startOffset = (position * 100).toLong() // delay per item (100ms per module)
        holder.itemView.startAnimation(animation)
        holder.itemView.alpha = 1f
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

        private var currentModule: Module? = null
        private var lessonAdapter: LessonAdapter? = null
        private var clickListenerSet = false

        init {
            // Set click listeners only once in init to avoid multiple listeners
            moduleHeader.setOnClickListener {
                currentModule?.let { module ->
                    module.isExpanded = !module.isExpanded
                    updateExpandState(module.isExpanded)
                    Log.d(TAG, "Module ${module.id} expanded: ${module.isExpanded}")
                }
            }

            btnTakeQuizzes.setOnClickListener {
                currentModule?.let { module ->
                    try {
                        Log.d(TAG, "═══ QUIZ BUTTON CLICKED ═══")
                        Log.d(TAG, "Module ID: ${module.id}")
                        Log.d(TAG, "Module Title: ${module.title}")
                        Log.d(TAG, "ID parts: ${module.id.split("_")}")
                        Log.d(TAG, "Is valid: ${module.isValidModuleId()}")

                        if (!module.isValidModuleId()) {
                            Log.e(TAG, "Invalid module ID format: ${module.id}")
                            Log.e(TAG, "Expected format: coursename_module_number (e.g., python_module_1)")
                            DialogUtils.showErrorDialog(context, "Invalid Format", "Invalid module format: ${module.id}")
                            return@setOnClickListener
                        }

                        // Extract quiz ID from module ID (e.g., "sql_module_1" -> "sql_quiz")
                        val quizId = "${module.id.split("_").firstOrNull()}_quiz"

                        Log.d(TAG, "Starting quiz:")
                        Log.d(TAG, "  Module ID: ${module.id}")
                        Log.d(TAG, "  Quiz ID: $quizId")
                        Log.d(TAG, "  Title: ${module.title}")

                        val intent = Intent(context, DynamicQuizActivity::class.java).apply {
                            putExtra("module_id", module.id.trim())
                            putExtra("module_title", module.title)
                            putExtra("quiz_id", quizId)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error launching quiz for module ${module.id}", e)
                        DialogUtils.showErrorDialog(context, "Error", "Unable to start quiz")
                    }
                }
            }
        }

        fun bind(module: Module) {
            currentModule = module

            setModuleTitleWithScoreIndicator(module)
            tvModuleDescription.text = module.description

            // Calculate and update progress
            updateModuleProgress(module)

            // Always recreate the lesson adapter with the current module
            setupLessonsRecyclerView(module)

            // Expanded state
            updateExpandState(module.isExpanded)
        }

        private fun updateModuleProgress(module: Module) {
            try {
                val progress = calculateProgress(module)
                moduleProgress.progress = progress
                Log.d(TAG, "Module ${module.id} progress: $progress%")
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating progress for module ${module.id}", e)
                moduleProgress.progress = 0
            }
        }

        private fun calculateProgress(module: Module): Int {
            if (module.lessons.isEmpty()) return 0
            val completed = module.lessons.count { lesson -> 
                completedLessonIds.contains(lesson.id)
            }
            return (completed * 100) / module.lessons.size
        }

        private fun setupLessonsRecyclerView(module: Module) {
            rvLessons.layoutManager = LinearLayoutManager(context)
            lessonAdapter = LessonAdapter(
                context,
                module.lessons,
                completedLessonIds,
                module.id,
                onLessonClick = { /* Handle lesson click if needed */ },
                onLessonCompleted = { lessonId, isCompleted ->
                    onLessonCompleted(lessonId, module.id, isCompleted)
                    updateModuleProgress(module)
                }
            )
            rvLessons.adapter = lessonAdapter
        }

        private fun setModuleTitleWithScoreIndicator(module: Module) {
            try {
                // Use the new versioned system: get latest score from summary
                // This ensures we only show scores from actual completed quizzes in Firestore
                quizScoreManager.getAllQuizSummariesFromFirestore { summaries ->
                    // Find the summary for this module
                    val summary = summaries.firstOrNull { it.quizId == module.id }

                    if (summary != null && summary.totalAttempts > 0) {
                        // Quiz has been completed at least once
                        val score = summary.latestScore
                        val total = summary.latestTotal
                        val isPassing = summary.latestPassed
                        val statusEmoji = if (isPassing) "🟩 " else "🟥 "

                        tvModuleTitle.text = statusEmoji + module.title
                        tvQuizScore.text = "Score: $score/$total (${summary.totalAttempts} attempt${if (summary.totalAttempts > 1) "s" else ""})"
                        tvQuizScore.setTextColor(
                            if (isPassing) ContextCompat.getColor(context, R.color.success_green)
                            else ContextCompat.getColor(context, R.color.error_red)
                        )
                        tvQuizScore.visibility = View.VISIBLE

                        Log.d(TAG, "Showing score for ${module.id}: $score/$total (${summary.totalAttempts} attempts)")
                    } else {
                        // No quiz taken yet - hide score
                        tvModuleTitle.text = module.title
                        tvQuizScore.visibility = View.GONE
                        Log.d(TAG, "No score found for ${module.id} in Firestore")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting module title with score", e)
                tvModuleTitle.text = module.title
                tvQuizScore.visibility = View.GONE
            }
        }

        private fun updateExpandState(isExpanded: Boolean) {
            try {
                ivModuleExpand.rotation = if (isExpanded) 180f else 0f
                rvLessons.visibility = if (isExpanded) View.VISIBLE else View.GONE
                btnTakeQuizzes.visibility = if (isExpanded) View.VISIBLE else View.GONE
                
                if (isExpanded) {
                    rvLessons.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.slide_down)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating expand state", e)
                // Fallback to basic visibility changes
                rvLessons.visibility = if (isExpanded) View.VISIBLE else View.GONE
                btnTakeQuizzes.visibility = if (isExpanded) View.VISIBLE else View.GONE
            }
        }
    }
}
