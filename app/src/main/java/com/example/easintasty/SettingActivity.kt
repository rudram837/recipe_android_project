package com.example.easintasty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easintasty.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Sign out logic
        binding.signout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        // Navigate to ProfileActivity
        binding.accbtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        // Close current activity on image click
        binding.imageView4.setOnClickListener {
            finish()
        }

        // Account deletion logic
        binding.deleteAccountBtn.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                deleteAccount(userId)
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteAccount(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("user").child(userId)

        // Remove user data from Realtime Database
        databaseReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // After successfully deleting user data, delete subscription info
                deleteSubscriptionData(userId)
            } else {
                // Failed to delete data from Realtime Database
                val error = task.exception?.message
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                Log.e("SettingActivity", "Error deleting user data: $error")
            }
        }
    }

    private fun deleteSubscriptionData(userId: String) {
        val userReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Create a map to hold the keys to delete
        val updates = hashMapOf<String, Any?>(
            "subscribe" to null,
            "subscriptionTime" to null
        )

        // Remove user's subscription data and subscriptionTime
        userReference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Now, delete the user authentication after subscription data is removed
                auth.currentUser?.delete()?.addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        Toast.makeText(this, "Account and subscription successfully deleted.", Toast.LENGTH_SHORT).show()
                        Log.d("SettingActivity", "Account and subscription successfully deleted.")

                        // Navigate to SignUpActivity after account deletion
                        startActivity(Intent(this, SignUpActivity::class.java))
                        finish()
                    } else {
                        // Failed to delete the user from authentication
                        val error = authTask.exception?.message
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                        Log.e("SettingActivity", "Error deleting account: $error")
                    }
                }
            } else {
                // Failed to delete subscription data from Realtime Database
                val error = task.exception?.message
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                Log.e("SettingActivity", "Error deleting subscription data: $error")
            }
        }
    }
}
