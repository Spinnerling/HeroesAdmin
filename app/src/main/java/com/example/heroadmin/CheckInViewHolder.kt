package com.example.heroadmin

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CheckInViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var nameText: TextView = itemView.findViewById(R.id.checkin_nameText)
    var ageText: TextView = itemView.findViewById(R.id.checkin_ageText)
    var note: TextView = itemView.findViewById(R.id.checkin_noteText)
    var groupName: Button = itemView.findViewById(R.id.checkin_groupButton)
    var ticketIdText: TextView = itemView.findViewById(R.id.checkin_ticketIdText)
    var playerIdText: TextView = itemView.findViewById(R.id.checkin_playerIdText)

    var infoButton: Button = itemView.findViewById(R.id.checkin_infoButton)
    var teamButton: Button = itemView.findViewById(R.id.checkin_teamButton)
    var unteamButton: Button = itemView.findViewById(R.id.checkin_unteamButton)
    var checkInButton: Button = itemView.findViewById(R.id.checkin_checkinButton)
    var hideNoteButton: Button = itemView.findViewById(R.id.checkin_HideNoteButton)
    var editNoteButton: Button = itemView.findViewById(R.id.checkin_editNoteButton)

    var notePanel : LinearLayout = itemView.findViewById(R.id.checkin_notepanel)
    var itemInfoHeaders : LinearLayout = itemView.findViewById(R.id.checkIn_itemInfoHeaders)
    var itemInfoTexts : LinearLayout = itemView.findViewById(R.id.checkIn_itemInfoTexts)
    var buttonPanel : LinearLayout = itemView.findViewById(R.id.checkin_buttonPanel)
    var idTitlesLayout : LinearLayout = itemView.findViewById(R.id.checkIn_idTitlesLayout)
    var idValuesLayout : LinearLayout = itemView.findViewById(R.id.checkIn_idValuesLayout)

    var contactName: TextView = itemView.findViewById(R.id.checkin_ContactName)
    var contactPhone: TextView = itemView.findViewById(R.id.checkin_ContactPhone)
    var bookerEmail: TextView = itemView.findViewById(R.id.checkin_BookerEmail)

    var groupButton: Button = itemView.findViewById(R.id.checkin_groupButton)
}