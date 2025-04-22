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

class SqlModuleAdapter(
    private val context: Context,
    private val modules: List<Module>,
    private val completedLessonIds: MutableSet<String>, // ✅ changed to MutableSet
    private val onLessonCompleted: (String) -> Unit
) : RecyclerView.Adapter<SqlModuleAdapter.ModuleViewHolder>() { // Use SqlModuleAdapter.ModuleViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.sql_itemmodule, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        holder.bind(module)
    }

    override fun getItemCount(): Int = modules.size

    inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSqlModuleTitle: TextView = itemView.findViewById(R.id.sqlTvModuleTitle)  // Changed to sqlTvModuleTitle
        private val tvSqlModuleDescription: TextView = itemView.findViewById(R.id.sqlTvModuleDescription)  // Changed to sqlTvModuleDescription
        private val sqlModuleHeader: FrameLayout = itemView.findViewById(R.id.sqlModuleHeader)  // Changed to sqlModuleHeader
        private val ivSqlModuleExpand: ImageView = itemView.findViewById(R.id.sqlIvModuleExpand)  // Changed to sqlIvModuleExpand
        private val rvSqlLessons: RecyclerView = itemView.findViewById(R.id.sqlRvLessons)  // Changed to sqlRvLessons
        private val sqlModuleProgress: LinearProgressIndicator = itemView.findViewById(R.id.sqlModuleProgress)  // Changed to sqlModuleProgress

        fun bind(module: Module) {
            tvSqlModuleTitle.text = module.title
            tvSqlModuleDescription.text = module.description

            // Set up progress indicator
            val progressPercentage = module.getProgressPercentage(completedLessonIds)
            Log.d("Module", "Module Progress: $progressPercentage%")
            sqlModuleProgress.progress = progressPercentage

            // Set up lessons RecyclerView
            rvSqlLessons.layoutManager = LinearLayoutManager(context)
            val lessonAdapter = LessonAdapter(context, module.lessons, completedLessonIds) { lessonId ->
                onLessonCompleted(lessonId)
                notifyItemChanged(adapterPosition) // Update progress indicator
                Log.d("Module", "Completed Lessons: $completedLessonIds")
            }
            rvSqlLessons.adapter = lessonAdapter

            // Initialize expanded state
            updateExpandState(module.isExpanded)

            // Set click listener for expanding/collapsing
            sqlModuleHeader.setOnClickListener {
                module.isExpanded = !module.isExpanded
                updateExpandState(module.isExpanded)
            }
        }

        private fun updateExpandState(isExpanded: Boolean) {
            ivSqlModuleExpand.rotation = if (isExpanded) 180f else 0f
            if (isExpanded) {
                rvSqlLessons.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
                rvSqlLessons.startAnimation(animation)
            } else {
                rvSqlLessons.visibility = View.GONE
            }
        }
    }
}
