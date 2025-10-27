package com.labactivity.lala

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.labactivity.lala.LEADERBOARDPAGE.Leaderboard
import com.labactivity.lala.databinding.ActivityProfileMain5Binding
import com.labactivity.lala.homepage.MainActivity4
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import java.util.UUID

class ProfileMainActivity5 : BaseActivity() {

    private lateinit var binding: ActivityProfileMain5Binding
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ProfileMainActivity5"

    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                uploadImageToFirebase(uri)
            }
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
            openImagePicker()
        }

        // Edit profile button
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Profile image click to upload
        binding.profileImageCard.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading indicator
        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show()

        // Create a unique filename
        val filename = "profile_photos/${currentUser.uid}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(filename)

        // Upload the file
        storageRef.putFile(imageUri)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d(TAG, "Upload is $progress% done")
            }
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "Image uploaded successfully")

                // Get download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val photoUrl = downloadUri.toString()
                    Log.d(TAG, "Download URL: $photoUrl")

                    // Update Firestore with new photo URL
                    updateProfilePhotoUrl(photoUrl)

                    // Display the image immediately
                    displayProfileImage(photoUrl)

                    Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get download URL", e)
                    Toast.makeText(this, "Failed to get photo URL", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Upload failed", e)
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfilePhotoUrl(photoUrl: String) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users")
            .document(currentUser.uid)
            .update("profilePhotoUrl", photoUrl)
            .addOnSuccessListener {
                Log.d(TAG, "Profile photo URL updated in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update profile photo URL", e)
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
                    val profilePhotoUrl = document.getString("profilePhotoUrl")

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
                    if (!profilePhotoUrl.isNullOrEmpty()) {
                        displayProfileImage(profilePhotoUrl)
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

    private fun displayProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.user)
            .error(R.drawable.user)
            .circleCrop()
            .into(binding.imageView3)
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to the activity
        loadUserProfile()
    }
}
