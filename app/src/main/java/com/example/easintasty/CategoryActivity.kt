package com.example.easintasty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.easintasty.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {

    private lateinit var rvAdapter: CategoryAdapter
    private lateinit var dataList: ArrayList<Recipe>

    private val binding by  lazy {
        ActivityCategoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        title = intent.getStringExtra("TITTLE")
        setUpRecyclerView()
        binding.goBackHome.setOnClickListener {
            finish()
        }
    }

    private fun setUpRecyclerView() {
        dataList = ArrayList()

        binding.rvCategory.layoutManager = LinearLayoutManager(this)
        val db = Room.databaseBuilder(this@CategoryActivity, AppDatabase::class.java, "db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("recipe.db")
            .build()
        val daoObject = db.getDao()
        val recipes = daoObject.getAll()
        for (i in recipes!!.indices) {
            if (recipes[i]!!.category.contains(intent.getStringExtra("CATETGORY")!!)) {
                dataList.add(recipes[i]!!)
            }
            rvAdapter = CategoryAdapter(dataList, this)
            binding.rvCategory.adapter = rvAdapter
        }
    }
}
