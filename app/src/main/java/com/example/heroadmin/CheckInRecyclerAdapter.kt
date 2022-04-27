package com.example.heroadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class CheckInRecyclerAdapter(private var ticketArray: MutableList<Ticket>, private val eventView : EventView) : RecyclerView.Adapter< CheckInViewHolder>(){
    private lateinit var view : View

    override fun getItemCount(): Int {
        return if (ticketArray.isEmpty()) 0 else ticketArray.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckInViewHolder{
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.checkin_listitem, parent, false)
        return CheckInViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckInViewHolder, position: Int) {
        // is called to attach data to a ViewHolder. Here you change the text, color, whatever needs to be done to the list item views according to the data being displayed. I created a bind function in the ViewHolder earlier for convenience which I use here, if you want you could do everything in the bind function here instead.
        val ticket = ticketArray[position]

        var name = ticket.fullName
        holder.nameText.text = name
        var age = ticket.age
        holder.ageText.text = age.toString()
        var note = ticket.note
        holder.note.text = note

        var contactName = ticket.guardianName
        holder.contactName.text = contactName
        var contactPhone = ticket.guardianPhoneNr
        holder.contactPhone.text = contactPhone
        var bookerName = ticket.bookerFullName
        holder.bookerName.text = bookerName


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

        if (ticket.teamColor == "Blue"){
            holder.teamButton.setBackgroundColor(Color.BLUE)
        }
        else if (ticket.teamColor == "Red"){
            holder.teamButton.setBackgroundColor(Color.RED)
        }
    }
}