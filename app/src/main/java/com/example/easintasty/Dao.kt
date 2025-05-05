package com.example.easintasty

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface Dao {
    @Query("SELECT * FROM recipe")
    fun getAll(): List<Recipe?>?

    // Assuming there is a boolean column `isFavorite` in your Recipe table
    @Query("SELECT * FROM Recipe WHERE isFavorite = 1")
    fun getAllFavorites(): List<Recipe>

    //Query to insert from favorite
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(recipe: Recipe)

    // Query to check if the recipe is already in favorites
    @Query("SELECT COUNT(*) FROM recipe WHERE tittle = :recipeTitle AND isFavorite = 1")
    fun isRecipeFavorite(recipeTitle: String): Int

    //Query to remove from favorite
    @Query("DELETE FROM recipe WHERE tittle = :title")
    fun removeFavorite(title: String)
}

