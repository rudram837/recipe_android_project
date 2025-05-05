package com.example.easintasty

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.easintasty.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInclient: GoogleSignInClient

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //google variable
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        //initialize firebase auth
        auth = Firebase.auth
        //initialize firebase database
        database = Firebase.database.reference


        //initialize google sign in
        googleSignInclient = GoogleSignIn.getClient(this, googleSignInOptions)

        //login with email & password
        binding.loginbtn.setOnClickListener {
            //get text from edit-text
            email = binding.mailbtn.text.toString().trim()
            password = binding.pwdbtn.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all details",Toast.LENGTH_SHORT).show()
            } else {
                loginUserAccount(email, password)
            }
        }

        //google login
        binding.googlebtn.setOnClickListener {
            val signIntent = googleSignInclient.signInIntent
            launcher.launch(signIntent)
        }

        binding.donthavebtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUserAccount(email: String, password: String) {
        //sign in if the user already exist
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                onUserLoggedIn()
                updateUi(user)
            } else {
                Toast.makeText(this, "You don't have account\n      Create Account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //update the UI move to next screen
    private fun updateUi(user: FirebaseUser?) {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    //launcher function
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account : GoogleSignInAccount = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        //successfully sign in with Google
                        Toast.makeText(this, "Successfully Sign In with Google", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Google Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else {
                Toast.makeText(this, "Google Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            // User is signed in, call the onUserLoggedIn function
            onUserLoggedIn()
            finish()
        }
    }

    private fun onUserLoggedIn() {
        checkUserSubscriptionStatus { userHasSubscribed ->
            // Update the subscription status in the database
            updateUserSubscriptionStatus(userHasSubscribed)

            // Show a toast message based on the subscription status
            if (userHasSubscribed) {
                Toast.makeText(this, "You are Subscribed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "You are not Subscribed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserSubscriptionStatus(isSubscribed: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

            // Set the hasSubscribe variable based on the subscription status
            database.child("subscribe").setValue(isSubscribed)
                .addOnSuccessListener {
                    // Successfully updated
                    println("User subscription status updated to: $isSubscribed")
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    println("Failed to update user subscription status: ${exception.message}")
                }
        } else {
            println("No user is currently signed in.")
        }
    }

    private fun checkUserSubscriptionStatus(onResult: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

            database.child("subscribe").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val hasSubscribed = dataSnapshot.getValue(Boolean::class.java) ?: false
                    println("Subscribed")
                    // Pass the Boolean result
                    onResult(hasSubscribed)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Failed to read subscription status: ${databaseError.message}")
                    // Handle error case
                    onResult(false)
                }
            }
            )
        } else {
            println("No user is currently signed in.")
            // Handle case where no user is signed in
            onResult(false)
        }
    }
}
