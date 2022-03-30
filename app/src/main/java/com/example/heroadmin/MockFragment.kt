package com.example.heroadmin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.heroadmin.databinding.FragmentMockBinding

class MockFragment : Fragment() {

    private lateinit var binding: FragmentMockBinding
    private var currEvent: String = "event123"
    private var allPlayers: MutableList<Player> = getAllPlayers(currEvent)
    private var allTickets: MutableList<Ticket> = getAllTickets(currEvent)
    private var redTeam: MutableList<Ticket>? = getTeamPlayers(allTickets, false)
    private var blueTeam: MutableList<Ticket>? = getTeamPlayers(allTickets, true)
    private lateinit var redTeamNames : MutableList<String>
    private lateinit var blueTeamNames : MutableList<String>
    private lateinit var redTeamNumbers : MutableList<Int>
    private lateinit var blueTeamNumbers : MutableList<Int>
    private lateinit var redTeamRoles : MutableList<String>
    private lateinit var blueTeamRoles : MutableList<String>
    private lateinit var redBenchedNames : MutableList<String>
    private lateinit var blueBenchedNames : MutableList<String>
    private lateinit var redBenchedNumbers : MutableList<Int>
    private lateinit var blueBenchedNumbers : MutableList<Int>
    private lateinit var redBenchedRoles : MutableList<String>
    private lateinit var blueBenchedRoles : MutableList<String>
    private lateinit var assignList : MutableList<Ticket>
    private lateinit var checkInList : MutableList<Ticket>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mock, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun updateAssignTeamList(){
        // Empty list
        assignList = mutableListOf()

        // Go through all the event's tickets
        val eventObject = getEvent(currEvent)
        for(i in eventObject.tickets.indices) {

            // Find the ones who don't have a team yet
            val currTicket = getTicket(eventObject.tickets[i])
            if (currTicket.teamColor == "None"){
                assignList.add(currTicket)
            }
        }
    }

    private fun updateCheckInList(){
        // Empty list
        checkInList = mutableListOf()

        // Go through all the event's tickets
        val eventObject = getEvent(currEvent)
        for(i in eventObject.tickets.indices) {

            // Find the ones who haven't checked in yet
            val currTicket = getTicket(eventObject.tickets[i])
            if (!currTicket.checkedIn){
                checkInList.add(currTicket)
            }
        }
    }

    private fun setTicketPlayer(ticket: Ticket) {
        if (ticket.playerId == "") {
            // Search for player

            // If player doesn't exist, create a new userId?
        } else {
            // See if userId exists

            // Otherwise, search for player
        }

        // Transfer ticket info to player
    }

    private fun autoSetRoleAmounts() {
        if (allPlayers.isEmpty()) {
            return
        }

        var healers = allPlayers.size / 16
        var mages = (allPlayers.size + 4) / 16
        var rogues = (allPlayers.size + 12) / 16
        var knights = (allPlayers.size + 8) / 16

    }

    private fun endEvent() {
        // Find eventId
        // Go to report screen
        // Pass along eventId
    }

    private fun updateTeamLists() {
        // Empty old lists
        redTeamNames = mutableListOf()
        redTeamNumbers = mutableListOf()
        redTeamRoles = mutableListOf()
        blueTeamNames = mutableListOf()
        blueTeamNumbers = mutableListOf()
        blueTeamRoles = mutableListOf()

        for (i in redTeam?.indices!!){
            val currTicket = redTeam!![i]

            // Skip benched players
            if (currTicket.benched) {
                continue
            }

            // Set Name
            val name = currTicket.firstName + " " + currTicket.lastName
            // Set Numbers
            val number = currTicket.tabardNr
            // Set Roles
            val role = getRoleByNumber(currTicket.currentRole)

            // Pick out benched players
            if (currTicket.benched) {
                redBenchedNames.add(name)
                redBenchedNumbers.add(number)
                redBenchedRoles.add("Undecided")
                continue
            }

            // Set players in field
            redTeamNames.add(name)
            redTeamNumbers.add(number)
            redTeamRoles.add(role)
        }

        for (i in blueTeam?.indices!!) {
            val currTicket = blueTeam!![i]

            // Set Name
            val name = currTicket.firstName + " " + currTicket.lastName
            // Set Numbers
            val number = currTicket.tabardNr
            // Set Roles
            val role = getRoleByNumber(currTicket.currentRole)

            // Pick out benched players
            if (currTicket.benched) {
                blueBenchedNames.add(name)
                blueBenchedNumbers.add(number)
                blueBenchedRoles.add("Undecided")
                continue
            }

            // Set players in field
            blueTeamNames.add(name)
            blueTeamNumbers.add(number)
            blueTeamRoles.add(role)
        }

    }



    // SORTING FUNCTIONS

    private fun sortAssignByUserId() {

    }

    private fun sortAssignByName() {

    }

    private fun sortAssignByAge() {

    }

    private fun sortAssignByEmail() {

    }


    private fun sortCheckInByUserId() {

    }

    private fun sortCheckInByName() {

    }

    private fun sortCheckInByAge() {

    }

    private fun sortCheckInByEmail() {

    }


    private fun sortBlueByName() {
        blueTeam?.sortBy { it.firstName }
        updateTeamLists()
    }

    private fun sortBlueByNumber() {
        blueTeam?.sortBy { it.tabardNr }
        updateTeamLists()
    }

    private fun sortBlueByRole() {
        blueTeam?.sortBy { it.currentRole }
        updateTeamLists()
    }

    private fun sortRedByName() {
        redTeam?.sortBy { it.firstName }
        updateTeamLists()
    }

    private fun sortRedByNumber() {
        redTeam?.sortBy { it.tabardNr }
        updateTeamLists()
    }

    private fun sortRedByRole() {
        redTeam?.sortBy { it.currentRole }
        updateTeamLists()
    }


}