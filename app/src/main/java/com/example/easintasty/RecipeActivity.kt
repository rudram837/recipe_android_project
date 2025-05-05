package com.example.easintasty

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.easintasty.databinding.ActivityRecipeBinding

class RecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeBinding
    private lateinit var db: AppDatabase
    private var imgCrop = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load data from the intent
        val img = intent.getStringExtra("img")
        val title = intent.getStringExtra("tittle")
        val description = intent.getStringExtra("des")
        val ingredients = intent.getStringExtra("ing")?.split("\n".toRegex())?.dropLastWhile { it.isEmpty() }

        // Set UI elements
        Glide.with(this).load(img).into(binding.itemImage)
        binding.tittle.text = title
        binding.stepData.text = description
        binding.time.text = ingredients?.get(0)

        // Display ingredients with bullet points
        ingredients?.let {
            binding.ingData.text = it.drop(1).joinToString(separator = "\n") { ing -> "🟢 $ing" }
        }

        // Set up step and ingredient button functionality
        binding.step.background = null
        binding.step.setTextColor(getColor(R.color.black))

        binding.step.setOnClickListener {
            binding.step.setBackgroundResource(R.drawable.btn_ing)
            binding.step.setTextColor(getColor(R.color.white))
            binding.ing.setTextColor(getColor(R.color.black))
            binding.ing.background = null
            binding.stepScroll.visibility = View.VISIBLE
            binding.ingScroll.visibility = View.GONE
        }

        binding.ing.setOnClickListener {
            binding.ing.setBackgroundResource(R.drawable.btn_ing)
            binding.ing.setTextColor(getColor(R.color.white))
            binding.step.setTextColor(getColor(R.color.black))
            binding.step.background = null
            binding.ingScroll.visibility = View.VISIBLE
            binding.stepScroll.visibility = View.GONE
        }

        // Fullscreen toggle for image
        binding.fullScreen.setOnClickListener {
            if (imgCrop) {
                binding.itemImage.scaleType = ImageView.ScaleType.FIT_CENTER
                Glide.with(this).load(img).into(binding.itemImage)
                binding.fullScreen.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
                binding.shade.visibility = View.GONE
            } else {
                binding.itemImage.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).load(img).into(binding.itemImage)
                binding.fullScreen.setColorFilter(null)
                binding.shade.visibility = View.GONE
            }
            imgCrop = !imgCrop
        }

        // Initialize Room database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db_name"
        )
            .allowMainThreadQueries() // Not recommended for production; use coroutines instead
            .build()

        val daoObject = db.getDao()

        // Handle favorite button click
        binding.favbtn.setOnClickListener {
            // Check if the recipe is already a favorite
            val isFavorite = daoObject.isRecipeFavorite(title ?: "") > 0

            if (isFavorite) {
                // Remove the recipe from favorites
                daoObject.removeFavorite(title ?: "")
                binding.favbtn.setColorFilter(null)
                Toast.makeText(this, "Recipe removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                // Create a new Recipe instance and mark it as favorite
                val recipe = Recipe(
                    img = img ?: "",
                    tittle = title ?: "",
                    des = description ?: "",
                    ing = ingredients?.joinToString("\n") ?: "",
                    category = intent.getStringExtra("category") ?: "",
                    isFavorite = true
                )

                // Insert the recipe as a favorite
                daoObject.insertFavorite(recipe)
                Toast.makeText(this, "${recipe.tittle} marked as favorite!", Toast.LENGTH_SHORT).show()
                binding.favbtn.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
            }
        }

        // Handle back button click
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}
