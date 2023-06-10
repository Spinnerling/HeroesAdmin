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
        holder.note.text = ticket.note
        holder.playerId.text = ticket.playerId ?: "Player ID not found"

        if (ticket.group != "SELF" && ticket.group != "" && ticket.group != null){
            holder.groupName.text = "Group: ${ticket.group}"
        }
        else {
            holder.groupName.text = "Ungrouped"
        }

        if (holder.note.text != "" && holder.note.text != null  && !ticket.noteHandled) {
            holder.notePanel.visibility = View.VISIBLE
        }

        // Set team color buttons on/off
        if (eventView.allTicketsMatched) {
            // Alla tickets har match, inkl denna
            holder.blueButton.visibility = View.VISIBLE
            holder.redButton.visibility = View.VISIBLE
            holder.checkIdButton.visibility = View.GONE
        }
        else {
            if (ticket.playerId == "" || ticket.playerId == null){
                // Denna har inte matchat
                holder.blueButton.visibility = View.GONE
                holder.redButton.visibility = View.GONE
                holder.checkIdButton.visibility = View.VISIBLE
            }
            else {
                // Denna har matchat, men inte andra
                holder.checkIdButton.visibility = View.GONE
                holder.blueButton.visibility = View.INVISIBLE
                holder.redButton.visibility = View.INVISIBLE
            }
        }

        // Note visibility
        if (ticket.note == "" || ticket.note == null){
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
                holder.playerIdLayout.visibility = View.GONE
            }
            else {
                holder.itemInfoHeaders.visibility = View.VISIBLE
                holder.itemInfoTexts.visibility = View.VISIBLE
                holder.itemButtons.visibility = View.VISIBLE
                holder.playerIdLayout.visibility = View.VISIBLE
            }
        }

        holder.blueButton.setOnClickListener{
            if (ticket.group == "" || ticket.group == null){
                DBF?.setTicketTeamColor(ticket, true)
            }
            else {
                eventView.setGroupColor(ticket.group!!, true, true)
            }

            eventView.updateTicketLists()

            if (ticket.checkedIn == 1){
                eventView.checkInTicket(ticket)
            }
        }

        holder.redButton.setOnClickListener{
            if (ticket.group == "" || ticket.group == null){
                DBF?.setTicketTeamColor(ticket, false)
            }
            else {
                eventView.setGroupColor(ticket.group!!, false, true)
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