package com.example.heroadmin

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
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
            val dialogView = LayoutInflater.from(eventView.context).inflate(R.layout.checkin_popup,null)

            val builder = AlertDialog.Builder(eventView.context)
                .setView(dialogView)

            val alertDialog = builder.show()
            val name : TextView = dialogView.findViewById<TextView>(R.id.checkInPopupNameText)
            name.text = ticket.fullName
            val userNo = dialogView.findViewById<EditText>(R.id.checkInPopupEditText)
            userNo.requestFocus()

            dialogView.findViewById<Button>(R.id.checkinAcceptButton).setOnClickListener{
                val number = userNo.text.toString().toInt()
                ticket.tabardNr = number
                ticket.checkedIn = true;
                eventView.updateTicketLists()
                Toast.makeText(eventView.context,"Checked in ${ticket.firstName}",Toast.LENGTH_SHORT).show()

                alertDialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.checkinCancelButton).setOnClickListener{
                Toast.makeText(eventView.context,"Cancelled",Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }
        }
    }

    fun openSoftKeyboard(context: Context, view: View) {
        view.requestFocus()
        // open the soft keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}