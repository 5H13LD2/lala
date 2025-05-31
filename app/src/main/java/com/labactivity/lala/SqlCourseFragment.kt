package com.labactivity.lala

import android.content.Context
import android.content.Intent
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
import com.labactivity.lala.LEARNINGMATERIAL.Lesson
import com.labactivity.lala.LEARNINGMATERIAL.Module
import com.labactivity.lala.LEARNINGMATERIAL.SqlModuleAdapter
import com.labactivity.lala.homepage.Courses
import com.labactivity.lala.quiz.DynamicQuizActivity

class SqlCourseFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sqlRvModules: RecyclerView
    private lateinit var sqlProgressIndicator: LinearProgressIndicator
    private lateinit var sqlModuleAdapter: SqlModuleAdapter

    private val completedLessonIds = mutableSetOf<String>()
    private lateinit var course: Courses

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sql_fragmentcourse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize sharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("course_preferences", Context.MODE_PRIVATE)

        // Initialize views
        sqlRvModules = view.findViewById(R.id.sqlRvModules)
        sqlProgressIndicator = view.findViewById(R.id.sqlProgressIndicator)
        val sqlToolbar: MaterialToolbar = view.findViewById(R.id.sqlToolbar)
        val sqlTvCourseDescription: TextView = view.findViewById(R.id.sqlTvCourseDescription)

        // Load saved completed lessons
        loadCompletedLessons()

        // Initialize course data
        course = createDummyCourse()

        // Set up course info
        sqlToolbar.title = course.title
        sqlTvCourseDescription.text = course.description

        // Set up RecyclerView
        sqlRvModules.layoutManager = LinearLayoutManager(requireContext())
        sqlModuleAdapter = SqlModuleAdapter(
            requireContext(),
            course.modules,
            completedLessonIds,
            onLessonCompleted = { lessonId ->
                completedLessonIds.add(lessonId)
                saveCompletedLessons()  // Save the updated completed lessons
                updateCoursesProgress() // Update the course progress UI
            },
            onLessonClick = { lesson ->
                // Handle lesson click (e.g., open lesson detail)
                // For now, just log the click
                Log.d("SqlCourseFragment", "Lesson clicked: ${lesson.title}")
            },
            onQuizClick = { module ->
                // Navigate to the quiz for this module
                Log.d("SqlCourseFragment", "Quiz button clicked for module: ${module.id}")
                val intent = Intent(requireContext(), DynamicQuizActivity::class.java).apply {
                    putExtra("module_id", module.id)
                    putExtra("module_title", module.title)
                }
                startActivity(intent)
            }
        )
        sqlRvModules.adapter = sqlModuleAdapter

        // Initialize course progress
        updateCoursesProgress()
    }

    private fun updateCoursesProgress() {
        val totalLessons = course.modules.sumOf { it.lessons.size }
        val completedCount = completedLessonIds.size
        val progressPercentage = if (totalLessons > 0) (completedCount.toInt() * 100) / totalLessons else 0
        sqlProgressIndicator.progress = progressPercentage
    }

    private fun loadCompletedLessons() {
        val completedLessons = sharedPreferences.getStringSet("completed_lessons", emptySet())
        Log.d("SqlCourseFragment", "Completed Lessons Loaded: ${completedLessons?.size}")
        completedLessonIds.addAll(completedLessons ?: emptySet())
    }

    private fun saveCompletedLessons() {
        sharedPreferences.edit().putStringSet("completed_lessons", completedLessonIds).apply()
        Log.d("SqlCourseFragment", "Completed Lessons Saved: ${completedLessonIds.size}")
    }
}


private fun createDummyCourse(): Courses {
    return Courses(
        id = "sql_101",
        title = "SQL Mastery Course",
        description = "Become proficient in SQL with this comprehensive course. Learn SQL from the basics to advanced queries, with practical exercises and real-world applications.",
        modules = listOf(
            Module(
                id = "module_1",
                title = "Module 1: Introduction to SQL",
                description = "Learn the basics of SQL, databases, and queries.",
                lessons = listOf(
                    Lesson("lesson_1_1", "module_1", "1.1", "What is SQL?", "Understand the importance of SQL in database management.", "SELECT * FROM users;", "https://youtu.be/DJ2U0FC1qHk"),
                    Lesson("lesson_1_2", "module_1", "1.2", "SQL Syntax Basics", "Learn the basic structure of SQL queries.", "SELECT column_name FROM table_name;", "https://youtu.be/5hf-VzXtaAc"),
                    Lesson("lesson_1_3", "module_1", "1.3", "Database Structure", "Understand tables, rows, and columns in a database.", "CREATE TABLE users (id INT, name VARCHAR(255));", "https://youtu.be/1XpzP1Go2kE"),
                    Lesson("lesson_1_4", "module_1", "1.4", "Selecting Data", "Learn to retrieve data from a table using SELECT.", "SELECT * FROM employees;", "https://youtu.be/1pGBpGVrHzg")
                ),
                isExpanded = false
            ),
            Module(
                id = "module_2",
                title = "Module 2: Data Manipulation",
                description = "Learn how to manipulate data using SQL commands like INSERT, UPDATE, and DELETE.",
                lessons = listOf(
                    Lesson("lesson_2_1", "module_2", "2.1", "Inserting Data", "Learn how to insert data into a table.", "INSERT INTO users (id, name) VALUES (1, 'John Doe');", "https://youtu.be/-2dAplV4_W0"),
                    Lesson("lesson_2_2", "module_2", "2.2", "Updating Data", "Learn how to modify existing data in a table.", "UPDATE users SET name = 'Jane Doe' WHERE id = 1;", "https://youtu.be/zJXv6z0OH-Q"),
                    Lesson("lesson_2_3", "module_2", "2.3", "Deleting Data", "Learn how to remove data from a table.", "DELETE FROM users WHERE id = 1;", "https://youtu.be/OBtNHO2BoMQ"),
                    Lesson("lesson_2_4", "module_2", "2.4", "Using NULL", "Understand how to handle NULL values.", "SELECT * FROM users WHERE address IS NULL;", "https://youtu.be/1sdV1joG4uk")
                ),
                isExpanded = false
            ),
            Module(
                id = "module_3",
                title = "Module 3: SQL Joins",
                description = "Learn how to combine data from multiple tables using different types of joins.",
                lessons = listOf(
                    Lesson("lesson_3_1", "module_3", "3.1", "INNER JOIN", "Learn how to combine rows from two tables where there's a match.", "SELECT * FROM orders INNER JOIN customers ON orders.customer_id = customers.id;", "https://youtu.be/L9l9NdgGmw8"),
                    Lesson("lesson_3_2", "module_3", "3.2", "LEFT JOIN", "Learn how to get all rows from the left table and matched rows from the right table.", "SELECT * FROM employees LEFT JOIN departments ON employees.department_id = departments.id;", "https://youtu.be/HGxmiFcAmA0"),
                    Lesson("lesson_3_3", "module_3", "3.3", "RIGHT JOIN", "Learn about the right join in SQL.", "SELECT * FROM orders RIGHT JOIN customers ON orders.customer_id = customers.id;", "https://youtu.be/pqXyfn2KztI"),
                    Lesson("lesson_3_4", "module_3", "3.4", "FULL OUTER JOIN", "Learn how to get all rows from both tables.", "SELECT * FROM products FULL OUTER JOIN orders ON products.product_id = orders.product_id;", "https://youtu.be/gDjZPv8YsXI")
                ),
                isExpanded = false
            ),
            Module(
                id = "module_4",
                title = "Module 4: Advanced SQL Queries",
                description = "Dive deeper into SQL with complex queries and subqueries.",
                lessons = listOf(
                    Lesson("lesson_4_1", "module_4", "4.1", "Subqueries", "Learn how to write queries within queries.", "SELECT * FROM orders WHERE customer_id IN (SELECT customer_id FROM customers WHERE city = 'New York');", "https://youtu.be/9v4rz0_9Zgk"),
                    Lesson("lesson_4_2", "module_4", "4.2", "Aggregating Data", "Learn how to use aggregate functions like COUNT, SUM, AVG.", "SELECT COUNT(*) FROM orders WHERE amount > 100;", "https://youtu.be/_XzIkFJbAfo"),
                    Lesson("lesson_4_3", "module_4", "4.3", "Group By", "Learn how to group rows that have the same values.", "SELECT city, COUNT(*) FROM customers GROUP BY city;", "https://youtu.be/jfz6U1HeGb4"),
                    Lesson("lesson_4_4", "module_4", "4.4", "Having Clause", "Learn how to filter aggregated data.", "SELECT city, COUNT(*) FROM customers GROUP BY city HAVING COUNT(*) > 10;", "https://youtu.be/3_HmWRt8cyI")
                ),
                isExpanded = false
            ),
            Module(
                id = "module_5",
                title = "Module 5: Database Design & Optimization",
                description = "Learn about designing efficient databases and optimizing queries.",
                lessons = listOf(
                    Lesson("lesson_5_1", "module_5", "5.1", "Normalization", "Learn how to organize data in a database efficiently.", "CREATE TABLE employees (id INT PRIMARY KEY, name VARCHAR(100), department_id INT);", "https://youtu.be/JZjVA-jkRjQ"),
                    Lesson("lesson_5_2", "module_5", "5.2", "Indexes", "Learn how indexes improve the speed of data retrieval.", "CREATE INDEX idx_name ON employees (name);", "https://youtu.be/8u4jrqGfS5Q"),
                    Lesson("lesson_5_3", "module_5", "5.3", "Query Optimization", "Learn strategies to optimize SQL queries for better performance.", "EXPLAIN SELECT * FROM orders WHERE customer_id = 101;", "https://youtu.be/Z9ZPbH3lDjg"),
                    Lesson("lesson_5_4", "module_5", "5.4", "Stored Procedures", "Learn how to create and use stored procedures.", "CREATE PROCEDURE GetEmployeeDetails(id INT)\nBEGIN\nSELECT * FROM employees WHERE id = id;\nEND;", "https://youtu.be/Zt9cQxZy9no")
                ),
                isExpanded = false
            )
        )
    )
}


