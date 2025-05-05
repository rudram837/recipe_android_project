package com.example.easintasty

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.easintasty.databinding.ActivitySubscriptionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class SubscriptionActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private var subscriptionListener: ValueEventListener? = null // To hold the listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Set up click listeners using binding
        binding.plans.setOnClickListener {
            startPayment(amount = 5000, email = "rudramaurya313@gmail.com", contact = "8369783733")
        }

        binding.plansv.setOnClickListener {
            startPayment(amount = 7500, email = "rudramaurya313@gmail.com", contact = "8369783733")
        }

        binding.go.setOnClickListener {
            finish()
        }

        // Fetch subscription status for the current user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get the unique user ID
            databaseReference = database.child("user").child(userId)
            fetchSubscriptionStatus() // Fetch subscription status
        } else {
            binding.methods.text = "No user is currently logged in"
        }

        // Call the checkUserSubscription function
        checkUserSubscription()
    }


    private fun startPayment(amount: Int, email: String, contact: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_U1DIfJyKCywMJS") // Replace with your Razorpay API key

        val paymentOptions = JSONObject()
        try {
            paymentOptions.put("name", "Easintasty Subscription")
            paymentOptions.put("description", "Payment for Subscription Plan")
            paymentOptions.put("currency", "INR") // Set currency
            paymentOptions.put("amount", amount) // Amount in paise

            // Add customer details
            val prefill = JSONObject()
            prefill.put("email", email)
            prefill.put("contact", contact)
            paymentOptions.put("prefill", prefill)

            // Start the payment
            checkout.open(this, paymentOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error in starting payment: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle payment success
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()
        // Handle successful payment here (e.g., update the server or UI)
        // Assuming the user has subscribed
        updateUserSubscriptionStatus(isSubscribed = true)
    }

    // Handle payment failure
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
        // Handle failed payment here (e.g., show error message to the user)
    }

    private fun updateUserSubscriptionStatus(isSubscribed: Boolean) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.child("user").child(userId)

            // Map to hold the data for updating subscription status and time
            val updates = mutableMapOf<String, Any>(
                "subscribe" to isSubscribed
            )

            if (isSubscribed) {
                val oneMinuteInMillis = 60L * 1000L // 1 minute in milliseconds
                //val twentyEightDaysInMillis = 28L * 24L * 60L * 60L * 1000L  28 days in milliseconds
                val subscriptionExpirationTime = System.currentTimeMillis() + oneMinuteInMillis
                updates["subscriptionTime"] = subscriptionExpirationTime
            } else {
                // Reset the subscription time if unsubscribed
                updates["subscriptionTime"] = 0L
            }

            // Update the data in the database
            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    // Successfully updated
                    Toast.makeText(this, "Subscription status updated successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Toast.makeText(this, "Failed to update subscription status: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No user is currently signed in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchSubscriptionStatus() {
        subscriptionListener = databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch the subscription status
                    val subscribeStatus = dataSnapshot.child("subscribe").getValue(Boolean::class.java)

                    // Display the subscription status in the TextView
                    binding.methods.text = if (subscribeStatus == true) {
                        "Subscribed"
                    } else {
                        "Not Subscribed"
                    }
                } else {
                    binding.methods.text = "User data does not exist"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                binding.methods.text = "Error fetching data"
            }
        })
    }


    private fun checkUserSubscription() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Get the unique user ID
            val userId = currentUser.uid
            val userRef = database.child("user").child(userId)

            // Read the "subscribe" field from the database
            userRef.child("subscribe").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val isSubscribed = dataSnapshot.getValue(Boolean::class.java) ?: false
                    if (isSubscribed) {
                        // User is already subscribed, show the popup and navigate to profile
                        showSubscriptionPopup()
                    } else {
                        // User is not subscribed, allow them to proceed with the subscription
                        Toast.makeText(this@SubscriptionActivity, "You can proceed with the subscription.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors
                    Toast.makeText(this@SubscriptionActivity, "Error fetching data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No current user found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSubscriptionPopup() {
        AlertDialog.Builder(this)
            .setTitle("Already Subscribed")
            .setMessage("You have already subscribed.")
            .setPositiveButton("OK") { dialog, _ ->
                // Dismiss the dialog and navigate to ProfileActivity
                dialog.dismiss()
                navigateToProfileActivity()
            }
            .create()
            .show()
    }


    private fun navigateToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        // Optional: finish the current activity if you don't want the user to return here
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener to prevent memory leaks
        subscriptionListener?.let {
            databaseReference.removeEventListener(it)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // Set "subscribe" to false for the new user
                        updateUserSubscriptionStatus(isSubscribed = false)
                    }
                } else {
                    // Handle registration failure
                    Toast.makeText(this, "User registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
