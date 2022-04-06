package com.example.heroadmin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
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
    private lateinit var redTeamPowerText : TextView
    private lateinit var blueTeamPowerText : TextView
    private lateinit var redTeamAmountText : TextView
    private lateinit var blueTeamAmountText : TextView
    private lateinit var redTeamTeensText : TextView
    private lateinit var blueTeamTeensText : TextView
    private lateinit var redTeamTiniesText : TextView
    private lateinit var blueTeamTiniesText : TextView
    private lateinit var selectedTicket : Ticket
    private lateinit var playerOnOffSwitch : Switch

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
            redTeam = getTeamTickets(allTickets, false)
            blueTeam = getTeamTickets(allTickets, true)
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
        val playerCloseButton = binding.playerCloseButton
        val bottomPanel = binding.bottomPanel
        val bottomPanelNewRound = binding.bottomPanelNewRound
        val bottomPanelPlayer = binding.bottomPanelPlayer
        val newRoundButton = binding.newRoundButton

        redTeamPowerText = binding.redTeamPowerText
        blueTeamPowerText = binding.blueTeamPowerText
        redTeamAmountText = binding.redTeamAmountText
        blueTeamAmountText = binding.blueTeamAmountText
        redTeamTeensText = binding.redTeamTeensText
        blueTeamTeensText = binding.blueTeamTeensText
        redTeamTiniesText = binding.redTeamTiniesText
        blueTeamTiniesText = binding.blueTeamTiniesText
        playerOnOffSwitch = binding.playerOnOffSwitch

        // Set variables
        eventInfoDate.text = "Date: ${event.actualDate}"
        eventInfoTime.text = "Start: ${event.actualStartTime}"
        eventInfoVenue.text = "Venue: ${event.venue}"
        eventInfoPlayerAmount.text = "Tickets: ${allTickets.size} / ${event.playerMax}"

        updateTicketLists()

        assignTeamPanelButton.setOnClickListener {
            if (assignTeamList.layoutParams.height == 0) {
                assignTeamList.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_open, 0, 0, 0)
                assignTeamPanelButton.background.setTint(Color.RED)
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

        playerOnOffSwitch.setOnCheckedChangeListener{ _, isChecked ->
            selectedTicket.benched = !isChecked
            updateTicketLists()
        }

        playerCloseButton.setOnClickListener{
            bottomPanel.visibility = View.VISIBLE
            bottomPanelPlayer.visibility = View.GONE
        }

        newRoundButton.setOnClickListener{
            bottomPanel.visibility = View.GONE
            bottomPanelNewRound.visibility = View.VISIBLE
        }

    }

    fun updateTicketLists() {
        assignList = mutableListOf()
        checkInList = mutableListOf()
        redTeam = mutableListOf()
        blueTeam = mutableListOf()
        redBench = mutableListOf()
        blueBench = mutableListOf()

        for (ticket in allTickets){
            if (ticket.teamColor == "None"){
                assignList.add(ticket)
            }
            else if(!ticket.checkedIn){
                checkInList.add(ticket)
            }
            else if(ticket.teamColor == "Red"){
                if (ticket.benched){
                    redBench.add(ticket)
                }
                else{
                    redTeam.add(ticket)
                }
            }
            else if (ticket.teamColor == "Blue"){
                if (ticket.benched){
                    blueBench.add(ticket)
                }
                else{
                    blueTeam.add(ticket)
                }
            }
        }

        setAssignTeamAdapter()
        setCheckInAdapter()
        setTeamAdapters()
        checkListVisibilities()
        updateTeamPower()
    }

    private fun updateTeamPower() {
        var redPowerLevel = 0
        var bluePowerLevel = 0
        var redTeenAmount = 0
        var blueTeenAmount = 0
        var redTiniesAmount = 0
        var blueTiniesAmount = 0


        for (ticket in allTickets) {
            if (ticket.teamColor == "Red" && !ticket.benched) {
                redPowerLevel += ticket.age
                if (ticket.age > 12){
                    redTeenAmount++
                }
                else if (ticket.age < 8){
                    redTiniesAmount++
                }

            } else if (ticket.teamColor == "Blue" && !ticket.benched) {
                bluePowerLevel += ticket.age
                if (ticket.age > 12){
                    blueTeenAmount++
                }
                else if (ticket.age < 8){
                    blueTiniesAmount++
                }
            }
        }

        redTeamPowerText.text = redPowerLevel.toString()
        blueTeamPowerText.text = bluePowerLevel.toString()

        redTeamAmountText.text = redTeam.size.toString()
        blueTeamAmountText.text = blueTeam.size.toString()

        redTeamTeensText.text = redTeenAmount.toString()
        blueTeamTeensText.text = blueTeenAmount.toString()

        redTeamTiniesText.text = redTiniesAmount.toString()
        blueTeamTiniesText.text = blueTiniesAmount.toString()
    }

    private fun checkListVisibilities() {
        if (assignList.isEmpty()){
            binding.assignTeamPanel.visibility = View.GONE
        }
        else {
            binding.assignTeamPanel.visibility = View.VISIBLE
        }

        if (checkInList.isEmpty()){
            binding.checkInPanel.visibility = View.GONE
        }
        else {
            binding.checkInPanel.visibility = View.VISIBLE
        }
    }

    private fun setAssignTeamAdapter() {
        assignTeamAdapter = AssignTeamRecyclerAdapter(assignList, this)
        val layoutManager = LinearLayoutManager(v.context)
        val ticketList = binding.assignTeamRecycler
        ticketList.layoutManager = layoutManager
        ticketList.itemAnimator = DefaultItemAnimator()
        ticketList.adapter = assignTeamAdapter
    }

    private fun setCheckInAdapter() {
        checkInAdapter = CheckInRecyclerAdapter(checkInList, this)
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
        selectedTicket = allTickets[position]
        binding.bottomPanel.visibility = View.GONE
        binding.bottomPanelPlayer.visibility = View.VISIBLE
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
        blueBench.sortBy { it.fullName }
    }

    private fun sortBlueByNumber() {
        blueTeam.sortBy { it.tabardNr }
        blueBench.sortBy { it.tabardNr }
    }

    private fun sortBlueByRole() {
        blueTeam.sortBy { it.currentRole }
        blueBench.sortBy { it.currentRole }
    }

    private fun sortRedByName() {
        redTeam.sortBy { it.fullName }
        redBench.sortBy { it.fullName }
    }

    private fun sortRedByNumber() {
        redTeam.sortBy { it.tabardNr }
        redBench.sortBy { it.tabardNr }
    }

    private fun sortRedByRole() {
        redTeam.sortBy { it.currentRole }
        redBench.sortBy { it.currentRole }
    }


}