package com.labactivity.lala.SQLCOMPILER

import java.util.regex.Pattern

class QueryValidator {

    companion object {
        private val ALLOWED_KEYWORDS = setOf(
            "SELECT", "FROM", "WHERE", "ORDER", "BY", "GROUP",
            "HAVING", "JOIN", "INNER", "LEFT", "RIGHT", "OUTER",
            "UNION", "DISTINCT", "COUNT", "SUM", "AVG", "MIN", "MAX",
            "AND", "OR", "NOT", "IN", "LIKE", "BETWEEN", "IS", "NULL",
            "AS", "ASC", "DESC", "LIMIT", "OFFSET"
        )

        private val BLOCKED_KEYWORDS = setOf(
            "DROP", "DELETE", "INSERT", "UPDATE", "CREATE", "ALTER",
            "EXEC", "EXECUTE", "TRUNCATE", "GRANT", "REVOKE"
        )

        private val SQL_INJECTION_PATTERNS = listOf(
            Pattern.compile(".*;\\s*(DROP|DELETE|INSERT|UPDATE)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("--", Pattern.CASE_INSENSITIVE),
            Pattern.compile("/\\*.*\\*/", Pattern.CASE_INSENSITIVE)
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateQuery(query: String): ValidationResult {
        val cleanQuery = query.trim().uppercase()

        // Check for empty query
        if (cleanQuery.isEmpty()) {
            return ValidationResult(false, "Query cannot be empty")
        }

        // Check for blocked keywords
        for (keyword in BLOCKED_KEYWORDS) {
            if (cleanQuery.contains(keyword)) {
                return ValidationResult(false, "Keyword '$keyword' is not allowed")
            }
        }

        // Check for SQL injection patterns
        for (pattern in SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(query).find()) {
                return ValidationResult(false, "Query contains potentially dangerous patterns")
            }
        }

        // Check query length
        if (query.length > 1000) {
            return ValidationResult(false, "Query is too long (max 1000 characters)")
        }

        return ValidationResult(true)
    }
}

