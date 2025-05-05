package com.example.easintasty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.easintasty.databinding.ActivityProfileBinding
import com.example.easintasty.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adminReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adminReference = database.reference.child("user")

        binding.back.setOnClickListener {
            finish()
        }

        binding.save.setOnClickListener {
            updateUserData()
        }

        binding.name.isEnabled = false
        binding.mail.isEnabled = false
        binding.password.isEnabled = false
        binding.save.isEnabled = false

        var isEnable = false
        binding.edit.setOnClickListener {
            isEnable = ! isEnable
            binding.name.isEnabled = isEnable
            binding.mail.isEnabled = isEnable
            binding.password.isEnabled = isEnable
            if (isEnable) {
                binding.name.requestFocus()
            }
            binding.save.isEnabled = isEnable
        }

        binding.subscription.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }

        binding.settingact.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        binding.favlay.setOnClickListener {
            startActivity(Intent(this, FavouriteActivity::class.java))
        }

        // Retrieve and display the current admin data
        retrieveData()
    }

    private fun retrieveData() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val userReference = adminReference.child(currentUserUid)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var name = snapshot.child("name").getValue()
                        var mail = snapshot.child("email").getValue()
                        var password = snapshot.child("password").getValue()
                        Log.d("TAG", "onDataChange: $mail")
                        setDatatoTextView(name, mail, password)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun setDatatoTextView(name: Any?, email: Any?, password: Any?) {
        binding.name.setText(name.toString())
        binding.mail.setText(email.toString())
        binding.password.setText(password.toString())
    }


    private fun updateUserData() {
        val updateName = binding.name.text.toString()
        val updateEmail = binding.mail.text.toString()
        val updatePassword = binding.password.text.toString()
        // Get the current user's ID
        val userId = auth.currentUser?.uid
        // Check if userId is not null
        if (userId != null) {
            // Create an instance of UserModel with the updated data
            val userData = UserModel(updateName, updateEmail, updatePassword)
            // Reference the user's data in the database using the userId
            val userReference = FirebaseDatabase.getInstance().getReference("users/$userId")
            // Update the user's data in the database
            userReference.setValue(userData).addOnSuccessListener {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                // Update the user's email and password
                auth.currentUser?.updateEmail(updateEmail)
                auth.currentUser?.updatePassword(updatePassword)
            }.addOnFailureListener {
                Toast.makeText(this, "Profile Update Failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
