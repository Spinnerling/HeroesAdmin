package com.example.heroadmin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class CheckInRecyclerAdapter(private var ticketArray: MutableList<Ticket>, private val eventView : EventView) : RecyclerView.Adapter< CheckInViewHolder>(){
    private val DBF = eventView.context?.let { DatabaseFunctions(it) }
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

        holder.contactName.text = ticket.bookerName
        holder.contactPhone.text = ticket.bookerPhone
        holder.bookerEmail.text = ticket.bookerEmail
        holder.ticketIdText.text = ticket.ticketId
        holder.playerIdText.text = ticket.playerId ?: "Player ID not found"

        if (holder.note.text != "" && !ticket.noteHandled) {
            holder.notePanel.visibility = View.VISIBLE
        }

        if (ticket.group != "SELF" && ticket.group != "" && ticket.group != null){
            holder.groupName.text = "Group: ${ticket.group}"
        }
        else {
            holder.groupName.text = "No Group"
        }

        if (ticket.note == "" || ticket.note == null){
            holder.hideNoteButton.visibility = View.INVISIBLE
            holder.editNoteButton.text = "Add Note"
        }

        holder.infoButton.setOnClickListener{
            if (holder.itemInfoHeaders.visibility == View.VISIBLE){
                holder.itemInfoHeaders.visibility = View.GONE
                holder.itemInfoTexts.visibility = View.GONE
                holder.buttonPanel.visibility = View.GONE
                holder.idTitlesLayout.visibility = View.GONE
                holder.idValuesLayout.visibility = View.GONE
            }
            else {
                holder.itemInfoHeaders.visibility = View.VISIBLE
                holder.itemInfoTexts.visibility = View.VISIBLE
                holder.buttonPanel.visibility = View.VISIBLE
                holder.idTitlesLayout.visibility = View.VISIBLE
                holder.idValuesLayout.visibility = View.VISIBLE
            }
        }

        holder.checkInButton.setOnClickListener{
            eventView.checkInTicket(ticket)
        }

        holder.unteamButton.setOnClickListener{
            ticket.teamColor = null
            DBF?.updateData(ticket)
            eventView.updateTicketLists()
        }

        holder.teamButton.setOnClickListener{
            val isBlue = ticket.teamColor == "Blue"

                if (ticket.group == "" || ticket.group == null){
                    DBF?.setTicketTeamColor(ticket, isBlue)
                }
                else {
                    eventView.setGroupColor(ticket.group!!, isBlue, true)
                }

            eventView.updateTicketLists()
        }

        if (ticket.teamColor == "Blue"){
            holder.teamButton.setBackgroundColor(context.resources.getColor(R.color.teamBlueColor))
        }
        else if (ticket.teamColor == "Red"){
            holder.teamButton.setBackgroundColor(context.resources.getColor(R.color.teamRedColor))
        }

        holder.groupButton.setOnClickListener{
            eventView.setGroupName(ticket)
        }

        holder.hideNoteButton.setOnClickListener {
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

        holder.editNoteButton.setOnClickListener {
            eventView.editNote(ticket)
        }
    }
}