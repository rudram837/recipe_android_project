package com.example.easintasty

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.easintasty.databinding.ActivitySignBinding
import com.example.easintasty.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class SignUpActivity : AppCompatActivity() {

    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInclient: GoogleSignInClient

    private val binding: ActivitySignBinding by lazy {
        ActivitySignBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //google variable
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        //Initialize Firebase Auth
        auth = Firebase.auth

        //Initialize Firebase Databbase
        database = Firebase.database.reference


        //initialize google sign in
        googleSignInclient = GoogleSignIn.getClient(this, googleSignInOptions)

        //create acc with name, email and password
        binding.createbutn.setOnClickListener {
            //get text from edittext
            name = binding.namebtn.text.toString().trim()
            email = binding.mailbutn.text.toString().trim()
            password = binding.pwdbutn.text.toString().trim()

            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
            
            if (password.isBlank()){
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            }
        }


        //google login
        binding.googlebutn.setOnClickListener {
            val signIntent = googleSignInclient.signInIntent
            launcher.launch(signIntent)
        }

        binding.alreadyhavebtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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
                        Toast.makeText(this, "Successfully Sign Up with Google", Toast.LENGTH_SHORT).show()
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

    private fun createAccount(mail: String, password: String) {
        if (!isValidEmail(mail)) {
            Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isStrongPassword(password)) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                // Set "subscribe" to false for the new user
                updateUserSubscriptionStatus(false)
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Account Creation Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure", task.exception)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Define the regex pattern for validating email
        val emailPattern =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"

        // Return true if the email matches the pattern, false otherwise
        return email.matches(emailPattern.toRegex())
    }

    // Function to check password strength
    private fun isStrongPassword(password: String): Boolean {
        return password.length >= 6 // You can enhance this with regex if needed
    }

    //save data  into database
    private fun saveUserData() {
        name = binding.namebtn.text.toString().trim()
        email = binding.mailbutn.text.toString().trim()
        password = binding.pwdbutn.text.toString().trim()
        val user= UserModel(name, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        //save user data firebase database
        database.child("user").child(userId).setValue(user)
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun registerUser(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // Set "subscribe" to false for the new user
                        updateUserSubscriptionStatus(false)
                    }
                } else {
                    // Handle registration failure
                    println("User registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun updateUserSubscriptionStatus(isSubscribed: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child(userId)

            // Set the "subscribe" variable based on the subscription status
            database.child("subscribe").setValue(isSubscribed)
                .addOnSuccessListener {
                    // Successfully updated
                    println("User subscription status updated successfully: subscribe = $isSubscribed")
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    println("Failed to update user subscription status: ${exception.message}")
                }
        } else {
            println("No user is currently signed in.")
        }
    }
}
