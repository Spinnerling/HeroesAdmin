package com.example.heroadmin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.heroadmin.databinding.FragmentEventViewBinding
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.random.Random


class EventView : Fragment() {
    private lateinit var currActivity: MainActivity
    private lateinit var binding: FragmentEventViewBinding
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
    private lateinit var args: EventViewArgs
    private lateinit var currEventId: String
    private lateinit var event: Event
    private lateinit var allTickets: MutableList<Ticket>
    private lateinit var redTeam: MutableList<Ticket>
    private lateinit var blueTeam: MutableList<Ticket>
    private var redBench: MutableList<Ticket> = mutableListOf()
    private var blueBench: MutableList<Ticket> = mutableListOf()
    private lateinit var assignList: MutableList<Ticket>
    private lateinit var checkInList: MutableList<Ticket>
    private lateinit var assignTeamAdapter: AssignTeamRecyclerAdapter
    private lateinit var checkInAdapter: CheckInRecyclerAdapter
    private lateinit var redTeamAdapter: TeamRecyclerAdapter
    private lateinit var blueTeamAdapter: TeamRecyclerAdapter
    private lateinit var redBenchAdapter: TeamRecyclerAdapter
    private lateinit var blueBenchAdapter: TeamRecyclerAdapter
    private lateinit var redTeamPowerText: TextView
    private lateinit var blueTeamPowerText: TextView
    private lateinit var redTeamAmountText: TextView
    private lateinit var blueTeamAmountText: TextView
    private lateinit var redTeamTeensText: TextView
    private lateinit var blueTeamTeensText: TextView
    private lateinit var redTeamTiniesText: TextView
    private lateinit var blueTeamTiniesText: TextView
    lateinit var selectedTicket: Ticket
    private lateinit var selectedPlayer: Player
    private lateinit var playerOnOffSwitch: Switch
    lateinit var selectedTicketTVH: TeamViewHolder
    private lateinit var playerExpText: TextView
    private lateinit var bottomPanel: LinearLayout
    private lateinit var bottomPanelPlayer: LinearLayout
    private lateinit var bottomPanelNewRound: LinearLayout
    private lateinit var playerRoleButtonPanel: LinearLayout
    private lateinit var loadingDialogue: AlertDialog
    private var healerAmount: Int = 0
    private var rogueAmount: Int = 0
    private var mageAmount: Int = 0
    private var knightAmount: Int = 0
    private var specialAAmount: Int = 0
    private var specialBAmount: Int = 0
    private var firstPlayerSelected: Boolean = false
    private var assignSorting = 3
    private var checkInSorting = 0
    private var teamSorting = 0
    private var takenGroupNames = mutableListOf<String>()
    private var redPowerLevel = 0
    private var bluePowerLevel = 0
    private var redTeenAmount = 0
    private var blueTeenAmount = 0
    private var redTiniesAmount = 0
    private var blueTiniesAmount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_view, container, false)
        v = inflater.inflate(R.layout.fragment_event_view, container, false)
        DBF = DatabaseFunctions(v.context)
        args = EventViewArgs.fromBundle(requireArguments())
        currEventId = args.passedEventId
        currActivity = (activity as MainActivity)
        event = currActivity.event

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {

        super.onResume()

        // Find elements
        val assignTeamPanelButton: Button = binding.assignTeamPanelButton
        val assignTeamList: LinearLayout = binding.assignTeamList
        val checkInPanelButton: Button = binding.checkInPanelButton
        val checkInList: LinearLayout = binding.checkInList
        val eventInfoDate = binding.dateText
        val eventInfoTime = binding.timeText
        val eventInfoVenue = binding.venueText
        val eventInfoPlayerAmount = binding.playerAmountText
        val playerCloseButton = binding.playerCloseButton
        val newRoundButton = binding.newRoundButton
        val cancelNewRoundButton = binding.cancelNewRoundButton
        val switchTeamButton = binding.switchTeamButton
        val switchTeamButton2 = binding.switchTeamButton2
        val spendExpButton = binding.spendExpButton

        playerExpText = binding.playerExpText
        redTeamPowerText = binding.redTeamPowerText
        blueTeamPowerText = binding.blueTeamPowerText
        redTeamAmountText = binding.redTeamAmountText
        blueTeamAmountText = binding.blueTeamAmountText
        redTeamTeensText = binding.redTeamTeensText
        blueTeamTeensText = binding.blueTeamTeensText
        redTeamTiniesText = binding.redTeamTiniesText
        blueTeamTiniesText = binding.blueTeamTiniesText
        playerOnOffSwitch = binding.playerOnOffSwitch
        bottomPanel = binding.bottomPanel
        bottomPanelNewRound = binding.bottomPanelNewRound
        bottomPanelPlayer = binding.bottomPanelPlayer
        playerRoleButtonPanel = binding.playerRoleButtonPanel

        loadingPopup()
        getAllTickets(event)

        // Set variables
        eventInfoDate.text = "Date: ${event.actualDate}"
        eventInfoTime.text = "Start: ${event.actualStartTime}"
        eventInfoVenue.text = "Venue: ${event.venue}"
        eventInfoPlayerAmount.text = "Tickets: ${allTickets.size} / ${event.playerMax}"

        assignTeamPanelButton.setOnClickListener {
            if (assignTeamList.layoutParams.height == 0) {
                assignTeamList.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_open,
                    0,
                    0,
                    0
                )
            } else {
                assignTeamList.layoutParams.height = 0
                assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_closed,
                    0,
                    0,
                    0
                )
            }
        }

        checkInPanelButton.setOnClickListener {
            if (checkInList.layoutParams.height == 0) {
                checkInList.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                // Set button arrow icon
                checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_open,
                    0,
                    0,
                    0
                )
            } else {
                checkInList.layoutParams.height = 0

                // Set button arrow icon
                checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_closed,
                    0,
                    0,
                    0
                )
            }
        }

        playerOnOffSwitch.setOnClickListener {
            if (selectedTicket.benched == 0) {
                selectedTicket.benched = 1
            } else {
                selectedTicket.benched = 0
            }
            updateTicketLists()
        }

        playerCloseButton.setOnClickListener {
            deselectPlayer()
        }

        newRoundButton.setOnClickListener {
            deselectPlayer()
            bottomPanel.visibility = View.GONE
            bottomPanelNewRound.visibility = View.VISIBLE
        }

        cancelNewRoundButton.setOnClickListener {
            deselectPlayer()
            bottomPanel.visibility = View.VISIBLE
            bottomPanelNewRound.visibility = View.GONE
            playerRoleButtonPanel.visibility = View.INVISIBLE
        }

        switchTeamButton.setOnClickListener {
            switchTeam()
        }

        switchTeamButton2.setOnClickListener {
            switchTeam()
        }

        spendExpButton.setOnClickListener {
            findNavController().navigate(
                EventViewDirections.actionEventViewToLevelUpFragment(
                    selectedTicket.playerId
                )
            )
        }

        binding.ticketInfoButton.setOnClickListener {
            openTicketInfo()
        }

        binding.rollRoundButton2.setOnClickListener {
            randomizeRoles()
        }

        binding.devButton.setOnClickListener {
            var team = "Blue"
            for (ticket in allTickets) {
                ticket.checkedIn = 1
                if (team == "Blue") {
                    ticket.teamColor = "Blue"
                    team = "Red"
                } else {
                    ticket.teamColor = "Red"
                    team = "Blue"
                }
            }
            updateTicketLists()
        }

        binding.assignTeamOrgByNameButton.setOnClickListener {
            assignSorting = 0
            updateTicketLists()
        }
        binding.assignTeamOrgByAgeButton.setOnClickListener {
            assignSorting = 1
            updateTicketLists()
        }
        binding.assignTeamOrgByUserIDButton.setOnClickListener {
            assignSorting = 2
            updateTicketLists()
        }
        binding.assignTeamOrgByGroupButton.setOnClickListener {
            assignSorting = 3
            updateTicketLists()
        }

        binding.checkInOrgByNameButton.setOnClickListener {
            checkInSorting = 0
            updateTicketLists()
        }
        binding.checkInOrgByAge.setOnClickListener {
            checkInSorting = 1
            updateTicketLists()
        }
        binding.checkInOrgByNote.setOnClickListener {
            checkInSorting = 2
            updateTicketLists()
        }
        binding.checkInOrgByColor.setOnClickListener {
            checkInSorting = 3
            updateTicketLists()
        }

        binding.teamNameButton1.setOnClickListener {
            teamSorting = 0
            updateTicketLists()
        }
        binding.teamNumberButton1.setOnClickListener {
            teamSorting = 1
            updateTicketLists()
        }
        binding.teamRoleButton1.setOnClickListener {
            teamSorting = 2
            updateTicketLists()
        }

        binding.teamNameButton2.setOnClickListener {
            teamSorting = 0
            updateTicketLists()
        }
        binding.teamNumberButton2.setOnClickListener {
            teamSorting = 1
            updateTicketLists()
        }
        binding.teamRoleButton2.setOnClickListener {
            teamSorting = 2
            updateTicketLists()
        }
        binding.awardExpButton.setOnClickListener {
            openAwardExp()
        }
        binding.refreshButton.setOnClickListener {
            getEvent()
            binding.refreshButton.isEnabled = false
            loadingDialogue.show()
        }
        binding.assignTeamAutoAssignButton.setOnClickListener {
            autoAssignLoop()
        }
        binding.endEventButton.setOnClickListener {
            endEvent()
        }
    }

    private fun loadingPopup() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.loading_popup, null)

        val builder =
            AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
                .setView(dialogView)

        loadingDialogue = builder.show()
    }

    private fun getEvent() {
        DBF.apiCallGet(
            "https://talltales.nu/API/api/event.php?id=" + currEventId,
            ::refreshEvent, {}
        )
    }

    private fun refreshEvent(response: JSONObject) {
        val dataArray: JSONArray = response.getJSONArray("data")
        val eventJson: JSONObject = dataArray.getJSONObject(0)
        val ticketIdJsonArray: JSONArray = eventJson.getJSONArray("TicketIDs")
        val ticketIdList = MutableList(ticketIdJsonArray.length()) {
            ticketIdJsonArray.getString(it)
        }

        event = Event(
            eventJson.getString("ID"),
            eventJson.getString("Event_Title"),
            eventJson.getString("Event_Start_date"),
            eventJson.getString("Event_End_Date"),
            eventJson.getString("Venue_ID"),
            eventJson.getString("Report_Text"),
            eventJson.getString("Description"),
            eventJson.getInt("EXP_Blueteam"),
            eventJson.getInt("EXP_Redteam"),
            eventJson.getInt("EXP_Attendance"),
            eventJson.getInt("EXP_Recruit"),
            eventJson.getInt("Round"),
            eventJson.getString("Status"),
            ticketIdList
        )

        getAllTickets(event)
    }

    private fun getAllTickets(event: Event) {
        // Get the event's ticket ids
        val allTicketIds: MutableList<String> = event.tickets

        // Create an array of the players connected to the tickets
        allTickets = mutableListOf()
//        for (i in allTicketIds.indices) {
//            getTicket(allTicketIds[i])
//        }

        //Temporary faux Tickets
        val ticket = Ticket(
            "1234",
            "",
            "",
            12,
            "",
            "",
            "",
            "",
            "",
            1,
            "",
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            ""

        )
        
        allTickets.add(ticket)
        updateTicketLists()
        DBF.getTicketGuardians(allTickets)

        loadingDialogue.dismiss()
        binding.refreshButton.isEnabled = true
    }

    private fun getTicket(ticketId: String) {
        // Find ticket in database by ticketId, return an array of its contents
        DBF.apiCallGet(
            "https://talltales.nu/API/api/ticket.php?id=$ticketId",
            ::parseTicket, {}
        )
    }

    private fun parseTicket(response: JSONObject) {
        val ticket = Ticket(
            response.getString("Ticket_ID"),
            response.getString("First_Name"),
            response.getString("Last_Name"),
            response.getInt("Age"),
            response.getString("KP_Name"),
            response.getString("KP_Phone_Nr"),
            response.getString("Booking_Mail"),
            response.getString("Booking_Name"),
            response.getString("Team_Color"),
            response.getInt("Tabard_Nr"),
            response.getString("Note"),
            response.getInt("Checked_In"),
            response.getInt("Recruits"),
            response.getInt("EXP_Personal"),
            response.getInt("Benched"),
            response.getInt("Guaranteed_Role"),
            response.getInt("Rounds_M"),
            response.getInt("Rounds_O"),
            response.getInt("Rounds_K"),
            response.getInt("Rounds_H"),
            response.getInt("Rounds_R"),
            response.getInt("Respawns"),
            response.getInt("Current_Role"),
            response.getString("Player_ID"),
            //response.getString("Group"),
        )

        allTickets.add(ticket)

        // If this is the last ticket to be parsed, update lists
        if (allTickets.size >= event.tickets.size) {
            updateTicketLists()
            DBF.getTicketGuardians(allTickets)

            loadingDialogue.dismiss()
            binding.refreshButton.isEnabled = true
        }
    }

    private fun deselectPlayer() {
        if (!firstPlayerSelected) {
            return
        }
        if (bottomPanelPlayer.visibility == View.VISIBLE) {
            bottomPanel.visibility = View.VISIBLE
            bottomPanelPlayer.visibility = View.GONE
        }
        selectedTicketTVH.deselect()
    }

    fun updateTicketLists() {
        assignList = mutableListOf()
        checkInList = mutableListOf()
        redTeam = mutableListOf()
        blueTeam = mutableListOf()
        redBench = mutableListOf()
        blueBench = mutableListOf()

        for (ticket in allTickets) {
            if (ticket.teamColor == "None" || ticket.teamColor == "") {
                assignList.add(ticket)
            } else if (ticket.checkedIn == 0) {
                checkInList.add(ticket)
            } else if (ticket.teamColor == "Red") {
                if (ticket.benched == 1) {
                    redBench.add(ticket)
                } else {
                    redTeam.add(ticket)
                }
            } else if (ticket.teamColor == "Blue") {
                if (ticket.benched == 1) {
                    blueBench.add(ticket)
                } else {
                    blueTeam.add(ticket)
                }
            }
        }

        updateTicketGroups()

        when (assignSorting) {
            0 -> {
                sortAssignByName()
            }
            1 -> {
                sortAssignByAge()
            }
            2 -> {
                sortAssignByUserId()
            }
            3 -> {
                sortAssignByGroup()
            }
        }

        when (checkInSorting) {
            0 -> {
                sortCheckInByName()
            }
            1 -> {
                sortCheckInByAge()
            }
            2 -> {
                sortCheckInByNote()
            }
            3 -> {
                sortCheckInByColor()
            }
        }

        when (teamSorting) {
            0 -> {
                sortTeamsByName()
            }
            1 -> {
                sortTeamsByNumber()
            }
            2 -> {
                sortTeamsByRole()
            }
        }

        setAssignTeamAdapter()
        setCheckInAdapter()
        setTeamAdapters()
        checkListVisibilities()
        updateTeamPower()
        autoSetRoleAmounts()
    }

    private fun updateTicketGroups() {
        takenGroupNames = mutableListOf()

        for (i in assignList.indices) {
            val ticket1 = assignList[i]

            if (ticket1.group == "SELF") {
                continue
            }

            for (j in assignList.indices) {
                val ticket2 = assignList[j]
                // Skip until next ticket in line
                if (j <= i || ticket2 == ticket1 || ticket2.group == "SELF") {
                    continue
                }

                // Skip if already in the same existing group
                if (ticket1.group != "" && ticket1.group == ticket2.group) {
                    continue
                }

                // Check if tickets are booked by same email
                if (ticket1.bookerEmail == ticket2.bookerEmail) {
                    // Check if both tickets have no group yet
                    if (ticket1.group == "" && ticket2.group == "") {

                        // Randomize new group name and put both in it
                        val nameArray = resources.getStringArray(R.array.groupNames)
                        var randomValue = Random.nextInt(nameArray.size)
                        var newGroupName: String = nameArray[randomValue].lowercase()

                        // Check that the name is not already taken
                        while (takenGroupNames.contains(newGroupName)) {
                            randomValue = Random.nextInt(nameArray.size)
                            newGroupName = nameArray[randomValue].lowercase()
                        }

                        takenGroupNames.add(newGroupName)
                        ticket1.group = newGroupName
                        ticket2.group = newGroupName

                        continue
                    }

                    // Check if they are already split up into different existing groups
                    if (ticket1.group != "" && ticket2.group != "" && ticket1.group != ticket2.group) {
                        continue
                    }

                    // Put the ticket with empty group into the other one's group
                    if (ticket1.group == "") {
                        ticket1.group = ticket2.group
                        continue
                    }

                    if (ticket2.group == "") {
                        ticket2.group = ticket1.group
                        continue
                    }
                }
            }

            if (ticket1.group != "") {
                ticket1.groupSize = assignList.count { it.group == ticket1.group }
            } else {
                ticket1.groupSize = 1
            }
        }
    }

    fun setGroupColor(group: String, setBlue: Boolean, setDatabase: Boolean) {
        for (ticket in allTickets) {
            if (ticket.group == group) {
                // Set Color
                if (setBlue) {
                    ticket.teamColor = "Blue"
                } else {
                    ticket.teamColor = "Red"
                }

                if (setDatabase) {
                    // Update database
                    val parcel = DBF.createTicketMap(ticket)
                    DBF.apiCallPost("https://talltales.nu/API/api/update-ticket.php", parcel)
                }

                // Check if Ticket is connected to a Player
                if (ticket.playerId == "") {
                    createNewPlayer(ticket)
                }
            }
        }
        updateTeamPower()
    }

    private fun createNewPlayer(ticket: Ticket) {
        val player = Player(
            getNewPlayerId(),
            ticket.firstName,
            ticket.lastName,
            ticket.age,
            0,
            mutableListOf(1, 0, 0),
            mutableListOf(1, 0, 0),
            mutableListOf(1, 0, 0),
            mutableListOf(1, 0, 0),
            mutableListOf(1, 0, 0),
            mutableListOf("", "", ""),
        )
        ticket.playerId = player.playerId
    }

    private fun getNewPlayerId(): String {
        val playerId = "0"
        return playerId
    }

    private fun autoAssignLoop() {
        var loop = 0
        var bestDifference = 1000
        var originalList: MutableList<Ticket> = assignList
        var bestBlueList: MutableList<Ticket> = mutableListOf()
        var bestRedList: MutableList<Ticket> = mutableListOf()

        while (loop < 50) {
            // Randomize and save version of list
            //assignList.shuffle()
            val currList = originalList

            // Assign teams with that list (which also sorts it)
            autoAssignTeams()

            // Check how well that went
            val difference = abs(bluePowerLevel - redPowerLevel)
            if (difference < bestDifference) {

                // If better than before, save that version of team lists
                bestDifference = difference

                bestRedList = mutableListOf()
                bestBlueList = mutableListOf()

                for (ticket in assignList) {
                    if (ticket.teamColor == "Blue") {
                        bestBlueList.add(ticket)
                    } else {
                        bestRedList.add(ticket)
                    }
                }

                // If already found best difference, break out of loop
                if (bestDifference == 0) {
                    break
                }
            }
            loop++
        }

        for (ticket in bestBlueList) {
            DBF.setTicketTeamColor(ticket, true)
        }
        for (ticket in bestRedList) {
            DBF.setTicketTeamColor(ticket, false)
        }
        updateTeamPower()

        updateTicketLists()
    }

    private fun autoAssignTeams() {
        sortAssignByGroup()

        // Create list of groups
        val groupList = mutableListOf<String>()
        for (ticket in assignList) {
            // Only take groups larger than 1
            if (ticket.group != "" && !groupList.contains(ticket.group)) {
                groupList.add(ticket.group)
            }
        }

/*
        // Find group stats
        val statList: MutableList<MutableList<Any>> = mutableListOf()

        for (group in groupList) {
            val groupStats: MutableList<Any> = mutableListOf("", 0, 0, 0, 0)
            for (ticket in assignList) {
                if (ticket.group == group) {
                    groupStats[0] = group
                    groupStats[1] = groupStats[1] as Int + ticket.powerLevel
                    groupStats[2] = groupStats[2] as Int + 1
                    if (ticket.age > 12) {
                        groupStats[3] = groupStats[3] as Int + 1
                    } else if (ticket.age < 7) {
                        groupStats[4] = groupStats[4] as Int + 1
                    }
                }
            }
            statList.add(groupStats)
        }*/

        // Assign all groups
        for (group in groupList) {
            if (bluePowerLevel > redPowerLevel) {
                setGroupColor(group, false, false)
            } else {
                setGroupColor(group, true, false)
            }
            updateTeamPower()
        }

        // Assign all loners
        sortAssignByAge()

        for (ticket in assignList) {
            if (ticket.group == "") {
                if (bluePowerLevel > redPowerLevel) {
                    ticket.teamColor = "Red"
                } else {
                    ticket.teamColor = "Blue"
                }
            }
            updateTeamPower()
        }
    }

    private fun updateTeamPower() {
        redPowerLevel = 0
        bluePowerLevel = 0
        redTeenAmount = 0
        blueTeenAmount = 0
        redTiniesAmount = 0
        blueTiniesAmount = 0
        var redAmount = 0
        var blueAmount = 0

        for (ticket in allTickets) {
            if (ticket.teamColor == "Red" && ticket.benched == 0) {
                redPowerLevel += ticket.powerLevel
                if (ticket.age > 12) {
                    redTeenAmount++
                } else if (ticket.age < 8) {
                    redTiniesAmount++
                }
                redAmount++

            } else if (ticket.teamColor == "Blue" && ticket.benched == 0) {
                bluePowerLevel += ticket.powerLevel
                if (ticket.age > 12) {
                    blueTeenAmount++
                } else if (ticket.age < 8) {
                    blueTiniesAmount++
                }
                blueAmount++
            }
        }

        redTeamPowerText.text = redPowerLevel.toString()
        blueTeamPowerText.text = bluePowerLevel.toString()

        redTeamAmountText.text = redAmount.toString()
        blueTeamAmountText.text = blueAmount.toString()

        redTeamTeensText.text = redTeenAmount.toString()
        blueTeamTeensText.text = blueTeenAmount.toString()

        redTeamTiniesText.text = redTiniesAmount.toString()
        blueTeamTiniesText.text = blueTiniesAmount.toString()
    }

    private fun checkListVisibilities() {
        if (assignList.isEmpty()) {
            binding.assignTeamPanel.visibility = View.GONE
        } else {
            binding.assignTeamPanel.visibility = View.VISIBLE
        }

        if (checkInList.isEmpty()) {
            binding.checkInPanel.visibility = View.GONE
        } else {
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
        redTeamAdapter =
            TeamRecyclerAdapter(redTeam, { position -> onTeamItemClick(position) }, this)
        blueTeamAdapter =
            TeamRecyclerAdapter(blueTeam, { position -> onTeamItemClick(position) }, this)
        redBenchAdapter =
            TeamRecyclerAdapter(redBench, { position -> onTeamItemClick(position) }, this)
        blueBenchAdapter =
            TeamRecyclerAdapter(blueBench, { position -> onTeamItemClick(position) }, this)

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

    private fun onTeamItemClick(position: Int) {
        if (!firstPlayerSelected) {
            deselectPlayer()
        }
        selectedPlayer = DBF.getPlayer(selectedTicket.ticketId)

        if (bottomPanelNewRound.visibility == View.VISIBLE) {
            playerRoleButtonPanel.visibility = View.VISIBLE
        } else {
            binding.bottomPanel.visibility = View.GONE
            binding.bottomPanelPlayer.visibility = View.VISIBLE
            binding.playerNameText.text = selectedTicket.fullName

            if (selectedTicket.teamColor == "Blue") {
                binding.playerNameText.setBackgroundResource(R.color.teamBlueColor)
            } else {
                binding.playerNameText.setBackgroundResource(R.color.teamRedColor)
            }

            playerExpText.text = "${selectedPlayer.totalExp} EXP kvar"
            val roleInText = DBF.getRoleByNumber(selectedTicket.currentRole)
            binding.ticketRoleText.text = roleInText

            if (selectedTicket.benched == 0) {
                playerOnOffSwitch.isChecked = true
            } else if (selectedTicket.benched == 1) {
                playerOnOffSwitch.isChecked = false
            }
        }
    }

    fun setTicketTabardNumber(ticket: Ticket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.checkin_popup, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()
        val name: TextView = dialogView.findViewById(R.id.checkInPopupNameText)
        name.text = ticket.fullName
        if (ticket.teamColor == "Blue") {
            name.setBackgroundResource(R.color.teamBlueColor)
        } else {
            name.setBackgroundResource(R.color.teamRedColor)
        }

        val userNo = dialogView.findViewById<EditText>(R.id.checkInPopupEditText)
        userNo.requestFocus()

        dialogView.findViewById<Button>(R.id.checkinAcceptButton).setOnClickListener {
            val number = userNo.text.toString()
            if (number != "") {
                // Update locally
                ticket.tabardNr = number.toInt()
                ticket.checkedIn = 1
                updateTicketLists()

                // Update database
                val parcel = DBF.createTicketMap(ticket)
                DBF.apiCallPost("https://talltales.nu/API/api/update-ticket.php", parcel)

                alertDialog.dismiss()
            }
        }
        dialogView.findViewById<Button>(R.id.checkinCancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    private fun openTicketInfo() {
        val ticket = selectedTicket
        val dialogView = LayoutInflater.from(context).inflate(R.layout.ticket_info, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val playerInfoDialog = builder.show()

        // Fill with info
        val name: TextView = dialogView.findViewById(R.id.ti_playerName)
        name.text = ticket.fullName
        val userAge: TextView = dialogView.findViewById(R.id.ti_playerAge)
        userAge.text = ticket.age.toString()
        val userId: TextView = dialogView.findViewById(R.id.ti_playerUserId)
        userId.text = ticket.playerId
        val ticketNote: TextView = dialogView.findViewById(R.id.ti_Note)
        ticketNote.text = ticket.note
        val guardianName: TextView = dialogView.findViewById(R.id.ti_guardianName)
        guardianName.text = ticket.guardianName
        val guardianPhone: TextView = dialogView.findViewById(R.id.ti_guardianPhone)
        guardianPhone.text = ticket.guardianPhoneNr
        val bookerName: TextView = dialogView.findViewById(R.id.ti_bookerName)
        bookerName.text = ticket.bookerFullName
        val bookerEmail: TextView = dialogView.findViewById(R.id.ti_bookerEmail)
        bookerEmail.text = ticket.bookerEmail

        // Close window
        dialogView.findViewById<Button>(R.id.ti_closeButton).setOnClickListener {
            playerInfoDialog.dismiss()
        }
    }

    private fun openAwardExp() {
        val ticket = selectedTicket
        val dialogView = LayoutInflater.from(context).inflate(R.layout.award_exp, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()
        val name: TextView = dialogView.findViewById(R.id.ae_playerNameText)
        name.text = ticket.fullName
        val expAmount: EditText = dialogView.findViewById(R.id.ae_expAmount)
        expAmount.requestFocus()

        dialogView.findViewById<Button>(R.id.ae_acceptButton).setOnClickListener {
            val number = expAmount.text.toString()
            if (number != "") {
                ticket.expPersonal += number.toInt()

                // Update database
                val parcel = DBF.createTicketMap(selectedTicket)
                DBF.apiCallPost("https://talltales.nu/API/api/update-ticket.php", parcel)

                alertDialog.dismiss()
            }
        }
        dialogView.findViewById<Button>(R.id.ae_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    fun autoSetRoleAmounts() {
        if (allTickets.isEmpty()) {
            Log.i("test", "allTickets is empty")
            return
        }
        healerAmount = allTickets.size / 16
        mageAmount = (allTickets.size + 4) / 16
        rogueAmount = (allTickets.size + 12) / 16
        knightAmount = (allTickets.size + 8) / 16
        binding.healerAmountValue.setText(healerAmount.toString())
        binding.mageAmountValue.setText(mageAmount.toString())
        binding.rogueAmountValue.setText(rogueAmount.toString())
        binding.knightAmountValue.setText(knightAmount.toString())
    }

    fun selectTicket(ticket: Ticket) {
        if (firstPlayerSelected) {
            deselectPlayer()
        }
        firstPlayerSelected = true
        ticket.selected = true
        selectedTicket = ticket
    }

    private fun switchTeam() {
        // Update locally
        selectedTicket.teamColor = "None"
        updateTicketLists()
        deselectPlayer()

        // Update database
        val parcel = DBF.createTicketMap(selectedTicket)
        DBF.apiCallPost("https://talltales.nu/API/api/update-ticket.php", parcel)
    }

    private fun endEvent() {
        // Find eventId
        // Go to report screen
        var team = 0;

        val dialogView = LayoutInflater.from(context).inflate(R.layout.report_popup, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()

        // Set texts
        val title: TextView = dialogView.findViewById(R.id.rp_titleText)
        title.text = event.title

        val dateText: TextView = dialogView.findViewById(R.id.rp_dateText)
        dateText.text = event.actualDate

        val timeText: TextView = dialogView.findViewById(R.id.rp_timeText)
        timeText.text = event.actualStartTime

        val venueText: TextView = dialogView.findViewById(R.id.rp_venueText)
        venueText.text = event.venue

        val attendanceValue: EditText = dialogView.findViewById(R.id.rp_attendanceValue)
        attendanceValue.setText(event.ExpAttendanceValue.toString())

        val recruitValue: EditText = dialogView.findViewById(R.id.rp_recruitValue)
        recruitValue.setText(event.ExpRecruitValue.toString())

        //val winningValue: EditText = dialogView.findViewById(R.id.rp_winningValue)
        //winningValue.setText(event.ExpWinValue.toString())

        val winningTeam: Button = dialogView.findViewById(R.id.rp_teamButton)
        winningTeam.setOnClickListener {
            if (team == 0 || team == 2) {
                winningTeam.setBackgroundColor(requireContext().resources.getColor(R.color.teamBlueColor))
                team = 1
            } else if (team == 1) {
                winningTeam.setBackgroundColor(requireContext().resources.getColor(R.color.teamRedColor))
                team = 2
            }
        }

        dialogView.findViewById<Button>(R.id.rp_exitButton).setOnClickListener {
            event.ExpAttendanceValue = attendanceValue.text.toString().toInt()
            event.ExpRecruitValue = recruitValue.text.toString().toInt()
            //event.ExpWinningValue = winningValue.text.toString().toInt()

            alertDialog.dismiss()
            findNavController().navigate(EventViewDirections.actionEventViewToEventList(currEventId))
        }

        dialogView.findViewById<Button>(R.id.rp_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    fun setGroupName(ticket: Ticket) {
        // Open popup

        val dialogView = LayoutInflater.from(context).inflate(R.layout.group_popup, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()
        val name: TextView = dialogView.findViewById(R.id.groupPopup_nameText)
        name.text = ticket.fullName

        val editText = dialogView.findViewById<EditText>(R.id.groupPopup_EditName)
        if (ticket.group == "") {
            editText.setText("Group", TextView.BufferType.EDITABLE)
        }
        editText.setText(ticket.group, TextView.BufferType.EDITABLE)
        editText.requestFocus()

        dialogView.findViewById<Button>(R.id.groupPopup_acceptButton).setOnClickListener {
            val groupName = editText.text.toString().lowercase()
            if (groupName != "") {
                ticket.group = groupName
                updateTicketLists()
                alertDialog.dismiss()
            }
        }
        dialogView.findViewById<Button>(R.id.groupPopup_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    fun editNote(ticket: Ticket) {
        // Open popup

        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_note, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()
        val name: TextView = dialogView.findViewById(R.id.an_nameText)
        name.text = ticket.fullName

        val editText = dialogView.findViewById<EditText>(R.id.an_editNoteText)
        editText.setText(ticket.note, TextView.BufferType.EDITABLE)
        editText.requestFocus()

        dialogView.findViewById<Button>(R.id.an_acceptButton).setOnClickListener {
            // Update locally
            val newNote = editText.text.toString().lowercase()
            ticket.note = newNote
            updateTicketLists()

            // Update database
            val parcel = DBF.createTicketMap(ticket)
            DBF.apiCallPost("https://talltales.nu/API/api/update-ticket.php", parcel)

            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.an_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    fun manualPlayerLink(ticket: Ticket) {
        var playerId = ""
        var playerList = mutableListOf<Player>()

        val dialogView = LayoutInflater.from(context).inflate(R.layout.manual_player_link, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()

        val playerLayout: LinearLayout = dialogView.findViewById(R.id.mpl_playerLayout)

        val name: TextView = dialogView.findViewById(R.id.mpl_ticketNameText)
        name.text = ticket.fullName

        val age: TextView = dialogView.findViewById(R.id.mpl_ageText)
        age.text = ticket.age.toString()

        val bookerName: TextView = dialogView.findViewById(R.id.mpl_bookerNameText)
        bookerName.text = ticket.bookerFullName

        val bookerEmail: TextView = dialogView.findViewById(R.id.mpl_bookerEmailText)
        bookerEmail.text = ticket.bookerEmail

        val guardianName: TextView = dialogView.findViewById(R.id.mpl_guardianNameText)
        guardianName.text = ticket.guardianName

        val guardianPhone: TextView = dialogView.findViewById(R.id.mpl_guardianPhoneText)
        guardianPhone.text = ticket.guardianPhoneNr

        val noEntriesText = dialogView.findViewById<TextView>(R.id.mpl_noEntriesText)

        val acceptButton = dialogView.findViewById<TextView>(R.id.mpl_acceptButton)

        val cancelButton = dialogView.findViewById<TextView>(R.id.mpl_cancelButton)

        val radioGroup = RadioGroup(context)
        radioGroup.orientation = LinearLayout.VERTICAL
        radioGroup.layoutDirection = View.LAYOUT_DIRECTION_RTL

        var layoutParams: RadioGroup.LayoutParams

        for (player in playerList) {
            val radioButton = RadioButton(context)
            radioButton.text = player.fullName + ", " + player.age + "år"
            radioButton.setPadding(20, 0, 0, 0)

            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams.setMargins(0, 20, 0, 0)
            radioGroup.addView(radioButton, layoutParams)
        }

        playerLayout.addView(radioGroup)

        if (playerList.size == 0) {
            noEntriesText.visibility = View.VISIBLE
        } else {
            noEntriesText.visibility = View.GONE
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<View>(checkedId) as RadioButton
            val checkedRadioButtonId = radioGroup.indexOfChild(checkedRadioButton)
            val checkedRadioButtonText = checkedRadioButton.text.toString()
        }

        cancelButton.setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        acceptButton.setOnClickListener {
            ticket.playerId = playerId
            updateTicketLists()
            alertDialog.dismiss()
        }
    }

// SORTING FUNCTIONS

    private fun sortAssignByUserId() {
        assignList.sortBy { it.ticketId }
    }

    private fun sortAssignByName() {
        assignList.sortBy { it.fullName }
    }

    private fun sortAssignByAge() {
        assignList.sortBy { it.age }
    }

    private fun sortAssignByGroup() {
        if (assignList.size == 0) {
            return
        }

        assignList = assignList.sortedWith(
            compareBy(
                Ticket::groupSize,
                Ticket::group,
                Ticket::fullName
            ).reversed()
        ) as MutableList<Ticket>
    }

    private fun sortCheckInByName() {
        checkInList.sortBy { it.fullName }
    }

    private fun sortCheckInByAge() {
        checkInList.sortBy { it.age }
    }

    private fun sortCheckInByNote() {
        checkInList.sortBy { it.note }
    }

    private fun sortCheckInByColor() {
        checkInList.sortBy { it.teamColor }
    }

    private fun sortTeamsByName() {
        blueTeam.sortBy { it.fullName }
        blueBench.sortBy { it.fullName }
        redTeam.sortBy { it.fullName }
        redBench.sortBy { it.fullName }
    }

    private fun sortTeamsByNumber() {
        blueTeam.sortBy { it.tabardNr }
        blueBench.sortBy { it.tabardNr }
        redTeam.sortBy { it.tabardNr }
        redBench.sortBy { it.tabardNr }
    }

    private fun sortTeamsByRole() {
        blueTeam.sortBy { it.currentRole }
        blueBench.sortBy { it.currentRole }
        redTeam.sortBy { it.currentRole }
        redBench.sortBy { it.currentRole }
    }

    // PICK ROLES

    private fun randomizeRoles() {
        val redSuccess = pickTeamRoles(redTeam)
        val blueSuccess = pickTeamRoles(blueTeam)

        if (redSuccess) {
            Toast.makeText(context, "Red team randomized successfully!", Toast.LENGTH_SHORT).show()
        }
        if (blueSuccess) {
            Toast.makeText(context, "Blue team randomized successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickTeamRoles(team: MutableList<Ticket>): Boolean {
        // Get the amount of special roles
        healerAmount = binding.healerAmountValue.text.toString().toInt()
        rogueAmount = binding.rogueAmountValue.text.toString().toInt()
        mageAmount = binding.mageAmountValue.text.toString().toInt()
        knightAmount = binding.knightAmountValue.text.toString().toInt()
        specialAAmount = binding.specialAAmountValue.text.toString().toInt()
        specialBAmount = binding.specialBAmountValue.text.toString().toInt()

        // Create lists to be filled in loop
        var finishedHealers = mutableListOf<Ticket>()
        var finishedrogue = mutableListOf<Ticket>()
        var finishedmage = mutableListOf<Ticket>()
        var finishedknight = mutableListOf<Ticket>()
        var finishedspecialA = mutableListOf<Ticket>()
        var finishedspecialB = mutableListOf<Ticket>()

        // Set everybody as warrior
        for (ticket in team) {
            ticket.currentRole = 7
        }

        // A function to set correct ticket in correct list
        fun setRole(ticket: Ticket, role: Int) {
            when (role) {
                1 -> {
                    finishedHealers.add(ticket)
                    healerAmount--
                }
                2 -> {
                    finishedrogue.add(ticket)
                    rogueAmount--
                }
                3 -> {
                    finishedmage.add(ticket)
                    mageAmount--
                }
                4 -> {
                    finishedknight.add(ticket)
                    knightAmount--
                }
                5 -> {
                    finishedspecialA.add(ticket)
                    specialAAmount--
                }
                6 -> {
                    finishedspecialB.add(ticket)
                    specialBAmount--
                }
            }
        }

        // Find how many special roles should be assigned - for comparison later
        val totalAmount =
            healerAmount + rogueAmount + mageAmount + knightAmount + specialAAmount + specialBAmount

        // Get the next players in line to be special
        team.sortBy { it.roundsSpecialRole }

        val secondList = team.slice(0..totalAmount).toList()

        // Set the rest as warriors

        // If any of the players have a guaranteed role, set it as the role and remove the person from the players list.
        val thirdList = mutableListOf<Ticket>()
        for (ticket in secondList) {
            if (ticket.guaranteedRole != 0) {
                setRole(ticket, ticket.guaranteedRole)
            } else {
                thirdList.add(ticket)
            }
        }
        // Repeat
        var tempTotal = 0
        var loops = 0

        while (tempTotal != totalAmount) {
            // Create and reset lists
            val tempHealers = mutableListOf<Ticket>()
            val temprogue = mutableListOf<Ticket>()
            val tempmage = mutableListOf<Ticket>()
            val tempknight = mutableListOf<Ticket>()
            val tempspecialA = mutableListOf<Ticket>()
            val tempspecialB = mutableListOf<Ticket>()

            finishedHealers = mutableListOf()
            finishedrogue = mutableListOf()
            finishedmage = mutableListOf()
            finishedknight = mutableListOf()
            finishedspecialA = mutableListOf()
            finishedspecialB = mutableListOf()

            // Put each player in the role lists they're allowed to be
            for (ticket in thirdList) {
                if (ticket.roundsHealer < ticket.allowedTimesPerRole) {
                    tempHealers.add(ticket)
                }
                if (ticket.roundsRogue < ticket.allowedTimesPerRole) {
                    temprogue.add(ticket)
                }
                if (ticket.roundsMage < ticket.allowedTimesPerRole) {
                    tempmage.add(ticket)
                }
                if (ticket.roundsKnight < ticket.allowedTimesPerRole) {
                    tempknight.add(ticket)
                }
                if (ticket.roundsSpecial < ticket.allowedTimesPerRole) {
                    tempspecialA.add(ticket)
                }
                if (ticket.roundsSpecial < ticket.allowedTimesPerRole) {
                    tempspecialB.add(ticket)
                }
            }

            // Shuffle each rolelist
            tempHealers.shuffle()
            temprogue.shuffle()
            tempmage.shuffle()
            tempknight.shuffle()
            tempspecialA.shuffle()
            tempspecialB.shuffle()

            // Pick out players who have not already been picked
            val pickedPlayerList = mutableListOf<Ticket>()

            for (ticket in tempHealers) {
                if (finishedHealers.size < healerAmount && !pickedPlayerList.contains(ticket)) {
                    pickedPlayerList.add(ticket)
                    finishedHealers.add(ticket)
                }
            }

            for (ticket in temprogue) {
                if (finishedrogue.size < rogueAmount && !pickedPlayerList.contains(ticket)) {
                    pickedPlayerList.add(ticket)
                    finishedrogue.add(ticket)
                }
            }

            for (ticket in tempmage) {
                if (finishedmage.size < mageAmount && !pickedPlayerList.contains(ticket)) {
                    pickedPlayerList.add(ticket)
                    finishedmage.add(ticket)
                }
            }

            for (ticket in tempknight) {
                if (finishedknight.size < knightAmount && !pickedPlayerList.contains(ticket)) {
                    pickedPlayerList.add(ticket)
                    finishedknight.add(ticket)
                }
            }

            for (ticket in tempspecialA) {
                if (finishedspecialA.size < specialAAmount && !pickedPlayerList.contains(ticket)) {
                    pickedPlayerList.add(ticket)
                    finishedspecialA.add(ticket)
                }
            }

            for (ticket in tempspecialB) {
                if (finishedspecialB.size < specialBAmount && !pickedPlayerList.contains(ticket)) {
                    pickedPlayerList.add(ticket)
                    finishedspecialB.add(ticket)
                }
            }

            // Check if the correct amount of roles have been picked, otherwise rinse & repeat.
            tempTotal =
                finishedHealers.size + finishedrogue.size + finishedmage.size + finishedknight.size + finishedspecialA.size + finishedspecialB.size
            loops++

            if (loops > 99) {
                Toast.makeText(
                    context,
                    "Randomizer looped 100 times without finding a match!",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        // Set role to player's currRole
        for (ticket in finishedHealers) {
            ticket.currentRole = 1
        }
        for (ticket in finishedrogue) {
            ticket.currentRole = 2
        }
        for (ticket in finishedmage) {
            ticket.currentRole = 3
        }
        for (ticket in finishedknight) {
            ticket.currentRole = 4
        }
        for (ticket in finishedspecialA) {
            ticket.currentRole = 5
        }
        for (ticket in finishedspecialB) {
            ticket.currentRole = 6
        }

        // If a player has been all special roles an equal amount, increase the amount of times they can be special
        for (ticket in team) {
            if (ticket.roundsHealer == ticket.roundsKnight && ticket.roundsHealer == ticket.roundsMage && ticket.roundsHealer == ticket.roundsRogue) {
                ticket.allowedTimesPerRole++
            }
            if (ticket.currentRole == ticket.guaranteedRole) {
                ticket.guaranteedRole = 0
            }
            if (ticket.currentRole != 7) {
                ticket.roundsSpecialRole++
            }
        }

        // Update team lists and return to play
        teamSorting = 2
        updateTicketLists()
        bottomPanel.visibility = View.VISIBLE
        bottomPanelNewRound.visibility = View.GONE
        return true
    }
}