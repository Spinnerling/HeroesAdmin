package com.example.heroadmin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class AssignTeamRecyclerAdapter (private val ticketArray: MutableList<Ticket>, private val eventView : EventView) : RecyclerView.Adapter<AssignTeamViewHolder>() {

    override fun getItemCount(): Int {
        return if (ticketArray.isEmpty()) 0 else ticketArray.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignTeamViewHolder{
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.assign_team_item, parent, false)
        return AssignTeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignTeamViewHolder, position: Int) {
        // is called to attach data to a ViewHolder. Here you change the text, color, whatever needs to be done to the list item views according to the data being displayed. I created a bind function in the ViewHolder earlier for convenience which I use here, if you want you could do everything in the bind function here instead.
        val ticket = ticketArray[position]

        var name = ticket.fullName
        holder.nameText.text = name
        var age = ticket.age
        holder.ageText.text = age.toString()
        var playerId = ticket.playerId
        holder.playerIdText.text = playerId
        var bookerEmail = ticket.bookerEmail
        holder.bookerEmailText.text = bookerEmail

        if (holder.playerIdText.text == ""){
            holder.checkIdButton.visibility = View.VISIBLE
            holder.playerIdText.visibility = View.GONE
        }
        else {
            holder.checkIdButton.visibility = View.GONE
            holder.playerIdText.visibility = View.VISIBLE
        }

        var contactName = ticket.guardianFullName
        holder.contactName.text = contactName
        var contactPhone = ticket.guardianPhoneNr
        holder.contactPhone.text = contactPhone
        var contactEmail = ticket.guardianEmail
        holder.contactEmail.text = contactEmail
        var bookerName = ticket.bookerEmail
        holder.bookerName.text = bookerName
        var bookerPhone = ticket.bookerPhoneNr
        holder.bookerPhone.text = bookerPhone
        var note = ticket.note
        holder.note.text = note

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

        holder.blueButton.setOnClickListener{
            ticket.teamColor = "Blue"
            eventView.updateTicketLists()

            if (ticket.checkedIn){
                eventView.setTicketTabardNumber(ticket)
            }
        }

        holder.redButton.setOnClickListener{
            ticket.teamColor = "Red"
            eventView.updateTicketLists()

            if (ticket.checkedIn){
                eventView.setTicketTabardNumber(ticket)
            }
        }

        if (note != "") {
            holder.infoButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0)
            // #E61F1F
        }
    }
}