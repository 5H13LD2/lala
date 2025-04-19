package com.labactivity.lala

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator


class CourseFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rvModules: RecyclerView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var moduleAdapter: ModuleAdapter

    private val completedLessonIds = mutableSetOf<String>()
    private lateinit var course: Courses

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize sharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("course_preferences", Context.MODE_PRIVATE)

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
            saveCompletedLessons()  // Save the updated completed lessons
            updateCoursesProgress() // Update the course progress UI
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
        val completedLessons = sharedPreferences.getStringSet("completed_lessons", emptySet())
        Log.d("CourseFragment", "Completed Lessons Loaded: ${completedLessons?.size}")
        completedLessonIds.addAll(completedLessons ?: emptySet())
    }

    private fun saveCompletedLessons() {
        sharedPreferences.edit().putStringSet("completed_lessons", completedLessonIds).apply()
        Log.d("CourseFragment", "Completed Lessons Saved: ${completedLessonIds.size}")
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
                        Lesson("lesson_1_1", "module_1", "1.1", "Introduction to Python", "Get started with Python and its uses.", "print(\"Hello, World!\")", "https://youtu.be/kqtD5dpn9C8"),
                        Lesson("lesson_1_2", "module_1", "1.2", "Variables and Data Types", "Understand different data types and how to use variables.", "x = 5\nprint(type(x))", "https://youtu.be/ohCDWZgNIU0"),
                        Lesson("lesson_1_3", "module_1", "1.3", "Basic Input/Output", "Learn how to get input and display output.", "name = input(\"Enter your name: \")\nprint(\"Hello\", name)", "https://youtu.be/fYlnfvKVDoM"),
                        Lesson("lesson_1_4", "module_1", "1.4", "Comments in Python", "Add comments to explain your code.", "# This is a comment\nprint(\"Hello\")", "https://youtu.be/MEz1J9wYqZ8")
                    ),
                    isExpanded = false
                ),
                Module(
                    id = "module_2",
                    title = "Module 2: Control Flow",
                    description = "Master conditionals, loops, and flow control in Python",
                    lessons = listOf(
                        Lesson("lesson_2_1", "module_2", "2.1", "Conditional Statements", "Use if-else statements in Python.", "if x > 0:\n    print(\"Positive\")", "https://youtu.be/f4KOjWS_KZs"),
                        Lesson("lesson_2_2", "module_2", "2.2", "Loops", "Learn about for and while loops.", "for i in range(5):\n    print(i)", "https://youtu.be/6iF8Xb7Z3wQ"),
                        Lesson("lesson_2_3", "module_2", "2.3", "Nested Conditions", "Understand how to nest conditions.", "if x > 0:\n    if x < 10:\n        print(\"Single digit positive\")", "https://youtu.be/L0CgkD9yYwM"),
                        Lesson("lesson_2_4", "module_2", "2.4", "Break and Continue", "Control loop execution flow.", "for i in range(10):\n    if i == 5:\n        break", "https://youtu.be/Y7Vm_uozTnY")
                    ),
                    isExpanded = false
                ),
                Module(
                    id = "module_3",
                    title = "Module 3: Functions & Recursion",
                    description = "Defining and calling functions",
                    lessons = listOf(
                        Lesson("lesson_3_1", "module_3", "3.1", "Defining Functions", "Learn how to create functions.", "def greet():\n    print(\"Hello!\")", "https://youtu.be/NSbOtYzIQI0"),
                        Lesson("lesson_3_2", "module_3", "3.2", "Recursion", "Understand recursive functions.", "def factorial(n):\n    return 1 if n == 0 else n * factorial(n-1)", "https://youtu.be/sv4hpv5Hl1I"),
                        Lesson("lesson_3_3", "module_3", "3.3", "Function Arguments", "Pass arguments to functions.", "def greet(name):\n    print(\"Hello, \" + name)", "https://youtu.be/9Os0o3wzS_I"),
                        Lesson("lesson_3_4", "module_3", "3.4", "Return Values", "Functions that return values.", "def add(a, b):\n    return a + b", "https://youtu.be/5m3D1jLUaEw")
                    ),
                    isExpanded = false
                ),
                Module(
                    id = "module_4",
                    title = "Module 4: Data Structures",
                    description = "Explore lists, tuples, dictionaries, and sets",
                    lessons = listOf(
                        Lesson("lesson_4_1", "module_4", "4.1", "Lists and Tuples", "Learn how to use lists and tuples.", "my_list = [1, 2, 3]\nmy_tuple = (1, 2, 3)", "https://youtu.be/W8KRzm-HUcc"),
                        Lesson("lesson_4_2", "module_4", "4.2", "Dictionaries and Sets", "Understand dictionaries and sets.", "my_dict = {'a': 1, 'b': 2}\nmy_set = {1, 2, 3}", "https://youtu.be/daefaLgNkw0"),
                        Lesson("lesson_4_3", "module_4", "4.3", "List Comprehension", "Write concise loops using list comprehension.", "[x*x for x in range(5)]", "https://youtu.be/3dt4OGnU5sM"),
                        Lesson("lesson_4_4", "module_4", "4.4", "Nested Data Structures", "Work with complex data like lists of dictionaries.", "students = [{'name': 'A', 'score': 90}, {'name': 'B', 'score': 85}]", "https://youtu.be/9vKqVkMQHKk")
                    ),
                    isExpanded = false
                ),
                Module(
                    id = "module_5",
                    title = "Module 5: File Handling & Exceptions",
                    description = "Read/write files and handle errors",
                    lessons = listOf(
                        Lesson("lesson_5_1", "module_5", "5.1", "Reading and Writing Files", "Learn file I/O operations.", "with open('file.txt', 'r') as f:\n    print(f.read())", "https://youtu.be/Uh2ebFW8OYM"),
                        Lesson("lesson_5_2", "module_5", "5.2", "Exception Handling", "Handle errors gracefully.", "try:\n    x = 1 / 0\nexcept ZeroDivisionError:\n    print(\"Cannot divide by zero!\")", "https://youtu.be/NIWwJbo-9_8"),
                        Lesson("lesson_5_3", "module_5", "5.3", "Working with CSV", "Read and write CSV files in Python.", "import csv\nwith open('data.csv') as file:\n    reader = csv.reader(file)", "https://youtu.be/NlD7xT0IS9s"),
                        Lesson("lesson_5_4", "module_5", "5.4", "Try-Except-Finally", "Use finally block in error handling.", "try:\n    print(\"Try block\")\nfinally:\n    print(\"Finally block\")", "https://youtu.be/_3b0eWNpRzI")
                    ),
                    isExpanded = false
                )
            )
        )
    }
}