package com.labactivity.lala.ProfileMainActivity5

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.labactivity.lala.UTILS.DialogUtils
import androidx.core.content.ContextCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.LEARNINGMATERIAL.CoreModule
import com.labactivity.lala.LEARNINGMATERIAL.ModuleProgressManager
import com.labactivity.lala.LEADERBOARDPAGE.Leaderboard
import com.labactivity.lala.LEADERBOARDPAGE.Achievement
import com.labactivity.lala.LEADERBOARDPAGE.AchievementAdapter
import com.labactivity.lala.databinding.ActivityProfileMain5Binding
import com.labactivity.lala.homepage.MainActivity4
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.InputStream
import com.labactivity.lala.quiz.QuizScoreManager
import com.labactivity.lala.GAMIFICATION.XPManager
import com.labactivity.lala.GAMIFICATION.AchievementManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.labactivity.lala.R
import kotlinx.coroutines.tasks.await
import com.labactivity.lala.UTILS.setupWithSafeNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.labactivity.lala.SettingsActivity

class ProfileMainActivity5 : BaseActivity() {

    private lateinit var binding: ActivityProfileMain5Binding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ProfileMainActivity5"

    private var selectedImageUri: Uri? = null

    // Adapters for courses and quizzes
    private lateinit var enrolledCoursesAdapter: EnrolledCoursesAdapter
    private lateinit var quizHistoryAdapter: QuizHistoryAdapter
    private lateinit var achievementBadgeAdapter: AchievementBadgeAdapter
    private lateinit var progressManager: ModuleProgressManager
    private lateinit var quizScoreManager: QuizScoreManager
    private lateinit var xpManager: XPManager
    private lateinit var achievementManager: AchievementManager
    private var allQuizHistory: List<QuizHistoryItem> = listOf()

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Permission granted")
            openImagePicker()
        } else {
            Log.w(TAG, "Permission denied")
            DialogUtils.showErrorDialog(this, "Permission Denied", "Cannot access photos without permission.")
        }
    }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Log.d(TAG, "Image selected: $uri")
                saveImageToFirestore(uri)
            } ?: run {
                Log.e(TAG, "No image data received")
                DialogUtils.showErrorDialog(this, "Error", "Failed to get image")
            }
        } else {
            Log.w(TAG, "Image picker cancelled or failed. Result code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileMain5Binding.inflate(layoutInflater)
        setContentView(binding.root)

        progressManager = ModuleProgressManager(this)
        quizScoreManager = QuizScoreManager(this)
        xpManager = XPManager()
        achievementManager = AchievementManager()

        setupRecyclerViews()
        setupClickListeners()
        setupBottomNavigation()
        loadUserProfile()
        loadEnrolledCourses()
        loadQuizHistory()
        loadAchievements()
    }

    private fun setupRecyclerViews() {
        // Setup enrolled courses RecyclerView
        enrolledCoursesAdapter = EnrolledCoursesAdapter(
            this,
            mutableListOf()
        ) { course ->
            // Navigate to CoreModule when course is clicked
            val intent = Intent(this, CoreModule::class.java).apply {
                putExtra("COURSE_ID", course.courseId)
                putExtra("COURSE_NAME", course.courseName)
                putExtra("COURSE_DESC", "")
            }
            startActivity(intent)
        }

        binding.enrolledCoursesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProfileMainActivity5)
            adapter = enrolledCoursesAdapter
        }

        // Setup quiz history RecyclerView
        quizHistoryAdapter = QuizHistoryAdapter(
            this,
            mutableListOf()
        ) { quiz ->
            // Show dialog with quiz details when clicked
            DialogUtils.showInfoDialog(
                this,
                "Quiz Details",
                "Quiz from ${quiz.courseName}: ${quiz.score}/${quiz.totalQuestions}"
            )
        }

        binding.quizHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProfileMainActivity5)
            adapter = quizHistoryAdapter
        }

        // Setup achievement badges RecyclerView
        achievementBadgeAdapter = AchievementBadgeAdapter(emptyList())
        binding.achievementBadgesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ProfileMainActivity5, 5)
            adapter = achievementBadgeAdapter
        }
    }

    private fun setupClickListeners() {
        // Back button - navigate to home
        binding.imageView2.setOnClickListener {
            Log.d(TAG, "Back button clicked - navigating to home")
            val intent = Intent(this, MainActivity4::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Leaderboard button
        binding.Leaderboard.setOnClickListener {
            Log.d(TAG, "Leaderboard clicked")
            checkLeaderboardEligibility()
        }

        // Upload photo button
        binding.btnUploadPhoto.setOnClickListener {
            Log.d(TAG, "Upload photo clicked")
            checkPermissionAndOpenPicker()
        }

        // Edit profile button
        binding.btnEditProfile.setOnClickListener {
            DialogUtils.showInfoDialog(this, "Coming Soon", "Edit profile feature coming soon!")
        }

        // Profile image click to upload
        binding.profileImageCard.setOnClickListener {
            checkPermissionAndOpenPicker()
        }

        // View All Quizzes button
        binding.viewAllQuizzesBtn.setOnClickListener {
            showAllQuizzes()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView

        // Set the profile item as selected since we're on the profile page
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setupWithSafeNavigation(
            this,
            ProfileMainActivity5::class.java,
            mapOf(
                R.id.nav_home to MainActivity4::class.java,
                R.id.nav_profile to ProfileMainActivity5::class.java,
                R.id.nav_settings to SettingsActivity::class.java
            )
        )
    }

    private fun checkLeaderboardEligibility() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user")
            DialogUtils.showWarningDialog(this, "Login Required", "Please log in to view leaderboard")
            return
        }

        // Fetch user's current XP from Firestore
        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val totalXP = (document.getLong("totalXP") ?: 0).toInt()

                    if (totalXP >= 500) {
                        // User is eligible, navigate to leaderboard
                        Log.d(TAG, "User eligible for leaderboard with $totalXP XP")
                        val intent = Intent(this, Leaderboard::class.java)
                        startActivity(intent)
                    } else {
                        // User doesn't have enough XP
                        val requiredXP = 500 - totalXP
                        Log.d(TAG, "User not eligible. Current XP: $totalXP, Required: 500")
                        DialogUtils.showInfoDialog(
                            this,
                            "XP Required",
                            "You need 500 XP to access the leaderboard. You need $requiredXP more XP!"
                        )
                    }
                } else {
                    Log.w(TAG, "No user document found")
                    DialogUtils.showInfoDialog(this, "XP Required", "You need 500 XP to access the leaderboard.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking leaderboard eligibility", e)
                DialogUtils.showErrorDialog(this, "Error", "Failed to check eligibility. Please try again.")
            }
    }

    private fun checkPermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
            Log.d(TAG, "Image picker launched")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening image picker", e)
            DialogUtils.showErrorDialog(this, "Error", "Error opening gallery: ${e.message}")
        }
    }

    private fun saveImageToFirestore(imageUri: Uri) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated")
            DialogUtils.showErrorDialog(this, "Error", "User not authenticated")
            return
        }

        Log.d(TAG, "Starting image save for URI: $imageUri")
        DialogUtils.showInfoDialog(this, "Processing", "Processing image...")

        try {
            // Convert image to Base64
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                DialogUtils.showErrorDialog(this, "Error", "Failed to load image")
                return
            }

            // Compress and convert to Base64
            val base64Image = compressAndEncodeImage(bitmap)

            if (base64Image == null) {
                DialogUtils.showErrorDialog(this, "Error", "Failed to process image")
                return
            }

            Log.d(TAG, "Image compressed. Size: ${base64Image.length} characters")

            // Display image immediately
            displayProfileImageFromBase64(base64Image)

            // Save to Firestore
            firestore.collection("users")
                .document(currentUser.uid)
                .update("profilePhotoBase64", base64Image)
                .addOnSuccessListener {
                    Log.d(TAG, "Profile photo saved to Firestore successfully")
                    DialogUtils.showSuccessDialog(this, "Success", "Photo saved successfully!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save photo to Firestore", e)
                    DialogUtils.showErrorDialog(this, "Error", "Failed to save photo: ${e.message}")
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            DialogUtils.showErrorDialog(this, "Error", "${e.message}")
        }
    }

    private fun compressAndEncodeImage(bitmap: Bitmap): String? {
        try {
            // Calculate new dimensions (max 512x512 to keep Base64 size reasonable)
            val maxDimension = 512
            val width = bitmap.width
            val height = bitmap.height

            val scale = Math.min(
                maxDimension.toFloat() / width,
                maxDimension.toFloat() / height
            )

            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()

            // Resize bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            // Encode to Base64
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            Log.d(TAG, "Original size: ${width}x${height}, Compressed size: ${newWidth}x${newHeight}")
            Log.d(TAG, "Base64 length: ${base64String.length}")

            // Clean up
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }

            return base64String

        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            return null
        }
    }

    private fun displayProfileImageFromBase64(base64String: String) {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            binding.imageView3.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying Base64 image", e)
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user")
            return
        }

        // Load user data from Firestore with XP system
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val document = withContext(Dispatchers.IO) {
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .addOnSuccessListener { }
                        .addOnFailureListener { }
                }

                firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Get user data
                            val username = document.getString("username") ?: "@${currentUser.email?.split("@")?.get(0)}"
                            val email = document.getString("email") ?: currentUser.email ?: "No email"
                            val totalXP = (document.getLong("totalXP") ?: 0).toInt()
                            val coursesCompleted = (document.getLong("coursesCompleted") ?: 0).toInt()
                            val quizzesTaken = (document.getLong("quizzesTaken") ?: 0).toInt()
                            val technicalAssessments = (document.getLong("technicalAssessmentsCompleted") ?: 0).toInt()
                            val profilePhotoBase64 = document.getString("profilePhotoBase64")
                            val currentBadge = document.getString("currentBadge") ?: Achievement.TIER_NONE

                            // Calculate level and progress using XPManager
                            val level = xpManager.calculateLevel(totalXP)
                            val progressInLevel = xpManager.getXPProgressInLevel(totalXP)
                            val progressPercentage = xpManager.getProgressPercentage(totalXP)

                            // Update UI
                            binding.textUsername.text = username
                            binding.textEmail.text = email

                            // Display user badge
                            if (currentBadge != Achievement.TIER_NONE) {
                                binding.userBadgeDisplay.visibility = View.VISIBLE
                                val badgeEmoji = when (currentBadge) {
                                    Achievement.TIER_BRONZE -> "🥉"
                                    Achievement.TIER_SILVER -> "🥈"
                                    Achievement.TIER_GOLD -> "🥇"
                                    Achievement.TIER_PLATINUM -> "💎"
                                    Achievement.TIER_DIAMOND -> "💎"
                                    else -> ""
                                }
                                binding.userBadgeDisplay.text = "$badgeEmoji ${Achievement.getBadgeDisplayName(currentBadge)}"
                                binding.userBadgeDisplay.setTextColor(android.graphics.Color.parseColor(Achievement.getBadgeColor(currentBadge)))
                            } else {
                                binding.userBadgeDisplay.visibility = View.GONE
                            }

                            // Display level and XP
                            binding.textXpValue.text = "${xpManager.getLevelString(totalXP)} — $totalXP XP"
                            binding.textCoursesValue.text = coursesCompleted.toString()
                            binding.textExercisesValue.text = quizzesTaken.toString()

                            // Update progress bar with checkpoints
                            updateProgressWithCheckpoints(totalXP, level, progressInLevel, progressPercentage)

                            // Update progress text to show progress within level
                            binding.progressText.text = "$progressInLevel / ${XPManager.XP_PER_LEVEL} XP"

                            // Load profile photo if available
                            if (!profilePhotoBase64.isNullOrEmpty()) {
                                displayProfileImageFromBase64(profilePhotoBase64)
                            }

                            // Load achievements based on total XP
                            loadAchievements()

                            Log.d(TAG, "User profile loaded successfully")
                            Log.d(TAG, "  Total XP: $totalXP")
                            Log.d(TAG, "  Level: $level")
                            Log.d(TAG, "  Progress: $progressInLevel/${XPManager.XP_PER_LEVEL} ($progressPercentage%)")
                        } else {
                            Log.w(TAG, "No user document found")
                            // Set default values
                            binding.textUsername.text = "@${currentUser.email?.split("@")?.get(0)}"
                            binding.textEmail.text = currentUser.email ?: "No email"
                            binding.textXpValue.text = "Level 0 — 0 XP"
                            binding.progressText.text = "0 / ${XPManager.XP_PER_LEVEL} XP"
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error loading user profile", e)
                        DialogUtils.showErrorDialog(this@ProfileMainActivity5, "Error", "Failed to load profile")
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error in loadUserProfile", e)
            }
        }
    }

    /**
     * Updates the XP progress bar with checkpoint markers
     */
    private fun updateProgressWithCheckpoints(totalXP: Int, level: Int, progressInLevel: Int, progressPercentage: Int) {
        val XP_PER_LEVEL = 500

        // Calculate current level based on totalXP
        val currentLevel = (totalXP / XP_PER_LEVEL) + 1
        val nextLevel = currentLevel + 1
        val xpInCurrentLevel = totalXP % XP_PER_LEVEL
        val progressPercent = (xpInCurrentLevel.toFloat() / XP_PER_LEVEL.toFloat() * 100).toInt()

        // Update level text indicators
        binding.currentLevelTextProfile.text = "Level $currentLevel"
        binding.nextLevelTextProfile.text = "Level $nextLevel"
        binding.xpProgressTextProfile.text = "$xpInCurrentLevel/$XP_PER_LEVEL XP"

        // Update progress bar width dynamically
        val progressBarParams = binding.xpProgressBar.layoutParams
        progressBarParams.width = 0 // Will be set based on percentage
        binding.xpProgressBar.layoutParams = progressBarParams

        // Post to ensure layout is ready
        binding.xpProgressBar.post {
            val containerWidth = binding.xpProgressBar.parent.let {
                if (it is View) it.width else 0
            }

            val newWidth = (containerWidth * progressPercent / 100)
            val params = binding.xpProgressBar.layoutParams
            params.width = newWidth
            binding.xpProgressBar.layoutParams = params
        }

        // Add checkpoint markers for reached levels
        binding.checkpointMarkersContainer.removeAllViews()

        // Calculate how many checkpoints to show (levels already completed)
        val completedLevels = totalXP / XP_PER_LEVEL

        // Post to add checkpoint markers after layout is ready
        binding.checkpointMarkersContainer.post {
            val containerWidth = binding.checkpointMarkersContainer.width

            if (containerWidth > 0 && completedLevels > 0) {
                // Add checkpoint markers for completed levels
                // We show markers to indicate previous level completions
                for (i in 1..minOf(completedLevels, 5)) { // Show up to 5 checkpoints
                    val checkpoint = ImageView(this)
                    checkpoint.setImageResource(R.drawable.milestone_marker)

                    val size = 20 // 20dp marker size
                    val sizeInPx = (size * resources.displayMetrics.density).toInt()

                    val params = FrameLayout.LayoutParams(sizeInPx, sizeInPx)

                    // Position marker at the start of the bar to indicate completed level
                    // For the current level progression, we position at the left as a reference point
                    params.leftMargin = (containerWidth * 0.05 * i).toInt() // Spread them out

                    checkpoint.layoutParams = params
                    binding.checkpointMarkersContainer.addView(checkpoint)
                }
            }
        }

        // Show achievement message if user just reached a level
        if (totalXP % XP_PER_LEVEL == 0 && totalXP > 0) {
            binding.checkpointText.visibility = View.VISIBLE
            binding.checkpointText.text = "🏆 Level $currentLevel Checkpoint Reached! Achievement Unlocked!"
        } else {
            binding.checkpointText.visibility = View.GONE
        }

        Log.d(TAG, "Progress bar updated: $xpInCurrentLevel/$XP_PER_LEVEL XP ($progressPercent%)")
        Log.d(TAG, "Completed levels: $completedLevels, Current level: $currentLevel")
    }

    private fun loadEnrolledCourses() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user")
            binding.noCoursesText.visibility = View.VISIBLE
            return
        }

        Log.d(TAG, "Loading enrolled courses for user: ${currentUser.uid}")

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val enrolledCoursesData = document.get("courseTaken") as? List<Map<String, Any>> ?: listOf()

                    Log.d(TAG, "Found ${enrolledCoursesData.size} enrolled courses")

                    if (enrolledCoursesData.isEmpty()) {
                        binding.enrolledCoursesRecyclerView.visibility = View.GONE
                        binding.noCoursesText.visibility = View.VISIBLE
                        binding.coursesCountBadge.text = "0"
                        return@addOnSuccessListener
                    }

                    val coursesList = mutableListOf<EnrolledCourseItem>()

                    // Process each enrolled course
                    enrolledCoursesData.forEach { courseData ->
                        val courseId = courseData["courseId"] as? String ?: ""
                        val courseName = courseData["courseName"] as? String ?: "Unknown Course"
                        val category = courseData["category"] as? String ?: "General"
                        val difficulty = courseData["difficulty"] as? String ?: "Beginner"
                        val enrolledAt = courseData["enrolledAt"] as? Long ?: 0L

                        // Calculate progress for this course
                        progressManager.loadCompletedLessons(courseId) { completedLessons ->
                            // Get total lessons for this course from Firestore
                            firestore.collection("courses")
                                .document(courseId)
                                .collection("modules")
                                .get()
                                .addOnSuccessListener { modulesSnapshot ->
                                    var totalLessons = 0
                                    var processedModules = 0
                                    val moduleCount = modulesSnapshot.size()

                                    if (moduleCount == 0) {
                                        // No modules, add course with 0% progress
                                        coursesList.add(
                                            EnrolledCourseItem(
                                                courseId = courseId,
                                                courseName = courseName,
                                                category = category,
                                                difficulty = difficulty,
                                                enrolledAt = enrolledAt,
                                                progress = 0
                                            )
                                        )
                                        updateCoursesUI(coursesList)
                                        return@addOnSuccessListener
                                    }

                                    modulesSnapshot.documents.forEach { moduleDoc ->
                                        firestore.collection("courses")
                                            .document(courseId)
                                            .collection("modules")
                                            .document(moduleDoc.id)
                                            .collection("lessons")
                                            .get()
                                            .addOnSuccessListener { lessonsSnapshot ->
                                                totalLessons += lessonsSnapshot.size()
                                                processedModules++

                                                if (processedModules == moduleCount) {
                                                    val progress = if (totalLessons > 0) {
                                                        (completedLessons.size * 100) / totalLessons
                                                    } else 0

                                                    coursesList.add(
                                                        EnrolledCourseItem(
                                                            courseId = courseId,
                                                            courseName = courseName,
                                                            category = category,
                                                            difficulty = difficulty,
                                                            enrolledAt = enrolledAt,
                                                            progress = progress
                                                        )
                                                    )
                                                    updateCoursesUI(coursesList)
                                                }
                                            }
                                    }
                                }
                        }
                    }
                } else {
                    binding.enrolledCoursesRecyclerView.visibility = View.GONE
                    binding.noCoursesText.visibility = View.VISIBLE
                    binding.coursesCountBadge.text = "0"
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading enrolled courses", e)
                binding.enrolledCoursesRecyclerView.visibility = View.GONE
                binding.noCoursesText.visibility = View.VISIBLE
                binding.coursesCountBadge.text = "0"
            }
    }

    private fun updateCoursesUI(coursesList: List<EnrolledCourseItem>) {
        Log.d(TAG, "updateCoursesUI called with ${coursesList.size} courses")
        coursesList.forEach { course ->
            Log.d(TAG, "  Course: ${course.courseName}, Progress: ${course.progress}%")
        }

        if (coursesList.isEmpty()) {
            binding.enrolledCoursesRecyclerView.visibility = View.GONE
            binding.noCoursesText.visibility = View.VISIBLE
            binding.coursesCountBadge.text = "0"
        } else {
            binding.enrolledCoursesRecyclerView.visibility = View.VISIBLE
            binding.noCoursesText.visibility = View.GONE
            binding.coursesCountBadge.text = coursesList.size.toString()
            enrolledCoursesAdapter.updateCourses(coursesList)
            Log.d(TAG, "RecyclerView updated and made visible")
        }
    }

    private fun loadQuizHistory() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user")
            binding.noQuizzesText.visibility = View.VISIBLE
            return
        }

        Log.d(TAG, "Loading quiz history for user: ${currentUser.uid}")

        // Use the new QuizScoreManager to fetch recent attempts
        quizScoreManager.getAllRecentAttemptsFromFirestore(limit = 20) { attempts ->
            if (attempts.isEmpty()) {
                binding.quizHistoryRecyclerView.visibility = View.GONE
                binding.noQuizzesText.visibility = View.VISIBLE
                binding.quizCountBadge.text = "0"
                binding.viewAllQuizzesBtn.visibility = View.GONE
                return@getAllRecentAttemptsFromFirestore
            }

            // Convert QuizAttempt to QuizHistoryItem for adapter
            val quizHistoryList = attempts.map { attempt ->
                QuizHistoryItem(
                    quizId = attempt.quizId,
                    courseId = attempt.courseId,
                    courseName = attempt.courseName,
                    score = attempt.score,
                    totalQuestions = attempt.totalQuestions,
                    completedAt = attempt.timestamp,
                    difficulty = attempt.difficulty
                )
            }

            Log.d(TAG, "Loaded ${quizHistoryList.size} quiz attempts")

            // Store the full list
            allQuizHistory = quizHistoryList

            // Show only first 3 items in the UI
            val displayedList = quizHistoryList.take(3)

            binding.quizHistoryRecyclerView.visibility = View.VISIBLE
            binding.noQuizzesText.visibility = View.GONE
            binding.quizCountBadge.text = quizHistoryList.size.toString()

            // Show "View All" button only if there are more than 3 quizzes
            if (quizHistoryList.size > 3) {
                binding.viewAllQuizzesBtn.visibility = View.VISIBLE
                binding.viewAllQuizzesBtn.text = "View All (${quizHistoryList.size})"
            } else {
                binding.viewAllQuizzesBtn.visibility = View.GONE
            }

            quizHistoryAdapter.updateQuizHistory(displayedList)
        }
    }

    private fun showAllQuizzes() {
        if (allQuizHistory.isEmpty()) {
            DialogUtils.showInfoDialog(this, "No History", "No quiz history available")
            return
        }

        // Create a dialog to show all quizzes
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("All Quiz Results (${allQuizHistory.size})")

        // Create a RecyclerView for the dialog
        val recyclerView = androidx.recyclerview.widget.RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val allQuizzesAdapter = QuizHistoryAdapter(
            this,
            allQuizHistory.toMutableList()
        ) { quiz ->
            DialogUtils.showInfoDialog(
                this,
                "Quiz Details",
                "Quiz from ${quiz.courseName}: ${quiz.score}/${quiz.totalQuestions}"
            )
        }
        recyclerView.adapter = allQuizzesAdapter
        recyclerView.setPadding(16, 16, 16, 16)

        dialogBuilder.setView(recyclerView)
        dialogBuilder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun loadAchievements() {
        val userId = auth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get user's total XP
                val userDoc = withContext(Dispatchers.IO) {
                    firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()
                }

                val totalXP = userDoc.getLong("totalXP")?.toInt() ?: 0

                // Create list of all achievements with their unlock status
                val achievementList = listOf(
                    AchievementBadgeItem(
                        name = "Bronze",
                        description = "500 XP",
                        requiredXP = 500,
                        badgeDrawable = R.drawable.badge_bronze,
                        isUnlocked = totalXP >= 500
                    ),
                    AchievementBadgeItem(
                        name = "Silver",
                        description = "1,000 XP",
                        requiredXP = 1000,
                        badgeDrawable = R.drawable.badge_silver,
                        isUnlocked = totalXP >= 1000
                    ),
                    AchievementBadgeItem(
                        name = "Gold",
                        description = "2,000 XP",
                        requiredXP = 2000,
                        badgeDrawable = R.drawable.badge_gold,
                        isUnlocked = totalXP >= 2000
                    ),
                    AchievementBadgeItem(
                        name = "Platinum",
                        description = "3,000 XP",
                        requiredXP = 3000,
                        badgeDrawable = R.drawable.badge_platinum,
                        isUnlocked = totalXP >= 3000
                    ),
                    AchievementBadgeItem(
                        name = "Diamond",
                        description = "5,000 XP",
                        requiredXP = 5000,
                        badgeDrawable = R.drawable.badge_diamond,
                        isUnlocked = totalXP >= 5000
                    )
                )

                // Update adapter with achievement data
                achievementBadgeAdapter = AchievementBadgeAdapter(achievementList)
                binding.achievementBadgesRecyclerView.adapter = achievementBadgeAdapter

                // Update achievement count
                val unlockedCount = achievementList.count { it.isUnlocked }
                binding.achievementsCountBadge.text = "$unlockedCount/5"

                Log.d(TAG, "✅ Loaded achievements: $unlockedCount unlocked")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading achievements", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to the activity
        loadUserProfile()
        loadEnrolledCourses()
        loadQuizHistory()
        loadAchievements()
    }
}
