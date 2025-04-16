// Create a file named Course.kt
package com.labactivity.lala

data class Courses(
    val id: String,
    val title: String,
    val description: String,
    val modules: List<Module>
)