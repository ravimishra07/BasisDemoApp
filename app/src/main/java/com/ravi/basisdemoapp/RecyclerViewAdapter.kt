package com.ravi.basisdemoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ravi.basisdemoapp.model.SubData


class RecyclerViewAdapter(private val userData: List<SubData>) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_layout, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = userData.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.apply {
            name.text = userData[position].text
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name_view)
    }
}