package com.labactivity.lala

import android.content.Context
import android.view.LayoutInflater
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class ModuleAdapter(
    private val context: Context,
    private val modules: List<Module>,
    private val completedLessonIds: MutableSet<String>, // ✅ changed to MutableSet
    private val onLessonCompleted: (String) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

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

        fun bind(module: Module) {
            tvModuleTitle.text = module.title
            tvModuleDescription.text = module.description

            // Set up progress indicator
            val progressPercentage = module.getProgressPercentage(completedLessonIds)
            Log.d("Module", "Module Progress: $progressPercentage%")
            moduleProgress.progress = progressPercentage


            // Set up lessons RecyclerView
            rvLessons.layoutManager = LinearLayoutManager(context)
            val lessonAdapter = LessonAdapter(context, module.lessons, completedLessonIds) { lessonId ->
                onLessonCompleted(lessonId)
                notifyItemChanged(adapterPosition) // Update progress indicator
                Log.d("Module", "Completed Lessons: $completedLessonIds")
            }
            rvLessons.adapter = lessonAdapter

            // Initialize expanded state
            updateExpandState(module.isExpanded)

            // Set click listener for expanding/collapsing
            moduleHeader.setOnClickListener {
                module.isExpanded = !module.isExpanded
                updateExpandState(module.isExpanded)
            }
        }

        private fun updateExpandState(isExpanded: Boolean) {
            ivModuleExpand.rotation = if (isExpanded) 180f else 0f
            if (isExpanded) {
                rvLessons.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
                rvLessons.startAnimation(animation)
            } else {
                rvLessons.visibility = View.GONE
            }
        }
    }
}
