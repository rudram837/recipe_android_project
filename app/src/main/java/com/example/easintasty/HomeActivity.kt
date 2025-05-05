package com.example.easintasty

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.easintasty.databinding.ActivityHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var rvAdapter: PopularAdapter
    private lateinit var rv_Adapter: IndianAdapter
    private lateinit var dataList: ArrayList<Recipe>

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var database: DatabaseReference

    private lateinit var popularRecyclerView: RecyclerView
    private lateinit var indItemsTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRecyclerView()
        setUpIndianRecyclerView()

        // Initialize RecyclerView
        popularRecyclerView = findViewById(R.id.rv_popular)
        indItemsTextView = findViewById(R.id.ind_items)

        // Initially disable access to the RecyclerView
        popularRecyclerView.visibility = View.GONE

        // Check the user's subscription status
        checkUserSubscription()

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference


        binding.search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.salad.setOnClickListener {
            var myIntent = Intent(this@HomeActivity, CategoryActivity::class.java)
            myIntent.putExtra("TITTLE", "Salad")
            myIntent.putExtra("CATETGORY", "Salad")
            startActivity(myIntent)
        }

        binding.maindish.setOnClickListener {
            var myIntent = Intent(this@HomeActivity, CategoryActivity::class.java)
            myIntent.putExtra("TITTLE", "Main Dish")
            myIntent.putExtra("CATETGORY", "Dish")
            startActivity(myIntent)
        }

        binding.drink.setOnClickListener {
            var myIntent = Intent(this@HomeActivity, CategoryActivity::class.java)
            myIntent.putExtra("TITTLE", "Drinks")
            myIntent.putExtra("CATETGORY", "Drinks")
            startActivity(myIntent)
        }

        binding.dessert.setOnClickListener {
            var myIntent = Intent(this@HomeActivity, CategoryActivity::class.java)
            myIntent.putExtra("TITTLE", "Desserts")
            myIntent.putExtra("CATETGORY", "Desserts")
            startActivity(myIntent)
        }

        binding.more.setOnClickListener {
            var dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.bottom_sheet)

            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setGravity(Gravity.BOTTOM)
            showBottomSheetDialog()
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        view.findViewById<TextView>(R.id.accbtn).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.aboutbtn).setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }


    private fun setUpRecyclerView() {
        dataList = ArrayList()

        binding.rvPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        val db = Room.databaseBuilder(this@HomeActivity, AppDatabase::class.java, "db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("recipe.db")
            .build()
        val daoObject = db.getDao()
        val recipes = daoObject.getAll()
        for (i in recipes!!.indices) {
            if (recipes[i]!!.category.contains("Indian")) {
                dataList.add(recipes[i]!!)
            }
            rvAdapter = PopularAdapter(dataList, this)
            binding.rvPopular.adapter = rvAdapter
        }
    }

    private fun setUpIndianRecyclerView() {
        dataList = ArrayList()

        binding.rvInd.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        val db = Room.databaseBuilder(this@HomeActivity, AppDatabase::class.java, "db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("recipe.db")
            .build()
        val daoObject = db.getDao()
        val recipes = daoObject.getAll()
        for (i in recipes!!.indices) {
            if (recipes[i]!!.category.contains("Popular")) {
                dataList.add(recipes[i]!!)
            }
            rv_Adapter = IndianAdapter(dataList, this)
            binding.rvInd.adapter = rv_Adapter
        }
    }

    // Function to check the user subscription status
    private fun checkUserSubscription() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()

        // Get current user ID
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null) {
            // Reference to the current user in the database
            val userRef = database.reference.child("user").child(currentUserId)

            // Read the "subscribe" field from the database
            userRef.child("subscribe").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val isSubscribed = dataSnapshot.getValue(Boolean::class.java) ?: false
                    if (isSubscribed) {
                        // User is subscribed, grant access to RecyclerView
                        enableRecyclerView()
                    } else {
                        // User is not subscribed, deny access to RecyclerView
                        denyRecyclerViewAccess()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors
                    println("Error fetching data: ${databaseError.message}")
                }
            })
        } else {
            println("No current user found.")
        }
    }

    // Function to enable RecyclerView access
    private fun enableRecyclerView() {
        popularRecyclerView.visibility = View.VISIBLE
        indItemsTextView.visibility = View.VISIBLE
        // Set up your RecyclerView adapter here
        Toast.makeText(this, "Access granted.", Toast.LENGTH_SHORT).show()
        // You can add the RecyclerView adapter setup here
        // popularRecyclerView.adapter = IndianAdapter()
    }

    // Function to deny RecyclerView access
    private fun denyRecyclerViewAccess() {
        // Keep RecyclerView hidden
        popularRecyclerView.visibility = View.GONE
        indItemsTextView.visibility = View.GONE // Keep RecyclerView hidden
        Toast.makeText(this, "Access denied. Please subscribe.", Toast.LENGTH_LONG).show()
        // You can also display a dialog or redirect the user to a subscription page if necessary
    }
}
