package com.labactivity.lala.ProfileMainActivity5
// lapuk
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
import com.labactivity.lala.PROGRESSPAGE.UserProgressActivity
import com.labactivity.lala.ProfileMainActivity5.TechnicalAssessmentItem
import com.labactivity.lala.ProfileMainActivity5.TechnicalAssessmentsAdapter

class ProfileMainActivity5 : BaseActivity() {

    private lateinit var binding: ActivityProfileMain5Binding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ProfileMainActivity5"

    private var selectedImageUri: Uri? = null

    // Adapters for assessments and quizzes
    private lateinit var technicalAssessmentsAdapter: TechnicalAssessmentsAdapter
    private lateinit var quizHistoryAdapter: QuizHistoryAdapter
    private lateinit var achievementBadgeAdapter: AchievementBadgeAdapter
    private lateinit var progressManager: ModuleProgressManager
    private lateinit var quizScoreManager: QuizScoreManager
    private lateinit var xpManager: XPManager
    private lateinit var achievementManager: AchievementManager
    private var allQuizHistory: List<QuizHistoryItem> = listOf()
    private var allAssessments: List<TechnicalAssessmentItem> = listOf()

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
        loadTechnicalAssessments()
        loadQuizHistory()
        loadAchievements()
    }

    private fun setupRecyclerViews() {
        // Setup technical assessments RecyclerView
        technicalAssessmentsAdapter = TechnicalAssessmentsAdapter(
            this,
            mutableListOf()
        ) { assessment ->
            // Navigate to CompilerActivity when assessment is clicked
            if (!assessment.isUnlocked) {
                val message = when (assessment.difficulty.lowercase()) {
                    "medium" -> "Complete all Easy challenges to unlock Medium difficulty."
                    "hard" -> "Complete all Easy and Medium challenges to unlock Hard difficulty."
                    else -> "This assessment is currently locked."
                }
                DialogUtils.showLockedDialog(
                    context = this,
                    title = "🔒 Assessment Locked",
                    message = message
                )
                return@TechnicalAssessmentsAdapter
            }

            // Open the appropriate compiler based on category
            val compilerIntent = Intent(this, com.labactivity.lala.PYTHONCOMPILER.CompilerActivity::class.java).apply {
                putExtra("CHALLENGE_ID", assessment.id)
                putExtra("CHALLENGE_TITLE", assessment.title)
                putExtra("COURSE_ID", assessment.courseId)
            }
            startActivity(compilerIntent)
        }

        binding.enrolledCoursesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProfileMainActivity5)
            adapter = technicalAssessmentsAdapter
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

        // View All Assessments button
        binding.viewAllAssessmentsBtn.setOnClickListener {
            showAllAssessments()
        }

        // Courses Stat Card - Navigate to Home page courses section
        binding.coursesStatCard.setOnClickListener {
            Log.d(TAG, "Courses card clicked - navigating to home")
            val intent = Intent(this, MainActivity4::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Quizzes Stat Card - Scroll to quiz history section
        binding.quizzesStatCard.setOnClickListener {
            Log.d(TAG, "Quizzes card clicked - scrolling to quiz history")
            // Scroll to quiz history section
            binding.scrollView.post {
                val quizHistoryCard = binding.quizHistoryCard
                val scrollY = quizHistoryCard.top
                binding.scrollView.smoothScrollTo(0, scrollY)
            }
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
                R.id.nav_settings to SettingsActivity::class.java,
                R.id.user_progress to UserProgressActivity::class.java
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

        // Always navigate to Leaderboard activity
        // The Leaderboard activity will handle showing either:
        // - The leaderboard (if XP >= 500)
        // - The ineligible layout (if XP < 500)
        Log.d(TAG, "Navigating to Leaderboard activity")
        val intent = Intent(this, Leaderboard::class.java)
        startActivity(intent)
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
                            val quizzesTaken = (document.getLong("quizzesTaken") ?: 0).toInt()
                            val technicalAssessments = (document.getLong("technicalAssessmentsCompleted") ?: 0).toInt()
                            val profilePhotoBase64 = document.getString("profilePhotoBase64")
                            val currentBadge = document.getString("currentBadge") ?: Achievement.TIER_NONE

                            // Get actual enrolled courses count
                            val enrolledCoursesData = document.get("courseTaken") as? List<Map<String, Any>> ?: listOf()
                            val coursesCount = enrolledCoursesData.size

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
                            // Display dynamic counts
                            binding.textCoursesValue.text = coursesCount.toString()
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

    private fun loadTechnicalAssessments() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user")
            binding.noCoursesText.visibility = View.VISIBLE
            return
        }

        Log.d(TAG, "Loading taken technical assessments for user: ${currentUser.uid}")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch only assessments that the user has taken (from user_progress)
                val userProgressSnapshot = withContext(Dispatchers.IO) {
                    firestore.collection("user_progress")
                        .document(currentUser.uid)
                        .collection("technical_assessment_progress")
                        .get()
                        .await()
                }

                if (userProgressSnapshot.isEmpty) {
                    // Hide assessment section if no assessments taken
                    binding.coursesCard.visibility = View.GONE
                    binding.enrolledCoursesRecyclerView.visibility = View.GONE
                    binding.noCoursesText.visibility = View.VISIBLE
                    binding.coursesCountBadge.text = "0"
                    Log.d(TAG, "No assessments taken yet")
                    return@launch
                }

                // Show assessment section
                binding.coursesCard.visibility = View.VISIBLE

                val assessmentsList = mutableListOf<TechnicalAssessmentItem>()

                // For each taken assessment, fetch the challenge details
                for (progressDoc in userProgressSnapshot.documents) {
                    val challengeId = progressDoc.id
                    val progressStatus = progressDoc.getString("status") ?: "not_started"
                    val bestScore = progressDoc.getLong("bestScore")?.toInt() ?: 0
                    val attempts = progressDoc.getLong("attempts")?.toInt() ?: 0
                    val passed = progressDoc.getBoolean("passed") ?: false
                    val challengeTitle = progressDoc.getString("challengeTitle") ?: ""

                    // Fetch the challenge details from technical_assesment collection
                    val challengeDoc = withContext(Dispatchers.IO) {
                        firestore.collection("technical_assesment")
                            .document(challengeId)
                            .get()
                            .await()
                    }

                    if (challengeDoc.exists()) {
                        val title = challengeDoc.getString("title") ?: challengeTitle
                        val difficulty = challengeDoc.getString("difficulty") ?: "Unknown"
                        val courseId = challengeDoc.getString("courseId") ?: ""
                        val category = challengeDoc.getString("category") ?: ""

                        assessmentsList.add(
                            TechnicalAssessmentItem(
                                id = challengeId,
                                title = title,
                                difficulty = difficulty,
                                courseId = courseId,
                                category = category,
                                status = progressStatus,
                                isUnlocked = true,  // Already taken, so unlocked
                                bestScore = bestScore,
                                attempts = attempts,
                                passed = passed
                            )
                        )
                    } else {
                        // If challenge doc doesn't exist, use data from progress
                        Log.w(TAG, "Challenge document not found for ID: $challengeId, using progress data")
                        assessmentsList.add(
                            TechnicalAssessmentItem(
                                id = challengeId,
                                title = challengeTitle,
                                difficulty = "Unknown",
                                courseId = "",
                                category = "Assessment",
                                status = progressStatus,
                                isUnlocked = true,
                                bestScore = bestScore,
                                attempts = attempts,
                                passed = passed
                            )
                        )
                    }
                }

                // Sort by status (completed last) and then by difficulty
                val sortedAssessments = assessmentsList.sortedWith(
                    compareBy<TechnicalAssessmentItem> { it.passed }
                        .thenBy {
                            when(it.difficulty.lowercase()) {
                                "easy" -> 1
                                "medium" -> 2
                                "hard" -> 3
                                else -> 4
                            }
                        }
                )

                updateAssessmentsUI(sortedAssessments)

                Log.d(TAG, "Loaded ${sortedAssessments.size} taken technical assessments")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading technical assessments", e)
                binding.enrolledCoursesRecyclerView.visibility = View.GONE
                binding.noCoursesText.visibility = View.VISIBLE
                binding.coursesCountBadge.text = "0"
            }
        }
    }

    private fun updateAssessmentsUI(assessmentsList: List<TechnicalAssessmentItem>) {
        Log.d(TAG, "updateAssessmentsUI called with ${assessmentsList.size} assessments")
        assessmentsList.forEach { assessment ->
            Log.d(TAG, "  Assessment: ${assessment.title}, Status: ${assessment.status}, Locked: ${!assessment.isUnlocked}")
        }

        // Store all assessments
        allAssessments = assessmentsList

        if (assessmentsList.isEmpty()) {
            binding.enrolledCoursesRecyclerView.visibility = View.GONE
            binding.noCoursesText.visibility = View.VISIBLE
            binding.viewAllAssessmentsBtn.visibility = View.GONE
            binding.coursesCountBadge.text = "0"
        } else {
            // Show only first 2 assessments
            val displayedList = assessmentsList.take(2)

            binding.enrolledCoursesRecyclerView.visibility = View.VISIBLE
            binding.noCoursesText.visibility = View.GONE
            binding.coursesCountBadge.text = assessmentsList.size.toString()
            technicalAssessmentsAdapter.updateAssessments(displayedList)

            // Show "View All" button only if there are more than 2 assessments
            if (assessmentsList.size > 2) {
                binding.viewAllAssessmentsBtn.visibility = View.VISIBLE
                binding.viewAllAssessmentsBtn.text = "View All (${assessmentsList.size})"
            } else {
                binding.viewAllAssessmentsBtn.visibility = View.GONE
            }

            Log.d(TAG, "RecyclerView updated with ${displayedList.size} assessments (${assessmentsList.size} total)")
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

    private fun showAllAssessments() {
        if (allAssessments.isEmpty()) {
            DialogUtils.showInfoDialog(this, "No Assessments", "No assessments taken yet")
            return
        }

        // Create a dialog to show all assessments
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("All Technical Assessments (${allAssessments.size})")

        // Create a RecyclerView for the dialog
        val recyclerView = androidx.recyclerview.widget.RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val allAssessmentsAdapter = TechnicalAssessmentsAdapter(
            this,
            allAssessments.toMutableList()
        ) { assessment ->
            // Navigate to CompilerActivity when assessment is clicked
            if (!assessment.isUnlocked) {
                val message = when (assessment.difficulty.lowercase()) {
                    "medium" -> "Complete all Easy challenges to unlock Medium difficulty."
                    "hard" -> "Complete all Easy and Medium challenges to unlock Hard difficulty."
                    else -> "This assessment is currently locked."
                }
                DialogUtils.showLockedDialog(
                    context = this,
                    title = "🔒 Assessment Locked",
                    message = message
                )
                return@TechnicalAssessmentsAdapter
            }

            // Open the appropriate compiler based on category
            val compilerIntent = Intent(this, com.labactivity.lala.PYTHONCOMPILER.CompilerActivity::class.java).apply {
                putExtra("CHALLENGE_ID", assessment.id)
                putExtra("CHALLENGE_TITLE", assessment.title)
                putExtra("COURSE_ID", assessment.courseId)
            }
            startActivity(compilerIntent)
        }
        recyclerView.adapter = allAssessmentsAdapter
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
                // Python Badges
                val achievementList = listOf(
                    // Python Bronze
                    AchievementBadgeItem(
                        name = "Python Bronze",
                        description = "500 XP in Python",
                        requiredXP = 500,
                        badgeDrawable = R.drawable.badge_python_bronze,
                        isUnlocked = totalXP >= 500
                    ),
                    // Python Silver
                    AchievementBadgeItem(
                        name = "Python Silver",
                        description = "1,000 XP in Python",
                        requiredXP = 1000,
                        badgeDrawable = R.drawable.badge_python_silver,
                        isUnlocked = totalXP >= 1000
                    ),
                    // Python Gold
                    AchievementBadgeItem(
                        name = "Python Gold",
                        description = "2,000 XP in Python",
                        requiredXP = 2000,
                        badgeDrawable = R.drawable.badge_python_gold,
                        isUnlocked = totalXP >= 2000
                    ),
                    // Python Diamond
                    AchievementBadgeItem(
                        name = "Python Diamond",
                        description = "3,000 XP in Python",
                        requiredXP = 3000,
                        badgeDrawable = R.drawable.badge_python_diamond,
                        isUnlocked = totalXP >= 3000
                    ),
                    // Python Master
                    AchievementBadgeItem(
                        name = "Python Master",
                        description = "5,000 XP in Python",
                        requiredXP = 5000,
                        badgeDrawable = R.drawable.badge_python_master,
                        isUnlocked = totalXP >= 5000
                    ),

                    // Java Badges
                    // Java Bronze
                    AchievementBadgeItem(
                        name = "Java Bronze",
                        description = "500 XP in Java",
                        requiredXP = 500,
                        badgeDrawable = R.drawable.badge_java_bronze,
                        isUnlocked = totalXP >= 500
                    ),
                    // Java Silver
                    AchievementBadgeItem(
                        name = "Java Silver",
                        description = "1,000 XP in Java",
                        requiredXP = 1000,
                        badgeDrawable = R.drawable.badge_java_silver,
                        isUnlocked = totalXP >= 1000
                    ),
                    // Java Gold
                    AchievementBadgeItem(
                        name = "Java Gold",
                        description = "2,000 XP in Java",
                        requiredXP = 2000,
                        badgeDrawable = R.drawable.badge_java_gold,
                        isUnlocked = totalXP >= 2000
                    ),
                    // Java Diamond
                    AchievementBadgeItem(
                        name = "Java Diamond",
                        description = "3,000 XP in Java",
                        requiredXP = 3000,
                        badgeDrawable = R.drawable.badge_java_diamond,
                        isUnlocked = totalXP >= 3000
                    ),
                    // Java Master
                    AchievementBadgeItem(
                        name = "Java Master",
                        description = "5,000 XP in Java",
                        requiredXP = 5000,
                        badgeDrawable = R.drawable.badge_java_master,
                        isUnlocked = totalXP >= 5000
                    ),

                    // SQL Badges
                    // SQL Bronze
                    AchievementBadgeItem(
                        name = "SQL Bronze",
                        description = "500 XP in SQL",
                        requiredXP = 500,
                        badgeDrawable = R.drawable.badge_sql_bronze,
                        isUnlocked = totalXP >= 500
                    ),
                    // SQL Silver
                    AchievementBadgeItem(
                        name = "SQL Silver",
                        description = "1,000 XP in SQL",
                        requiredXP = 1000,
                        badgeDrawable = R.drawable.badge_sql_silver,
                        isUnlocked = totalXP >= 1000
                    ),
                    // SQL Gold
                    AchievementBadgeItem(
                        name = "SQL Gold",
                        description = "2,000 XP in SQL",
                        requiredXP = 2000,
                        badgeDrawable = R.drawable.badge_sql_gold,
                        isUnlocked = totalXP >= 2000
                    ),
                    // SQL Diamond
                    AchievementBadgeItem(
                        name = "SQL Diamond",
                        description = "3,000 XP in SQL",
                        requiredXP = 3000,
                        badgeDrawable = R.drawable.badge_sql_diamond,
                        isUnlocked = totalXP >= 3000
                    ),
                    // SQL Master
                    AchievementBadgeItem(
                        name = "SQL Master",
                        description = "5,000 XP in SQL",
                        requiredXP = 5000,
                        badgeDrawable = R.drawable.badge_sql_master,
                        isUnlocked = totalXP >= 5000
                    )
                )

                // Update adapter with achievement data
                achievementBadgeAdapter = AchievementBadgeAdapter(achievementList)
                binding.achievementBadgesRecyclerView.adapter = achievementBadgeAdapter

                // Update achievement count
                val unlockedCount = achievementList.count { it.isUnlocked }
                binding.achievementsCountBadge.text = "$unlockedCount/15"

                Log.d(TAG, "✅ Loaded achievements: $unlockedCount unlocked out of 15")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading achievements", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to the activity
        loadUserProfile()
        loadTechnicalAssessments()
        loadQuizHistory()
        loadAchievements()
    }
}
