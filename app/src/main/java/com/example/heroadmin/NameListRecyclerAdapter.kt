package com.example.heroadmin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class NameListRecyclerAdapter(private val nameArray: MutableList<String>) : RecyclerView.Adapter<NameListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameListViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.name_list_item, parent, false)
        return NameListViewHolder(view)
    }

    override fun onBindViewHolder(holder: NameListViewHolder, position: Int) {
        val name = nameArray[position]
        holder.nameText.text = name
    }

    override fun getItemCount(): Int {
        return if (nameArray.isEmpty()) 0 else nameArray.size
    }

    override fun getItemViewType(position : Int) : Int { return position; }
}