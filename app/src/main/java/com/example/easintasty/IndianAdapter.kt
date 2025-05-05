package com.example.easintasty

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.easintasty.databinding.IndianRvItemBinding

class IndianAdapter (var dataList: ArrayList<Recipe>, var context: Context): RecyclerView.Adapter<IndianAdapter.viewHolder>(){


    inner class viewHolder (var binding: IndianRvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        var binding = IndianRvItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {

        Glide.with(context).load(dataList.get(position).img).into(holder.binding.popularImg)
        holder.binding.popularTxt.text=dataList.get(position).tittle
        var time=dataList.get(position).ing.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        holder.binding.popularTime.text=time.get(0)
        holder.itemView.setOnClickListener {
            var intent = Intent(context, RecipeActivity::class.java)
            intent.putExtra("img", dataList.get(position).img)
            intent.putExtra("tittle", dataList.get(position).tittle)
            intent.putExtra("des", dataList.get(position).des)
            intent.putExtra("ing", dataList.get(position).ing)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}
