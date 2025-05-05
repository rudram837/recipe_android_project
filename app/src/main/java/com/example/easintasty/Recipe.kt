package com.example.easintasty

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe")
data class Recipe (
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    var img: String,
    var tittle: String,
    var des: String,
    var ing: String,
    var category: String,
    var isFavorite: Boolean = false // Add this field to track favorite status
)
{

}
