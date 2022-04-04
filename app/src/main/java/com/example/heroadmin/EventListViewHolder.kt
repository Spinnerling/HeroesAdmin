package com.example.heroadmin

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventListViewHolder(
    itemView: View,
    private val onEventItemClick: (position: Int) -> Unit
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    // Find all the views of the list item
    var titleText: TextView = itemView.findViewById(R.id.event_text_title)
    var dateText: TextView = itemView.findViewById(R.id.event_text_date)
    var timeText: TextView = itemView.findViewById(R.id.event_text_time)
    var playerAmountText: TextView = itemView.findViewById(R.id.event_text_playerAmount)
    var statusText: TextView = itemView.findViewById(R.id.event_text_status)

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val position = adapterPosition
        onEventItemClick(position)
    }
}
