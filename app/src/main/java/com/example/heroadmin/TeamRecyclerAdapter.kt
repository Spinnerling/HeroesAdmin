package com.example.heroadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TeamRecyclerAdapter (private val ticketArray: MutableList<Ticket>, private val onItemClicked: (position: Int) -> Unit, private val eventView : EventView) : RecyclerView.Adapter<TeamViewHolder>() {
    override fun getItemCount(): Int {
        return if (ticketArray.isEmpty()) 0 else ticketArray.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder{
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.team_listitem, parent, false)
        return TeamViewHolder(view, onItemClicked, eventView)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        // is called to attach data to a ViewHolder. Here you change the text, color, whatever needs to be done to the list item views according to the data being displayed. I created a bind function in the ViewHolder earlier for convenience which I use here, if you want you could do everything in the bind function here instead.
        val ticket = ticketArray[position]
        val name = ticket.fullName
        holder.nameText.text = name
        val number = ticket.tabardNr
        holder.numberText.text = number.toString()
        val role = ticket.currentRole
        val roleInText = getRoleByNumber(role)
        holder.roleText.text = roleInText
    }
}