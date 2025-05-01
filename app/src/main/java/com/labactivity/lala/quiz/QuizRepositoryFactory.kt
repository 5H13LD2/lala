package com.labactivity.lala.quiz

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
        Log.d("QuizRepositoryFactory", "Finding repository for module ID: $moduleId")
        
        // Check each repository in order
        for (repo in repositories) {
            val canHandle = repo.canHandleModule(moduleId)
            Log.d("QuizRepositoryFactory", "${repo.javaClass.simpleName}.canHandleModule('$moduleId') = $canHandle")
            
            if (canHandle) {
                Log.d("QuizRepositoryFactory", "Selected repository: ${repo.javaClass.simpleName} for module ID: $moduleId")
                return repo
            }
        }
        
        // If no specific repository is found, fall back to the Python repository (ModuleQuizRepository)
        Log.d("QuizRepositoryFactory", "No repository claimed module ID: $moduleId, using fallback")
        return repositories.last() // Python repository is the last in the list
    }
    

    fun getAllRepositories(): List<QuizRepository> {
        return repositories
    }

    fun getAllModuleIds(): List<String> {
        return repositories.flatMap { it.getAllModuleIds() }
    }
} 