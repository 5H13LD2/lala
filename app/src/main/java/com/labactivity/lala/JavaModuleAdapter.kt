package com.labactivity.lala

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class JavaModuleAdapter(
    private val context: Context,
    private val modules: List<Module>,
    private val completedLessonIds: MutableSet<String>,
    private val onLessonCompleted: (String) -> Unit
) : RecyclerView.Adapter<JavaModuleAdapter.ModuleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.java_itemmodule, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        holder.bind(module)
    }

    override fun getItemCount(): Int = modules.size

    inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvModuleTitle: TextView = itemView.findViewById(R.id.javaTvModuleTitle)
        private val tvModuleDescription: TextView = itemView.findViewById(R.id.javaTvModuleDescription)
        private val moduleHeader: FrameLayout = itemView.findViewById(R.id.javaModuleHeader)
        private val ivModuleExpand: ImageView = itemView.findViewById(R.id.javaIvModuleExpand)
        private val rvLessons: RecyclerView = itemView.findViewById(R.id.javaRvLessons)
        private val moduleProgress: LinearProgressIndicator = itemView.findViewById(R.id.javaModuleProgress)

        fun bind(module: Module) {
            tvModuleTitle.text = module.title
            tvModuleDescription.text = module.description

            val progressPercentage = module.getProgressPercentage(completedLessonIds)
            Log.d("JavaModule", "Module Progress: $progressPercentage%")
            moduleProgress.progress = progressPercentage

            rvLessons.layoutManager = LinearLayoutManager(context)
            val lessonAdapter = LessonAdapter(context, module.lessons, completedLessonIds) { lessonId ->
                onLessonCompleted(lessonId)
                notifyItemChanged(adapterPosition)
                Log.d("JavaModule", "Completed Lessons: $completedLessonIds")
            }
            rvLessons.adapter = lessonAdapter

            updateExpandState(module.isExpanded)

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
