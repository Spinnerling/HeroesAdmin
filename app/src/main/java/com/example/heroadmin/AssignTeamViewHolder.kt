package com.example.heroadmin

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AssignTeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    // Find all the views of the list item
    var nameText: TextView = itemView.findViewById(R.id.assignTeam_nameText)
    var ageText: TextView = itemView.findViewById(R.id.assignTeam_ageText)
    var checkIdButton : Button = itemView.findViewById(R.id.assignTeam_checkIdButton)
    var playerIdText: TextView = itemView.findViewById(R.id.assignTeam_playerId)
    var bookerEmailText: TextView = itemView.findViewById(R.id.assignTeam_bookingEmail)
    var infoButton: Button = itemView.findViewById(R.id.assignTeam_infoButton)
    var blueButton: Button = itemView.findViewById(R.id.assignTeam_blueTeamButton)
    var redButton: Button = itemView.findViewById(R.id.assignTeam_redTeamButton)

    var itemInfoHeaders : LinearLayout = itemView.findViewById(R.id.checkIn_itemInfoHeaders)
    var itemInfoTexts : LinearLayout = itemView.findViewById(R.id.checkIn_itemInfoTexts)

    var contactName: TextView = itemView.findViewById(R.id.assignTeam_ContactName)
    var contactPhone: TextView = itemView.findViewById(R.id.assignTeam_ContactPhone)
    var contactEmail: TextView = itemView.findViewById(R.id.assignTeam_ContactEmail)
    var bookerName: TextView = itemView.findViewById(R.id.assignTeam_BookingName)
    var bookerPhone: TextView = itemView.findViewById(R.id.assignTeam_BookingPhone)
    var note: TextView = itemView.findViewById(R.id.assignTeam_Note)

}