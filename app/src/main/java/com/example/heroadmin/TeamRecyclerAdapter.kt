package com.example.heroadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.heroadmin.LocalDatabaseSingleton.playerDatabase

class TeamRecyclerAdapter(
    private val ticketArray: MutableList<Ticket>,
    private val eventView: EventView
) : RecyclerView.Adapter<TeamViewHolder>() {
    override fun getItemCount(): Int {
        return if (ticketArray.isEmpty()) 0 else ticketArray.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.team_listitem, parent, false)

        return TeamViewHolder(view, eventView)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        // is called to attach data to a ViewHolder. Here you change the text, color, whatever needs to be done to the list item views according to the data being displayed. I created a bind function in the ViewHolder earlier for convenience which I use here, if you want you could do everything in the bind function here instead.
        val DBF = DatabaseFunctions(eventView.requireContext())
        val ticket = ticketArray[position]
        holder.ticket = ticket
        val name = ticket.fullName
        holder.nameText.text = name
        val role = ticket.currentRole
        val roleInText = getRoleByNumber(role ?: 0)
//        val player: Player? = ticket.playerId?.let { playerDatabase.getById(it) }
//
//        if (player != null) {
//            val level = when (roleInText) {
//                "Helare" -> player.healerLevel
//                "Odåga" -> player.rogueLevel
//                "Magiker" -> player.mageLevel
//                "Riddare" -> player.knightLevel
//                else -> 0
//            }
//            var levelText = "level $level"
//            if (level == 0) levelText = ""
//
//            holder.roleText.text = "$roleInText $levelText"
//        } else {
            holder.roleText.text = roleInText
//        }



        if (ticket.selected) {
            holder.background.setBackgroundColor(Color.LTGRAY)
            eventView.selectedTicketTVH = holder
        } else {
            holder.background.setBackgroundColor(Color.WHITE)
        }
    }
}