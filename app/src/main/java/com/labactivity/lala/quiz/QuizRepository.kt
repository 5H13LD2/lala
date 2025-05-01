package com.labactivity.lala.quiz

/**
 * Interface for repositories that provide quiz questions by module
 */
interface QuizRepository {
    /**
     * Gets quiz questions for a specific module
     * @param moduleId The ID of the module
     * @return List of Quiz objects for the specified module (limited to 10)
     */
    fun getQuestionsForModule(moduleId: String): List<Quiz>
    
    /**
     * Gets the total number of questions available for a module
     * @param moduleId The ID of the module
     * @return The number of questions available, limited to a maximum of 10
     */
    fun getQuestionCountForModule(moduleId: String): Int
    
    /**
     * Get a list of all module IDs supported by this repository
     * @return List of module IDs
     */
    fun getAllModuleIds(): List<String>
    
    /**
     * Checks if this repository can handle questions for the given module ID
     * @param moduleId The ID of the module to check
     * @return true if this repository can handle the module, false otherwise
     */
    fun canHandleModule(moduleId: String): Boolean
} 