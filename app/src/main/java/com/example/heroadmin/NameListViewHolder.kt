package com.example.heroadmin

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NameListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var nameText: TextView = itemView.findViewById(R.id.nameItem_nameText)
}