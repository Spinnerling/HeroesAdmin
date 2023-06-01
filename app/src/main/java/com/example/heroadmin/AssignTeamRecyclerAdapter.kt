package com.example.heroadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AssignTeamRecyclerAdapter (private val ticketArray: MutableList<Ticket>, private val eventView : EventView) : RecyclerView.Adapter<AssignTeamViewHolder>() {
    private val DBF = eventView.context?.let { DatabaseFunctions(it) }

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
        holder.bookerEmailText.text = ticket.bookerEmail
        holder.contactName.text = ticket.bookerName
        holder.contactPhone.text = ticket.bookerPhone
        holder.bookerName.text = ticket.bookerName
        holder.note.text = ticket.note

        if (ticket.group != "SELF" && ticket.group != ""){
            holder.groupName.text = "Group: ${ticket.group}"
        }
        else {
            holder.groupName.text = "Ungrouped"
        }

        if (holder.note.text != "" && !ticket.noteHandled) {
            holder.notePanel.visibility = View.VISIBLE
        }

        if (ticket.playerId == ""){
            holder.checkIdButton.visibility = View.VISIBLE
            holder.playerIdText.visibility = View.GONE
        }
        else {
            holder.checkIdButton.visibility = View.GONE
            holder.playerIdText.visibility = View.VISIBLE
            holder.playerIdText.text = "Found"
        }

        if (ticket.note == ""){
            holder.hideNoteButton.visibility = View.INVISIBLE
            holder.editNoteButton.text = "Add Note"
        } else {
            // Set the views as they should be when the note is not empty
            holder.hideNoteButton.visibility = View.VISIBLE
            holder.editNoteButton.text = "Edit Note"
        }

        holder.checkIdButton.setOnClickListener {
            eventView.manualPlayerLink(ticket)
        }

        holder.infoButton.setOnClickListener{
            if (holder.itemInfoHeaders.visibility == View.VISIBLE){
                holder.itemInfoHeaders.visibility = View.GONE
                holder.itemInfoTexts.visibility = View.GONE
                holder.itemButtons.visibility = View.GONE
            }
            else {
                holder.itemInfoHeaders.visibility = View.VISIBLE
                holder.itemInfoTexts.visibility = View.VISIBLE
                holder.itemButtons.visibility = View.VISIBLE
            }
        }

        holder.blueButton.setOnClickListener{
            if (ticket.group == ""){
                DBF?.setTicketTeamColor(ticket, true)
            }
            else {
                eventView.setGroupColor(ticket.group, true, true)
            }

            eventView.updateTicketLists()

            if (ticket.checkedIn == 1){
                eventView.checkInTicket(ticket)
            }
        }

        holder.redButton.setOnClickListener{
            if (ticket.group == ""){
                DBF?.setTicketTeamColor(ticket, false)
            }
            else {
                eventView.setGroupColor(ticket.group, false, true)
            }

            eventView.updateTicketLists()

            if (ticket.checkedIn == 1){
                eventView.checkInTicket(ticket)
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

        holder.groupName.setOnClickListener{
            eventView.setGroupName(ticket)
        }

        holder.editNoteButton.setOnClickListener {
            eventView.editNote(ticket)
        }
    }
}