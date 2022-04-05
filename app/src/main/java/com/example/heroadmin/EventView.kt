package com.example.heroadmin

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.heroadmin.databinding.FragmentEventViewBinding

class EventView : Fragment() {
    private lateinit var binding : FragmentEventViewBinding
    private lateinit var v : View
    private lateinit var args : EventViewArgs
    private lateinit var currEventId : String
    private lateinit var event : Event
    private lateinit var allPlayers: MutableList<Player>
    private lateinit var allTickets: MutableList<Ticket>
    private lateinit var redTeam: MutableList<Ticket>
    private lateinit var blueTeam: MutableList<Ticket>
    private var redBench: MutableList<Ticket> = mutableListOf()
    private var blueBench: MutableList<Ticket> = mutableListOf()
    private lateinit var assignList : MutableList<Ticket>
    private lateinit var checkInList : MutableList<Ticket>
    private lateinit var assignTeamAdapter : AssignTeamRecyclerAdapter
    private lateinit var checkInAdapter : CheckInRecyclerAdapter
    private lateinit var redTeamAdapter : TeamRecyclerAdapter
    private lateinit var blueTeamAdapter : TeamRecyclerAdapter
    private lateinit var redBenchAdapter : TeamRecyclerAdapter
    private lateinit var blueBenchAdapter : TeamRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_view, container, false)
        v = inflater.inflate(R.layout.fragment_event_view, container, false)

        args = EventViewArgs.fromBundle(requireArguments())
        currEventId = args.passedEventId.toString()
        event = getEvent(currEventId)
        allPlayers = getAllPlayers(currEventId)
        allTickets = getAllTickets(currEventId)

        if (allTickets.isNotEmpty()){
            Log.i("test", allTickets[0].teamColor)
            redTeam = getTeamTickets(allTickets, false)
            blueTeam = getTeamTickets(allTickets, true)
            Log.i("test", redTeam.size.toString() + " of red team")
        }
        else {
            Log.i("test", "no tickets found in event")
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {

        super.onResume()

        // Find elements
        val assignTeamPanelButton : Button = binding.assignTeamPanelButton
        val assignTeamList : LinearLayout = binding.assignTeamList
        val checkInPanelButton : Button = binding.checkInPanelButton
        val checkInList : LinearLayout = binding.checkInList
        val eventInfoDate = binding.dateText
        val eventInfoTime = binding.timeText
        val eventInfoVenue = binding.venueText
        val eventInfoPlayerAmount = binding.playerAmountText

        // Set variables
        eventInfoDate.text = "Date: ${event.actualDate}"
        eventInfoTime.text = "Start: ${event.actualStartTime}"
        eventInfoVenue.text = "Venue: ${event.venue}"
        eventInfoPlayerAmount.text = "Tickets: ${event.playerAmount.toString()} / ${event.playerMax}"

        setAssignTeamAdapter()
        setCheckInAdapter()
        setTeamAdapters()

        assignTeamPanelButton.setOnClickListener {
            if (assignTeamList.layoutParams.height == 0) {
                assignTeamList.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_open, 0, 0, 0)
            }
            else {
                assignTeamList.layoutParams.height = 0
                assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_closed, 0, 0, 0)
            }
        }

        checkInPanelButton.setOnClickListener {
            if (checkInList.layoutParams.height == 0) {
                checkInList.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_open, 0, 0, 0)
            }
            else {
                checkInList.layoutParams.height = 0
                checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_closed, 0, 0, 0)
            }
        }

    }

    private fun setAssignTeamAdapter() {
        assignTeamAdapter = AssignTeamRecyclerAdapter(allTickets)
        val layoutManager = LinearLayoutManager(v.context)
        val ticketList = binding.assignTeamRecycler
        ticketList.layoutManager = layoutManager
        ticketList.itemAnimator = DefaultItemAnimator()
        ticketList.adapter = assignTeamAdapter
    }

    private fun setCheckInAdapter() {
        checkInAdapter = CheckInRecyclerAdapter(allTickets)
        val layoutManager = LinearLayoutManager(v.context)
        val ticketList = binding.checkInRecycler
        ticketList.layoutManager = layoutManager
        ticketList.itemAnimator = DefaultItemAnimator()
        ticketList.adapter = checkInAdapter
    }

    private fun setTeamAdapters() {
        redTeamAdapter = TeamRecyclerAdapter(redTeam) { position -> onTeamItemClick(position)}
        blueTeamAdapter = TeamRecyclerAdapter(blueTeam) { position -> onTeamItemClick(position)}
        redBenchAdapter = TeamRecyclerAdapter(redBench) { position -> onTeamItemClick(position)}
        blueBenchAdapter = TeamRecyclerAdapter(blueBench) { position -> onTeamItemClick(position)}

        val layoutManagerR = LinearLayoutManager(v.context)
        val layoutManagerRB = LinearLayoutManager(v.context)
        val layoutManagerB = LinearLayoutManager(v.context)
        val layoutManagerBB = LinearLayoutManager(v.context)

        val ticketListR = binding.redTeamRecycler
        val ticketListRB = binding.redBenchedRecycler
        val ticketListB = binding.blueTeamRecycler
        val ticketListBB = binding.blueBenchedRecycler

        ticketListR.layoutManager = layoutManagerR
        ticketListR.itemAnimator = DefaultItemAnimator()
        ticketListR.adapter = redTeamAdapter

        ticketListB.layoutManager = layoutManagerB
        ticketListB.itemAnimator = DefaultItemAnimator()
        ticketListB.adapter = blueTeamAdapter

        ticketListRB.layoutManager = layoutManagerRB
        ticketListRB.itemAnimator = DefaultItemAnimator()
        ticketListRB.adapter = redBenchAdapter

        ticketListBB.layoutManager = layoutManagerBB
        ticketListBB.itemAnimator = DefaultItemAnimator()
        ticketListBB.adapter = blueBenchAdapter
    }

    private fun onTeamItemClick(position : Int) {
        //val args =  EventListArgs.fromBundle(requireArguments())
        val ticket = allTickets[position]
    }

    private fun updateAssignTeamList(){
        // Empty list
        assignList = mutableListOf()

        // Go through all the event's tickets
        val eventObject = getEvent(currEventId)
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
        val eventObject = getEvent(currEventId)
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

    fun updateTeamLists() {
        // Empty old lists

        for (i in redTeam.indices){
            val currTicket = redTeam[i]

            // Skip benched players
            if (currTicket.benched) {
                continue
            }

            // Set Name
            val name = currTicket.fullName
            // Set Numbers
            val number = currTicket.tabardNr
            // Set Roles
            val role = getRoleByNumber(currTicket.currentRole)

            // Pick out benched players
            if (currTicket.benched) {
                continue
            }
        }

        for (i in blueTeam.indices) {
            val currTicket = blueTeam[i]

            // Set Name
            val name = currTicket.fullName
            // Set Numbers
            val number = currTicket.tabardNr
            // Set Roles
            val role = getRoleByNumber(currTicket.currentRole)

            // Pick out benched players
            if (currTicket.benched) {
                continue
            }

            // Set players in field
        }

    }



    // SORTING FUNCTIONS

    private fun sortAssignByTicketId() {
        assignList.sortBy { it.ticketId }
    }

    private fun sortAssignByName() {
        assignList.sortBy { it.fullName }
    }

    private fun sortAssignByAge() {
        assignList.sortBy { it.age }
    }

    private fun sortAssignByEmail() {
        assignList.sortBy { it.bookerEmail }
    }


    private fun sortCheckInByTicketId() {
        checkInList.sortBy { it.ticketId }
    }

    private fun sortCheckInByName() {
        checkInList.sortBy { it.fullName }
    }

    private fun sortCheckInByAge() {
        checkInList.sortBy { it.age }
    }

    private fun sortCheckInByEmail() {
        checkInList.sortBy { it.bookerEmail }
    }


    private fun sortBlueByName() {
        blueTeam.sortBy { it.fullName }
        updateTeamLists()
    }

    private fun sortBlueByNumber() {
        blueTeam.sortBy { it.tabardNr }
        updateTeamLists()
    }

    private fun sortBlueByRole() {
        blueTeam.sortBy { it.currentRole }
        updateTeamLists()
    }

    private fun sortRedByName() {
        redTeam.sortBy { it.fullName }
        updateTeamLists()
    }

    private fun sortRedByNumber() {
        redTeam.sortBy { it.tabardNr }
        updateTeamLists()
    }

    private fun sortRedByRole() {
        redTeam.sortBy { it.currentRole }
        updateTeamLists()
    }


}