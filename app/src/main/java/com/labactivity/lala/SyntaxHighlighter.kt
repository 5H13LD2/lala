package com.labactivity.lala

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import java.util.regex.Pattern

class SyntaxHighlighter {

    companion object {
        private val SQL_KEYWORDS = arrayOf(
            "SELECT", "FROM", "WHERE", "ORDER", "BY", "GROUP", "HAVING",
            "JOIN", "INNER", "LEFT", "RIGHT", "OUTER", "UNION", "DISTINCT",
            "COUNT", "SUM", "AVG", "MIN", "MAX", "AND", "OR", "NOT",
            "IN", "LIKE", "BETWEEN", "IS", "NULL", "AS", "ASC", "DESC"
        )

        private val KEYWORD_COLOR = Color.BLUE
        private val STRING_COLOR = Color.GREEN
        private val NUMBER_COLOR = Color.RED
    }

    fun highlightSql(text: String): SpannableString {
        val spannable = SpannableString(text)

        // Highlight keywords
        for (keyword in SQL_KEYWORDS) {
            val pattern = Pattern.compile("\\b$keyword\\b", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(text)

            while (matcher.find()) {
                spannable.setSpan(
                    ForegroundColorSpan(KEYWORD_COLOR),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Highlight strings
        val stringPattern = Pattern.compile("'[^']*'")
        val stringMatcher = stringPattern.matcher(text)

        while (stringMatcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(STRING_COLOR),
                stringMatcher.start(),
                stringMatcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Highlight numbers
        val numberPattern = Pattern.compile("\\b\\d+\\b")
        val numberMatcher = numberPattern.matcher(text)

        while (numberMatcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(NUMBER_COLOR),
                numberMatcher.start(),
                numberMatcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
}

// ===================================
// 16. ENHANCED TEST CASES
// ===================================

// Add this to DatabaseHelper.kt to extend getTestCases() method
/*
fun getExtendedTestCases(): List<TestCase> {
    return listOf(
        TestCase(
            id = 1,
            title = "Select All Users",
            description = "Retrieve all user records from the users table",
            sampleTables = mapOf(
                "users" to listOf(
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 3, "name" to "Jane", "age" to 25)
                )
            ),
            expectedQuery = "SELECT * FROM users",
            expectedOutput = listOf(
                mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                mapOf("id" to 2, "name" to "John", "age" to 22),
                mapOf("id" to 3, "name" to "Jane", "age" to 25)
            ),
            difficulty = "easy",
            tags = listOf("basic", "select")
        ),

        TestCase(
            id = 2,
            title = "Filter Users by Age",
            description = "Select users who are older than 21 years",
            sampleTables = mapOf(
                "users" to listOf(
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 3, "name" to "Jane", "age" to 25)
                )
            ),
            expectedQuery = "SELECT * FROM users WHERE age > 21",
            expectedOutput = listOf(
                mapOf("id" to 2, "name" to "John", "age" to 22),
                mapOf("id" to 3, "name" to "Jane", "age" to 25)
            ),
            difficulty = "easy",
            tags = listOf("where", "filter", "comparison")
        ),

        TestCase(
            id = 3,
            title = "Select Specific Columns",
            description = "Select only name and age columns from users",
            sampleTables = mapOf(
                "users" to listOf(
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 3, "name" to "Jane", "age" to 25)
                )
            ),
            expectedQuery = "SELECT name, age FROM users",
            expectedOutput = listOf(
                mapOf("name" to "Jerico", "age" to 20),
                mapOf("name" to "John", "age" to 22),
                mapOf("name" to "Jane", "age" to 25)
            ),
            difficulty = "easy",
            tags = listOf("select", "columns")
        ),

        TestCase(
            id = 4,
            title = "Order Users by Age",
            description = "Select all users ordered by age in descending order",
            sampleTables = mapOf(
                "users" to listOf(
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 3, "name" to "Jane", "age" to 25)
                )
            ),
            expectedQuery = "SELECT * FROM users ORDER BY age DESC",
            expectedOutput = listOf(
                mapOf("id" to 3, "name" to "Jane", "age" to 25),
                mapOf("id" to 2, "name" to "John", "age" to 22),
                mapOf("id" to 1, "name" to "Jerico", "age" to 20)
            ),
            difficulty = "medium",
            tags = listOf("order by", "sorting")
        ),

        TestCase(
            id = 5,
            title = "Count Users",
            description = "Count the total number of users",
            sampleTables = mapOf(
                "users" to listOf(
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 3, "name" to "Jane", "age" to 25)
                )
            ),
            expectedQuery = "SELECT COUNT(*) as total_users FROM users",
            expectedOutput = listOf(
                mapOf("total_users" to 3)
            ),
            difficulty = "medium",
            tags = listOf("aggregate", "count")
        )
    )
}
*/