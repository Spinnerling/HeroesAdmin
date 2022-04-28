package com.example.heroadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AssignTeamRecyclerAdapter (private val ticketArray: MutableList<Ticket>, private val eventView : EventView) : RecyclerView.Adapter<AssignTeamViewHolder>() {

    override fun getItemCount(): Int {
        return if (ticketArray.isEmpty()) 0 else ticketArray.size
    }

    override fun getItemViewType(position : Int) : Int { return position; }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignTeamViewHolder{
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.assign_team_item, parent, false)
        return AssignTeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignTeamViewHolder, position: Int) {
        // is called to attach data to a ViewHolder. Here you change the text, color, whatever needs to be done to the list item views according to the data being displayed. I created a bind function in the ViewHolder earlier for convenience which I use here, if you want you could do everything in the bind function here instead.
        val ticket = ticketArray[position]

        holder.nameText.text = ticket.fullName
        holder.ageText.text = ticket.age.toString()
        holder.playerIdText.text = ticket.playerId
        holder.bookerEmailText.text = ticket.bookerEmail
        holder.contactName.text = ticket.guardianName
        holder.contactPhone.text = ticket.guardianPhoneNr
        holder.bookerName.text = ticket.bookerFullName
        holder.note.text = ticket.note

        if (ticket.groupSize > 1){
            holder.groupName.text = "(${ticket.groupSize}) ${ticket.group}"
        }
        else {
            holder.groupName.text = ""
        }

        if (holder.note.text != "" && !ticket.noteHandled) {
            holder.notePanel.visibility = View.VISIBLE
        }

        if (holder.playerIdText.text == "" || holder.playerIdText.text == "0"){
            holder.checkIdButton.visibility = View.VISIBLE
            holder.playerIdText.visibility = View.GONE
        }
        else {
            holder.checkIdButton.visibility = View.GONE
            holder.playerIdText.visibility = View.VISIBLE
        }

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
            eventView.setGroupColor(ticket, true)
            eventView.updateTicketLists()

            if (ticket.checkedIn == 1){
                eventView.setTicketTabardNumber(ticket)
            }
        }

        holder.redButton.setOnClickListener{
            eventView.setGroupColor(ticket, false)
            eventView.updateTicketLists()

            if (ticket.checkedIn == 1){
                eventView.setTicketTabardNumber(ticket)
            }
        }

        holder.hideNoteButton.setOnClickListener{
            ticket.noteHandled = !ticket.noteHandled
            if (ticket.noteHandled){
                holder.notePanel.visibility = View.GONE
                holder.hideNoteButton.text = "SHOW NOTE"
            }
            else {
                holder.hideNoteButton.text = "HIDE NOTE"
                if (ticket.note != "") {
                    holder.notePanel.visibility = View.VISIBLE
                }
            }
        }
    }
}