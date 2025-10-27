package com.labactivity.lala

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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.lala.LEADERBOARDPAGE.Leaderboard
import com.labactivity.lala.databinding.ActivityProfileMain5Binding
import com.labactivity.lala.homepage.MainActivity4
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileMainActivity5 : BaseActivity() {

    private lateinit var binding: ActivityProfileMain5Binding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ProfileMainActivity5"

    private var selectedImageUri: Uri? = null

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Permission granted")
            openImagePicker()
        } else {
            Log.w(TAG, "Permission denied")
            Toast.makeText(this, "Permission denied. Cannot access photos.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Image picker cancelled or failed. Result code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileMain5Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadUserProfile()
    }

    private fun setupClickListeners() {
        // Back button
        binding.imageView2.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
            finish()
        }

        // Leaderboard button
        binding.Leaderboard.setOnClickListener {
            Log.d(TAG, "Leaderboard clicked")
            val intent = Intent(this, Leaderboard::class.java)
            startActivity(intent)
            finish()
        }

        // Upload photo button
        binding.btnUploadPhoto.setOnClickListener {
            Log.d(TAG, "Upload photo clicked")
            checkPermissionAndOpenPicker()
        }

        // Edit profile button
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Profile image click to upload
        binding.profileImageCard.setOnClickListener {
            checkPermissionAndOpenPicker()
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
            Toast.makeText(this, "Error opening gallery: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToFirestore(imageUri: Uri) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Starting image save for URI: $imageUri")
        Toast.makeText(this, "Processing image...", Toast.LENGTH_SHORT).show()

        try {
            // Convert image to Base64
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                return
            }

            // Compress and convert to Base64
            val base64Image = compressAndEncodeImage(bitmap)

            if (base64Image == null) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "Image compressed. Size: ${base64Image.length} characters")

            // Display image immediately
            displayProfileImageFromBase64(base64Image)

            // Save to Firestore
            Toast.makeText(this, "Saving photo...", Toast.LENGTH_SHORT).show()

            firestore.collection("users")
                .document(currentUser.uid)
                .update("profilePhotoBase64", base64Image)
                .addOnSuccessListener {
                    Log.d(TAG, "Profile photo saved to Firestore successfully")
                    Toast.makeText(this, "Photo saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save photo to Firestore", e)
                    Toast.makeText(this, "Failed to save photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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

        // Load user data from Firestore
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
                    val profilePhotoBase64 = document.getString("profilePhotoBase64")

                    // Update UI
                    binding.textUsername.text = username
                    binding.textEmail.text = email
                    binding.textXpValue.text = totalXP.toString()
                    binding.textCoursesValue.text = coursesCompleted.toString()
                    binding.textExercisesValue.text = quizzesTaken.toString()

                    // Update progress bar
                    binding.xpProgressBar.progress = totalXP
                    binding.progressText.text = "$totalXP / 500 XP"

                    // Load profile photo if available
                    if (!profilePhotoBase64.isNullOrEmpty()) {
                        displayProfileImageFromBase64(profilePhotoBase64)
                    }

                    Log.d(TAG, "User profile loaded successfully")
                } else {
                    Log.w(TAG, "No user document found")
                    // Set default values
                    binding.textUsername.text = "@${currentUser.email?.split("@")?.get(0)}"
                    binding.textEmail.text = currentUser.email ?: "No email"
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user profile", e)
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to the activity
        loadUserProfile()
    }
}
