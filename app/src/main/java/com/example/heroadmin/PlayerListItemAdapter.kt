package com.example.heroadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerListItemAdapter(
    private var playerList: MutableList<PlayerListItem>,
    private val listener: OnItemTouchListener
) : RecyclerView.Adapter<PlayerListItemAdapter.PlayerListItemViewHolder>() {

    class PlayerListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firstName: TextView = itemView.findViewById(R.id.player_first_name)
        val lastName: TextView = itemView.findViewById(R.id.player_last_name)
        val age: TextView = itemView.findViewById(R.id.player_age)
        val bookerNames: TextView = itemView.findViewById(R.id.booker_names)
        val bookerPhones: TextView = itemView.findViewById(R.id.booker_phones)
        val bookerEmails: TextView = itemView.findViewById(R.id.booker_emails)
        val bookerAddresses: TextView = itemView.findViewById(R.id.booker_addresses)
        val moreButton: Button = itemView.findViewById(R.id.mlpli_moreButton)
    }

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerListItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.manual_player_listitem, parent, false)

        return PlayerListItemViewHolder(itemView)
    }

    override fun getItemCount() = playerList.size

    override fun onBindViewHolder(holder: PlayerListItemViewHolder, position: Int) {
        val currentItem = playerList[position]

        holder.firstName.text = currentItem.firstName
        holder.lastName.text = currentItem.lastName
        holder.age.text = currentItem.age.toString()

        // Update the UI based on the expanded state
        if (currentItem.isExpanded) {
            holder.bookerNames.text = currentItem.bookerNamesLong
            holder.bookerPhones.text = currentItem.bookerPhonesLong
            holder.bookerEmails.text = currentItem.bookerEmailsLong
            holder.bookerAddresses.text = currentItem.bookerAddressesLong
            holder.moreButton.text = "Less"
        } else {
            holder.bookerNames.text = currentItem.bookerNamesShort
            holder.bookerPhones.text = currentItem.bookerPhonesShort
            holder.bookerEmails.text = currentItem.bookerEmailsShort
            holder.bookerAddresses.text = currentItem.bookerAddressesShort
            holder.moreButton.text = "More"
        }

        holder.moreButton.visibility = if (currentItem.hasMoreInfo) View.VISIBLE else View.GONE

        // Add click listener for the "More" button
        holder.moreButton.setOnClickListener {
            currentItem.isExpanded = !currentItem.isExpanded
            notifyItemChanged(position)
        }

        // Add click listener for the whole item
        holder.itemView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                listener.onTouch(view, motionEvent, position, playerList[position])
            }
            true
        }

        // Change the background color or any other attribute to indicate the selected state
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F0F0F0")) // Selected item background color
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF")) // Default background color
        }
    }

    interface OnItemTouchListener {
        fun onTouch(view: View, event: MotionEvent, position: Int, playerListItem: PlayerListItem)
    }


    // Get the selected item
    fun getSelectedItem(): PlayerListItem? {
        return if (selectedPosition != -1) playerList[selectedPosition] else null
    }

    fun toggleSelection(position: Int) {
        if (selectedPosition == position) {
            selectedPosition = -1
        } else {
            selectedPosition = position
        }
        notifyDataSetChanged()
    }
}