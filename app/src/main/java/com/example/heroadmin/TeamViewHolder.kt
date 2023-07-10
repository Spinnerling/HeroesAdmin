package com.example.heroadmin

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TeamViewHolder(
    itemView: View,
    private val eventView : EventView
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var nameText: TextView = itemView.findViewById(R.id.team_nameText)
    var roleText: TextView = itemView.findViewById(R.id.team_roleText)
    var background : LinearLayout = itemView.findViewById(R.id.teamItemBackground)
    lateinit var ticket : Ticket

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        select()
    }

    fun select() {
        eventView.selectTicket(ticket)
        background.setBackgroundColor(Color.LTGRAY)
        eventView.selectedTicketTVH = this
        eventView.updateBottomPanel(2)
    }

    fun deselect() {
        ticket.selected = false
        background.setBackgroundColor(Color.WHITE)
    }
}