package com.labactivity.lala.LEARNINGMATERIAL

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.R
import android.widget.Toast

class CourseFragment : Fragment() {

    private var courseId: String = ""  // Changed from lateinit to default value
    private val modules = mutableListOf<Module>()
    private var completedLessonIds: MutableSet<String> = mutableSetOf()  // Initialize directly
    private val TAG = "CourseFragment"
    private lateinit var progressManager: ModuleProgressManager

    companion object {
        private const val ARG_COURSE_ID = "courseId"

        fun newInstance(courseId: String): CourseFragment {
            return CourseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COURSE_ID, courseId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        courseId = arguments?.getString(ARG_COURSE_ID).also { 
            Log.d(TAG, "Retrieved courseId from arguments: $it")
        } ?: run {
            Log.e(TAG, "No courseId provided in arguments!")
            Toast.makeText(requireContext(), "Error: Course not found", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        if (courseId.isEmpty()) {
            Log.e(TAG, "Empty courseId provided!")
            Toast.makeText(requireContext(), "Error: Invalid course", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        // Initialize progress manager
        progressManager = ModuleProgressManager(requireContext())

        // Load completed lessons early from Firebase
        loadCompletedLessons()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated started with courseId: $courseId")

        setupViews(view)

        // Wait for completed lessons to load before loading course data
        // This ensures the UI shows the correct completion state
        waitForCompletedLessonsAndLoadData()
    }

    private fun waitForCompletedLessonsAndLoadData() {
        // Check if lessons are already loaded
        if (lessonsLoaded) {
            loadCourseData()
        } else {
            // Wait a bit and check again (the callback will trigger loadCourseData)
            Log.d(TAG, "Waiting for completed lessons to load from Firebase...")
        }
    }

    private var lessonsLoaded = false
    private var courseDataLoading = false

    private fun setupViews(view: View) {
        val tvCourseDescription: TextView = view.findViewById(R.id.tvCourseDescription)
        val rvModules: RecyclerView = view.findViewById(R.id.rvModules)
        val progressIndicator: LinearProgressIndicator = view.findViewById(R.id.progressIndicator)

        // Setup RecyclerView
        rvModules.layoutManager = LinearLayoutManager(requireContext())
        val moduleAdapter = ModuleAdapter(
            requireContext(),
            modules,
            completedLessonIds,
            onLessonCompleted = { lessonId, moduleId, isCompleted ->
                Log.d(TAG, "Lesson $lessonId for module $moduleId completed: $isCompleted")

                if (isCompleted) {
                    completedLessonIds.add(lessonId)
                    progressManager.saveCompletedLesson(courseId, moduleId, lessonId) { success ->
                        if (success) {
                            Log.d(TAG, "Lesson progress saved to Firebase")
                        }
                    }
                } else {
                    completedLessonIds.remove(lessonId)
                    progressManager.removeCompletedLesson(courseId, moduleId, lessonId) { success ->
                        if (success) {
                            Log.d(TAG, "Lesson progress removed from Firebase")
                        }
                    }
                }

                updateCourseProgress(progressIndicator)
            }
        )
        rvModules.adapter = moduleAdapter

        // Store adapter reference for updates
        this.moduleAdapter = moduleAdapter
    }

    private lateinit var moduleAdapter: ModuleAdapter

    private fun loadCourseData() {
        // Prevent loading course data multiple times
        if (courseDataLoading) {
            Log.d(TAG, "Course data already loading, skipping duplicate call")
            return
        }
        courseDataLoading = true

        val progressIndicator = view?.findViewById<LinearProgressIndicator>(R.id.progressIndicator)
        val tvCourseDescription = view?.findViewById<TextView>(R.id.tvCourseDescription)
        val rvModules = view?.findViewById<RecyclerView>(R.id.rvModules)

        Log.d(TAG, "Loading course data with ${completedLessonIds.size} completed lessons")

        // Fetch course data
        fetchCourse { title, description ->
            Log.d(TAG, "Course fetched - Title: $title, Description length: ${description.length}")
            tvCourseDescription?.text = description

            fetchModulesAndLessons {
                Log.d(TAG, "Modules and lessons fetch completed. Modules count: ${modules.size}")

                moduleAdapter.notifyDataSetChanged()
                progressIndicator?.let { updateCourseProgress(it) }

                // 🔹 Apply smooth pop-up animation for modules
                rvModules?.let { recyclerView ->
                    val animation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slide_up)
                    recyclerView.startAnimation(animation)
                }
            }
        }
    }


    private fun fetchCourse(onFetched: (String, String) -> Unit) {
        Log.d(TAG, "Starting fetchCourse for courseId: $courseId")
        FirebaseFirestore.getInstance().collection("courses")
            .document(courseId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Log.e(TAG, "Course document doesn't exist!")
                    return@addOnSuccessListener
                }
                val title = doc.getString("title") ?: ""
                val description = doc.getString("description") ?: ""
                Log.d(TAG, "Course data fetched successfully")
                onFetched(title, description)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching course", e)
            }
    }

    private fun fetchModulesAndLessons(onComplete: () -> Unit) {
        Log.d(TAG, "Starting fetchModulesAndLessons")
        val firestore = FirebaseFirestore.getInstance()
        
        // Clear existing modules before fetching
        modules.clear()
        
        firestore.collection("courses")
            .document(courseId)
            .collection("modules")
            .orderBy("title")  // Order modules alphabetically by title
            .get()
            .addOnSuccessListener { moduleSnapshot ->
                Log.d(TAG, "Module snapshot received. Empty? ${moduleSnapshot.isEmpty}")
                
                if (moduleSnapshot.isEmpty) {
                    Log.d(TAG, "No modules found for course $courseId")
                    onComplete()
                    return@addOnSuccessListener
                }

                val moduleDocuments = moduleSnapshot.documents
                Log.d(TAG, "Found ${moduleDocuments.size} modules")
                var completedModules = 0

                // Create a temporary map to preserve order
                val tempModules = mutableMapOf<Int, Module>()

                moduleDocuments.forEachIndexed { index, moduleDoc ->
                    Log.d(TAG, "Processing module at index $index: ${moduleDoc.id}")
                    val module = Module(
                        id = moduleDoc.id,
                        title = moduleDoc.getString("title") ?: "Untitled Module",
                        description = moduleDoc.getString("description") ?: "",
                        lessons = mutableListOf()
                    )

                    firestore.collection("courses")
                        .document(courseId)
                        .collection("modules")
                        .document(module.id)
                        .collection("lessons")
                        .orderBy("number")  // Order lessons by their number (1.1, 1.2, etc.)
                        .get()
                        .addOnSuccessListener { lessonSnapshot ->
                            Log.d(TAG, "Lessons snapshot received for module ${module.id}. Count: ${lessonSnapshot.size()}")

                            val lessons = lessonSnapshot.documents.mapNotNull { lessonDoc ->
                                try {
                                    Lesson(
                                        id = lessonDoc.id,
                                        number = lessonDoc.getString("number") ?: "0",
                                        title = lessonDoc.getString("title") ?: "Untitled Lesson",
                                        description = lessonDoc.getString("description") ?: "",
                                        codeExample = lessonDoc.getString("codeExample") ?: "",
                                        explanation = lessonDoc.getString("explanation") ?: "",
                                        videoUrl = lessonDoc.getString("videoUrl") ?: ""
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing lesson ${lessonDoc.id}", e)
                                    null
                                }
                            }

                            Log.d(TAG, "Successfully parsed ${lessons.size} lessons for module ${module.id}")
                            module.lessons.addAll(lessons)

                            synchronized(modules) {
                                tempModules[index] = module
                                completedModules++

                                Log.d(TAG, "Completed module ${module.id} at index $index. Progress: $completedModules/${moduleDocuments.size}")

                                if (completedModules == moduleDocuments.size) {
                                    // Add modules in the correct order
                                    modules.clear()
                                    tempModules.toSortedMap().values.forEach { modules.add(it) }
                                    Log.d(TAG, "All modules loaded in order. Total: ${modules.size}")
                                    onComplete()
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching lessons for module ${module.id}", e)
                            synchronized(modules) {
                                completedModules++
                                if (completedModules == moduleDocuments.size) {
                                    // Add modules in the correct order even with errors
                                    modules.clear()
                                    tempModules.toSortedMap().values.forEach { modules.add(it) }
                                    Log.e(TAG, "Completing with errors. Loaded modules: ${modules.size}")
                                    onComplete()
                                }
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching modules", e)
                onComplete()
            }
    }

    private fun updateCourseProgress(progressIndicator: LinearProgressIndicator) {
        val totalLessons = modules.sumOf { it.lessons.size }
        val completed = completedLessonIds.size
        val progress = if (totalLessons > 0) (completed * 100) / totalLessons else 0
        progressIndicator.progress = progress
    }

    private fun loadCompletedLessons() {
        try {
            progressManager.loadCompletedLessons(courseId) { lessons ->
                completedLessonIds.clear()
                completedLessonIds.addAll(lessons)
                lessonsLoaded = true
                Log.d(TAG, "Loaded ${completedLessonIds.size} completed lessons from Firebase for course: $courseId")
                Log.d(TAG, "Completed lesson IDs: $completedLessonIds")

                // If view is already created, load the course data now
                if (view != null) {
                    loadCourseData()
                }

                // Refresh UI if already loaded
                if (::moduleAdapter.isInitialized) {
                    moduleAdapter.notifyDataSetChanged()
                    view?.findViewById<LinearProgressIndicator>(R.id.progressIndicator)?.let {
                        updateCourseProgress(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading completed lessons", e)
            completedLessonIds.clear()
            lessonsLoaded = true
            // Still load course data even if there's an error
            if (view != null) {
                loadCourseData()
            }
        }
    }
}
