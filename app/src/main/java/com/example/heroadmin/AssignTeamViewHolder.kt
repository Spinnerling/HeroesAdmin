package com.example.heroadmin

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class AssignTeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    // Find all the views of the list item
    var nameText: TextView = itemView.findViewById(R.id.assignTeam_nameText)
    var ageText: TextView = itemView.findViewById(R.id.assignTeam_ageText)
    var checkIdButton : Button = itemView.findViewById(R.id.assignTeam_checkIdButton)
    var infoButton: Button = itemView.findViewById(R.id.assignTeam_infoButton)
    var blueButton: Button = itemView.findViewById(R.id.assignTeam_blueTeamButton)
    var redButton: Button = itemView.findViewById(R.id.assignTeam_redTeamButton)
    var hideNoteButton: Button = itemView.findViewById(R.id.assignTeam_HideNoteButton)
    var editNoteButton: Button = itemView.findViewById(R.id.assignTeam_editNoteButton)
    var groupName: Button = itemView.findViewById(R.id.assignTeam_GroupButton)

    var notePanel : LinearLayout = itemView.findViewById(R.id.assignTeam_notepanel)
    var itemInfoHeaders : LinearLayout = itemView.findViewById(R.id.assignTeam_itemInfoHeaders)
    var itemInfoTexts : LinearLayout = itemView.findViewById(R.id.assignTeam_itemInfoTexts)
    var itemButtons : LinearLayout = itemView.findViewById(R.id.assignTeam_itemButtons)
    var playerIdLayout : LinearLayout = itemView.findViewById(R.id.assignTeam_playerIdLayout)

    var contactName: TextView = itemView.findViewById(R.id.assignTeam_ContactName)
    var contactPhone: TextView = itemView.findViewById(R.id.assignTeam_ContactPhone)
    var bookerEmailText: TextView = itemView.findViewById(R.id.assignTeam_bookingEmail)
    var note: TextView = itemView.findViewById(R.id.assignTeam_Note)
    var playerId: TextView = itemView.findViewById(R.id.assignTeam_playerIdText)

}