package com.example.easintasty

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easintasty.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set an OnClickListener on the LinearLayout for sending email
        binding.maillay.setOnClickListener {
            sendEmailToDeveloper()
        }

        // Set an OnClickListener on the LinearLayout for GitHub profile
        binding.githubl.setOnClickListener {
            openGitHubProfile()
        }

        // Set an OnClickListener on the LinearLayout for Instagram profile
        binding.instal.setOnClickListener {
            openInstagramProfile()
        }

        // Set the back button functionality
        binding.goBack.setOnClickListener {
            finish()
        }
    }

    // Function to send email to the developer
    private fun sendEmailToDeveloper() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf("rudramaurya313@gmail.com")) // Developer's email
            putExtra(Intent.EXTRA_SUBJECT, "Contacting Developer") // Optional subject
        }

        // Check if there is an app to handle the intent and start the activity
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }

    // Function to open the developer's GitHub profile
    private fun openGitHubProfile() {
        val githubUrl = "https://github.com/rudram837"
        val githubIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(githubUrl)
        }

        // Check if there is an app to handle the intent and start the activity
        if (githubIntent.resolveActivity(packageManager) != null) {
            startActivity(githubIntent)
        }
    }

    // Function to open the developer's Instagram profile
    private fun openInstagramProfile() {
        val instagramUrl = "https://instagram.com/itz_rudra_926"
        val instagramIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(instagramUrl)
        }

        // Check if there is an app to handle the intent and start the activity
        if (instagramIntent.resolveActivity(packageManager) != null) {
            startActivity(instagramIntent)
        }
    }
}
