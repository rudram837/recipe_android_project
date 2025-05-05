package com.example.easintasty

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easintasty.databinding.FavouriteRvBinding

class FavouriteAdapter(
    private var dataList: ArrayList<Recipe>,
    var context: Context,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(recipe: Recipe)
    }

    inner class ViewHolder(var binding: FavouriteRvBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            Glide.with(context).load(recipe.img).into(binding.img)
            binding.title.text = recipe.tittle
            val timeInfo = recipe.ing.split("\n").firstOrNull() ?: "N/A"
            binding.time0.text = timeInfo

            // Set up the click listener for the item
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FavouriteRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}
