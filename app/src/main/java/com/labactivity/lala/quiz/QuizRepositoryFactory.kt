package com.labactivity.lala.quiz

import android.content.ContentValues.TAG
import android.util.Log

/**
 * Factory class for creating the appropriate QuizRepository based on the module ID
 */
object QuizRepositoryFactory {
    
    // List of all available repositories
    private val repositories: List<QuizRepository> = listOf(
        JavaModuleQuizRepository(),
        SqlModuleQuizRepository(),
        ModuleQuizRepository() // Python repository is the fallback
    )
    
    /**
     * Gets the appropriate repository for the given module ID
     * @param moduleId The ID of the module
     * @return The appropriate QuizRepository for the module
     */
    fun getRepositoryForModule(moduleId: String): QuizRepository {
        Log.d(TAG, "Finding repository for module ID: $moduleId")
        
        // First check SQL repository since it has specific module ID patterns
        val sqlRepo = SqlModuleQuizRepository()
        if (sqlRepo.canHandleModule(moduleId)) {
            Log.d(TAG, "Selected repository: SqlModuleQuizRepository for module ID: $moduleId")
            return sqlRepo
        }
        
        // Then check Java repository
        val javaRepo = JavaModuleQuizRepository()
        if (javaRepo.canHandleModule(moduleId)) {
            Log.d(TAG, "Selected repository: JavaModuleQuizRepository for module ID: $moduleId")
            return javaRepo
        }
        
        // Finally check Python repository
        val pythonRepo = ModuleQuizRepository()
        if (pythonRepo.canHandleModule(moduleId)) {
            Log.d(TAG, "Selected repository: ModuleQuizRepository (Python) for module ID: $moduleId")
            return pythonRepo
        }
        
        // If no repository can handle the module, log an error and return SQL as fallback
        Log.e(TAG, "No repository found for module ID: $moduleId, using SQL repository as fallback")
        return sqlRepo
    }
    

    fun getAllRepositories(): List<QuizRepository> {
        return repositories
    }

    fun getAllModuleIds(): List<String> {
        return repositories.flatMap { it.getAllModuleIds() }
    }
} 