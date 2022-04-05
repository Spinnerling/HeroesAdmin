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

    var infoButton: Button = itemView.findViewById(R.id.checkin_infoButton)
    var teamButton: Button = itemView.findViewById(R.id.checkin_teamButton)
    var checkInButton: Button = itemView.findViewById(R.id.checkin_checkinButton)
    var removeButton: Button = itemView.findViewById(R.id.checkin_removeButton)

    var itemInfoHeaders : LinearLayout = itemView.findViewById(R.id.checkIn_itemInfoHeaders)
    var itemInfoTexts : LinearLayout = itemView.findViewById(R.id.checkIn_itemInfoTexts)

    var contactName: TextView = itemView.findViewById(R.id.assignTeam_ContactName)
    var contactPhone: TextView = itemView.findViewById(R.id.assignTeam_ContactPhone)
    var contactEmail: TextView = itemView.findViewById(R.id.assignTeam_ContactEmail)
    var bookerName: TextView = itemView.findViewById(R.id.assignTeam_BookingName)
    var bookerPhone: TextView = itemView.findViewById(R.id.assignTeam_BookingPhone)
}