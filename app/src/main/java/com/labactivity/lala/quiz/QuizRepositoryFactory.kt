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
        
        // First check Python repository since it handles legacy numeric IDs
        val pythonRepo = ModuleQuizRepository()
        if (pythonRepo.canHandleModule(moduleId)) {
            Log.d(TAG, "Selected repository: ModuleQuizRepository (Python) for module ID: $moduleId")
            return pythonRepo
        }
        
        // Then check SQL repository
        val sqlRepo = SqlModuleQuizRepository()
        if (sqlRepo.canHandleModule(moduleId)) {
            Log.d(TAG, "Selected repository: SqlModuleQuizRepository for module ID: $moduleId")
            return sqlRepo
        }
        
        // Finally check Java repository
        val javaRepo = JavaModuleQuizRepository()
        if (javaRepo.canHandleModule(moduleId)) {
            Log.d(TAG, "Selected repository: JavaModuleQuizRepository for module ID: $moduleId")
            return javaRepo
        }
        
        // If no repository can handle the module, log an error and return Python as fallback
        Log.e(TAG, "No repository found for module ID: $moduleId, using Python repository as fallback")
        return pythonRepo
    }
    

    fun getAllRepositories(): List<QuizRepository> {
        return repositories
    }

    fun getAllModuleIds(): List<String> {
        return repositories.flatMap { it.getAllModuleIds() }
    }
} 