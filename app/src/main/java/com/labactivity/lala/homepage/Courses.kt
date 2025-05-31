// Create a file named Course.kt
package com.labactivity.lala.homepage

import com.labactivity.lala.LEARNINGMATERIAL.Module

data class Courses(
    val id: String,
    val title: String,
    val description: String,
    val modules: List<Module>
)