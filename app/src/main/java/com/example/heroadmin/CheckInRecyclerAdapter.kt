package com.example.heroadmin

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.recyclerview.widget.RecyclerView


class CheckInRecyclerAdapter(private var ticketArray: MutableList<Ticket>, private val eventView : EventView) : RecyclerView.Adapter< CheckInViewHolder>(){
    private lateinit var view : View
    private lateinit var context : Context

    override fun getItemCount(): Int {
        return if (ticketArray.isEmpty()) 0 else ticketArray.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckInViewHolder{
        context = parent.context
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.checkin_listitem, parent, false)
        return CheckInViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckInViewHolder, position: Int) {
        // is called to attach data to a ViewHolder. Here you change the text, color, whatever needs to be done to the list item views according to the data being displayed. I created a bind function in the ViewHolder earlier for convenience which I use here, if you want you could do everything in the bind function here instead.
        val ticket = ticketArray[position]

        holder.nameText.text = ticket.fullName
        holder.ageText.text = ticket.age.toString()
        holder.note.text = ticket.note

        holder.contactName.text = ticket.guardianName
        holder.contactPhone.text = ticket.guardianPhoneNr

        holder.bookerName.text = ticket.bookerFullName
        holder.bookerEmail.text = ticket.bookerEmail


        holder.infoButton.setOnClickListener{
            if (holder.itemInfoHeaders.visibility == View.VISIBLE){
                holder.itemInfoHeaders.visibility = View.GONE
                holder.itemInfoTexts.visibility = View.GONE
            }
            else {
                holder.itemInfoHeaders.visibility = View.VISIBLE
                holder.itemInfoTexts.visibility = View.VISIBLE
            }
        }

        holder.checkInButton.setOnClickListener{
            eventView.setTicketTabardNumber(ticket)
            eventView.autoSetRoleAmounts()
        }

        holder.teamButton.setOnClickListener{
            if (ticket.teamColor == "Blue"){
                eventView.setGroupColor(ticket, false)
            }
            else {
                eventView.setGroupColor(ticket, true)
            }
            eventView.updateTicketLists()
        }

        if (ticket.teamColor == "Blue"){
            holder.teamButton.setBackgroundColor(context.resources.getColor(R.color.teamBlueColor))
        }
        else if (ticket.teamColor == "Red"){
            holder.teamButton.setBackgroundColor(context.resources.getColor(R.color.teamRedColor))
        }
    }
}