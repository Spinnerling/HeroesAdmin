package com.example.heroadmin

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TeamViewHolder(
    itemView: View,
    private val onTeamItemClick: (position: Int) -> Unit)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    var nameText: TextView = itemView.findViewById(R.id.team_nameText)
    var numberText: TextView = itemView.findViewById(R.id.team_tabardNr)
    var roleText: TextView = itemView.findViewById(R.id.team_roleText)

    override fun onClick(p0: View?) {
        val position = adapterPosition
        onTeamItemClick(position)
    }
}