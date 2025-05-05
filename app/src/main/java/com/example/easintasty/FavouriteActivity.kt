package com.example.easintasty

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.easintasty.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity(), FavouriteAdapter.OnItemClickListener {

    private lateinit var rvAdapter: FavouriteAdapter
    private lateinit var favouriteList: ArrayList<Recipe>

    private val binding by lazy {
        ActivityFavouriteBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        title = "Favorites"
        setUpRecyclerView() // Initialize RecyclerView
        binding.backbtn.setOnClickListener {
            finish()
        }
    }

    private fun setUpRecyclerView() {
        favouriteList = ArrayList()

        // Initialize the RecyclerView layout manager
        binding.rvFavourite.layoutManager = LinearLayoutManager(this)

        // Initialize Room database
        val db = Room.databaseBuilder(
            this@FavouriteActivity,
            AppDatabase::class.java,
            "db_name"
        )
            .allowMainThreadQueries() // This should be removed for production code
            .fallbackToDestructiveMigration() // Use with caution; this will delete all data on schema changes
            .build()

        // Get DAO instance and fetch all favorite recipes
        val daoObject = db.getDao() // Ensure this matches your DAO name
        val favourites = daoObject.getAllFavorites() // Fetch all favorite recipes from the database

        // Add fetched data to favouriteList
        favouriteList.addAll(favourites)

        // Initialize adapter with the fetched data
        rvAdapter = FavouriteAdapter(favouriteList, this, this)
        binding.rvFavourite.adapter = rvAdapter
    }

    override fun onItemClick(recipe: Recipe) {
        // Start RecipeActivity with data from the clicked item
        val intent = Intent(this, RecipeActivity::class.java).apply {
            putExtra("img", recipe.img)
            putExtra("tittle", recipe.tittle)
            putExtra("des", recipe.des)
            putExtra("ing", recipe.ing)
        }
        startActivity(intent)
    }
}
