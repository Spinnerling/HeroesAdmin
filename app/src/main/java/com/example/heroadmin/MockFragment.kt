package com.example.heroadmin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.heroadmin.databinding.BottompanelBinding
import com.example.heroadmin.databinding.FragmentMockBinding

class MockFragment : Fragment() {

    private lateinit var bottomPanelLayout: BottompanelBinding
    private lateinit var binding : FragmentMockBinding

    private val players = List(20) {
        var asd = getTicket("123")
        Log.i("123123", asd.ticketId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mock, container, false)
        bottomPanelLayout = binding.bottonPanelInclude
        //bottomPanelLayout.


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setTicketPlayer(ticket : Ticket) {
        if (ticket.playerId == "") {
            // Search for player

            // If player doesn't exist, create a new userId?
        }
        else {
            // See if userId exists

            // Otherwise, search for player
        }

        // Transfer ticket info to player
    }

    private fun autoSetRoleAmounts() {
        if (players.isEmpty()) {
            return
        }

        var healers = players.size / 16
        var mages = (players.size + 4) / 16
        var rogues = (players.size + 12) / 16
        var knights = (players.size + 8) / 16

    }

    private fun endEvent() {
        // Find eventId
        // Go to report screen
        // Pass along eventId
    }

}