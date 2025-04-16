package com.labactivity.lala

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator


class CourseFragment : Fragment() {

    private lateinit var rvModules: RecyclerView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var moduleAdapter: ModuleAdapter

    private val completedLessonIds = mutableSetOf<String>()
    private lateinit var course: Courses

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        rvModules = view.findViewById(R.id.rvModules)
        progressIndicator = view.findViewById(R.id.progressIndicator)
        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
        val tvCourseDescription: TextView = view.findViewById(R.id.tvCourseDescription)

        // Load saved completed lessons
        loadCompletedLessons()

        // Initialize course data
        course = createDummyCourse()

        // Set up course info
        toolbar.title = course.title
        tvCourseDescription.text = course.description

        // Set up RecyclerView
        rvModules.layoutManager = LinearLayoutManager(requireContext())
        moduleAdapter = ModuleAdapter(
            requireContext(),
            course.modules,
            completedLessonIds
        ) { lessonId ->
            completedLessonIds.add(lessonId)
            saveCompletedLessons()
            updateCoursesProgress()
        }
        rvModules.adapter = moduleAdapter

        // Initialize course progress
        updateCoursesProgress()
    }

    private fun updateCoursesProgress() {
        val totalLessons = course.modules.sumOf { it.lessons.size }
        val completedCount = completedLessonIds.size
        val progressPercentage = if (totalLessons > 0) (completedCount.toInt() * 100) / totalLessons else 0
        progressIndicator.progress = progressPercentage
    }

    private fun loadCompletedLessons() {
        val sharedPrefs = requireContext().getSharedPreferences("course_prefs", Context.MODE_PRIVATE)
        completedLessonIds.clear()
        completedLessonIds.addAll(
            sharedPrefs.getStringSet("completed_lessons", emptySet()) ?: emptySet()
        )
    }

    private fun saveCompletedLessons() {
        val sharedPrefs = requireContext().getSharedPreferences("course_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putStringSet("completed_lessons", completedLessonIds).apply()
    }

    private fun createDummyCourse(): Courses {
        return Courses(
            id = "python_101",
            title = "Python Course",
            description = "Master Python programming with our comprehensive course. Learn at your own pace through interactive modules and practical examples.",
            modules = listOf(
                Module(
                    id = "module_1",
                    title = "Module 1: Fundamentals of Python",
                    description = "Learn the basics of Python programming language",
                    lessons = listOf(
                        Lesson(
                            id = "lesson_1_1",
                            moduleId = "module_1",
                            number = "1.1",
                            title = "Introduction to Python",
                            explanation = "...",
                            codeExample = "...",
                            videoUrl = "..."
                        ),
                        Lesson(
                            id = "lesson_1_2",
                            moduleId = "module_1",
                            number = "1.2",
                            title = "Variables and Data Types",
                            explanation = "...",
                            codeExample = "...",
                            videoUrl = "..."
                        )
                    ),
                    isExpanded = false
                ),
                Module(
                    id = "module_2",
                    title = "Module 2: Control Flow",
                    description = "Master conditionals, loops, and flow control in Python",
                    lessons = listOf(
                        Lesson(
                            id = "lesson_2_1",
                            moduleId = "module_2",
                            number = "2.1",
                            title = "Conditional Statements",
                            explanation = "...",
                            codeExample = "...",
                            videoUrl = "..."
                        )
                    ),
                    isExpanded = false
                )
            )
        )
    }

}
