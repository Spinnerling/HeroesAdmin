package com.example.heroadmin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewStubProxy
import com.example.heroadmin.databinding.FragmentMockBinding

class MockFragment : Fragment() {

    private lateinit var bottomPanelLayout : ViewStubProxy
    private lateinit var binding : FragmentMockBinding

    val players = List(20) {
        Ticket(
            "123", "Bob", "Polo", 16, 0, false, "None", false, 0, false, "None", "Lena", "Fagerlös", "070 123 45 67", "asdasdasd@email.com", "Booker", "Person", "elmailo@adress.end", "498 456 12 12", 0, 0, 0 ,0, 0, 0, 0, "None", "12345"
        )
        Ticket(
            "123", "Naito", "Mare", 13, 0, false, "None", false, 0, false, "None", "Pitch", "Black", "070 666 66 66", "123123@email.com", "Free", "Man", "ndkanjsd@asad.eee", "927 12 45 34", 0, 0, 0 ,0, 0, 0, 0, "None", "54321"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mock, container, false)

        bottomPanelLayout = binding.bottomPanelViewStub
        //bottomPanelLayout.viewStub?.layoutResource = R.layout.bottompanel_newround



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