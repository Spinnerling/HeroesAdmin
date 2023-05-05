package com.example.heroadmin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlin.math.min
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heroadmin.databinding.FragmentEventViewBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import org.json.JSONObject
import kotlin.math.abs
import kotlinx.serialization.json.Json

class EventView : Fragment() {
    private lateinit var currActivity: MainActivity
    private lateinit var binding: FragmentEventViewBinding
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
    private lateinit var args: EventViewArgs
    private lateinit var currEventId: String
    private lateinit var event: Event
    val ticketDatabase = LocalDatabase(Ticket.serializer())
    val playerDatabase = LocalDatabase(Player.serializer())
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
    lateinit var selectedTicket: Ticket
    private lateinit var selectedPlayer: Player
    lateinit var selectedTicketTVH: TeamViewHolder
    private lateinit var bottomPanel: LinearLayout
    private lateinit var bottomPanelPlayer: LinearLayout
    private lateinit var bottomPanelNewRound: LinearLayout
    private lateinit var playerRoleButtonPanel: LinearLayout
    private lateinit var loadingDialogue: AlertDialog
    private lateinit var notification: AlertDialog
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
        loadTestData()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        // Find elements
        bottomPanel = binding.bottomPanel
        bottomPanelNewRound = binding.bottomPanelNewRound
        bottomPanelPlayer = binding.bottomPanelPlayer
        playerRoleButtonPanel = binding.playerRoleButtonPanel

        loadingPopup()
        getAllTickets(event)

        // Set variables
        binding.dateText.text = "Date: ${event.actualDate}"
        binding.timeText.text = "Start: ${event.actualStartTime}"
        binding.venueText.text = "Venue: ${event.venue}"
        binding.playerAmountText.text = "Tickets: ${allTickets.size} / ${event.playerMax}"
        binding.roundText.text = event.round.toString()

        binding.scrollingPanel.setOnTouchListener { _, _ ->
            layoutFunction()
            Log.i("check", "pressing scrollingPanel")
            // Keep
            true
        }

        binding.assignTeamPanelButton.setOnClickListener {
            if (binding.assignTeamList.layoutParams.height == 0) {
                binding.assignTeamList.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                binding.assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_open,
                    0,
                    0,
                    0
                )
            } else {
                binding.assignTeamList.layoutParams.height = 0
                binding.assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_closed,
                    0,
                    0,
                    0
                )
            }
        }

        binding.checkInPanelButton.setOnClickListener {
            if (binding.checkInList.layoutParams.height == 0) {
                binding.checkInList.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                // Set button arrow icon
                binding.checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_open,
                    0,
                    0,
                    0
                )
            } else {
                binding.checkInList.layoutParams.height = 0

                // Set button arrow icon
                binding.checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_list_closed,
                    0,
                    0,
                    0
                )
            }
        }

        binding.playerOnOffSwitch.setOnClickListener {
            benchTicket()
        }

        binding.playerOnOffSwitch2.setOnClickListener {
            benchTicket()
        }

        binding.playerCloseButton.setOnClickListener {
            deselectPlayer()
        }

        binding.newRoundButton.setOnClickListener {
            deselectPlayer()
            bottomPanel.visibility = View.GONE
            bottomPanelNewRound.visibility = View.VISIBLE
            checkTeamSizes()
        }

        binding.cancelNewRoundButton.setOnClickListener {
            deselectPlayer()
            bottomPanel.visibility = View.VISIBLE
            bottomPanelNewRound.visibility = View.GONE
            playerRoleButtonPanel.visibility = View.INVISIBLE
            dismissKeyboard()
        }

        binding.switchTeamButton.setOnClickListener {
            switchTeam()
        }

        binding.switchTeamButton2.setOnClickListener {
            switchTeam()
        }

        binding.spendExpButton.setOnClickListener {
            findNavController().navigate(
                EventViewDirections.actionEventViewToLevelUpFragment(
                    selectedTicket.playerId ?: ""
                )
            )
        }

        binding.ticketInfoButton.setOnClickListener {
            openTicketInfo()
        }

        binding.rollRoundButton2.setOnClickListener {
            randomizeRoles()
            dismissKeyboard()
            deselectPlayer()
            if (healerAmount + rogueAmount + knightAmount + mageAmount + specialAAmount + specialBAmount > blueTeam.size || healerAmount + rogueAmount + knightAmount + mageAmount + specialAAmount + specialBAmount > redTeam.size) {
                callNotification("There are more roles than players!\nEdit your role amounts.")
            }
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
            autoSetRoleAmounts()
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
        binding.teamRoleButton1.setOnClickListener {
            teamSorting = 2
            updateTicketLists()
        }

        binding.teamNameButton2.setOnClickListener {
            teamSorting = 0
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
        event = Json.decodeFromString<Event>(response.toString())
        val jsonArray = response.getJSONArray("data").getJSONObject(0).getJSONArray("TicketIDs")
        val list = MutableList(jsonArray.length()) {
            jsonArray.getString(it)
        }
        event.ticketIDs.addAll(list)

        getAllTickets(event)
    }

    private fun getAllTickets(event: Event) {
//        // Get the event's ticket ids safely
//        event.ticketIDs.let { allTicketIds ->
//            // Create an array of the players connected to the tickets
//            allTickets = mutableListOf()
//
//            CoroutineScope(Dispatchers.IO).launch {
//                val ticketJobs = allTicketIds.map { ticketId ->
//                    async {
//                        val result = CompletableDeferred<Ticket?>()
//                        getTicket(ticketId) { ticket ->
//                            result.complete(ticket)
//                        }
//                        result.await()
//                    }
//                }
//
//                val fetchedTickets = ticketJobs.awaitAll().filterNotNull()
//                allTickets.clear()
//                allTickets.addAll(fetchedTickets)
//
//                // Update UI with the new ticket list
//                withContext(Dispatchers.Main) {
//                    updateTicketLists()
//                    DBF.getTicketBookers(allTickets)
//                    loadingDialogue.dismiss()
//                    binding.refreshButton.isEnabled = true
//                }
//            }
//        } ?: run {
//            // Handle the case when event.tickets is null
//            // e.g., show an error message or set allTickets to an empty list
//            allTickets = mutableListOf()
//        }


        allTickets = ticketDatabase.getAll() // Temporary code while no APIs exists
        allTickets.forEach { automaticPlayerLink(it) }
        event.ticketIDs = allTickets.map { it.ticketId ?: "" }.toMutableList() // Temporary code while no APIs exists
        updateTicketLists()
        DBF.getTicketBookers(allTickets)
        loadingDialogue.dismiss()
        binding.refreshButton.isEnabled = true
    }

    private fun automaticPlayerLink(ticket: Ticket) {

    }

    private fun getTicket(ticketId: String, onComplete: (Ticket?) -> Unit) {
        // Find ticket in database by ticketId, return an array of its contents
        DBF.apiCallGet(
            "https://talltales.nu/API/api/ticket.php?id=$ticketId",
            { response ->
                val ticket = parseTicket(response)
                onComplete(ticket)
            },
            {
                // Handle error case here
                onComplete(null)
            }
        )
    }

    private fun parseTicket2(response: JSONObject) {
        // Deserialize the JSONObject into a Ticket object
        val ticket = Json.decodeFromString<Ticket>(response.toString())

        allTickets.add(ticket)

        // If this is the last ticket to be parsed, update lists
        if (allTickets.size >= (event.ticketIDs.size ?: 0)) {
            updateTicketLists()
            DBF.getTicketBookers(allTickets)

            loadingDialogue.dismiss()
            binding.refreshButton.isEnabled = true
        }
    }

    private fun parseTicket(response: JSONObject): Ticket {
        // Deserialize the JSONObject into a Ticket object
        return Json.decodeFromString(response.toString())
    }

    private fun deselectPlayer() {
        if (!firstPlayerSelected) {
            return
        }
        if (bottomPanelPlayer.visibility == View.VISIBLE) {
            bottomPanel.visibility = View.VISIBLE
            bottomPanelPlayer.visibility = View.GONE
        }
        if (bottomPanelNewRound.visibility == View.VISIBLE) {
            playerRoleButtonPanel.visibility = View.INVISIBLE
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

            2 -> {
                sortTeamsByRole()
            }
        }
        Log.i("check", "Ticket amount: " + allTickets.size.toString())

        setAssignTeamAdapter()
        setCheckInAdapter()
        setTeamAdapters()
        checkListVisibilities()
        updateTeamPower()
    }

    private fun updateTicketGroups() {
        val emailToGroupMap = mutableMapOf<String, Int>()
        val usedGroupNumbers = mutableSetOf<Int>()
        val groupToSizeMap = mutableMapOf<Int, Int>()

        // First pass: Assign group numbers and calculate group sizes.
        for (ticket in assignList) {
            if (ticket.group == "SELF") {
                continue
            }

            val bookerEmail = ticket.bookerEmail ?: continue
            val groupNumber = emailToGroupMap[bookerEmail] ?: run {
                var newGroupNumber = 1
                while (usedGroupNumbers.contains(newGroupNumber)) {
                    newGroupNumber++
                }
                usedGroupNumbers.add(newGroupNumber)
                newGroupNumber
            }
            emailToGroupMap[bookerEmail] = groupNumber
            ticket.group = groupNumber.toString()

            val groupSize = groupToSizeMap[groupNumber] ?: 0
            groupToSizeMap[groupNumber] = groupSize + 1
        }

        // Second pass: Assign group sizes to tickets.
        for (ticket in assignList) {
            if (ticket.group == "SELF") {
                continue
            }

            val groupNumber = ticket.group.toIntOrNull() ?: continue
            ticket.groupSize = groupToSizeMap[groupNumber] ?: 1
        }

        // Create a map with group identifier as key and group size as value.
        val groupSizeMap = mutableMapOf<String, Int>()
        for (ticket in assignList) {
            if (ticket.group != "SELF" && ticket.group != "") {
                groupSizeMap[ticket.group] = ticket.groupSize
            }
        }

        // Sort the groups by size in descending order.
        val sortedGroupSizeMap = groupSizeMap.entries.sortedByDescending { it.value }

        // Reassign group identifiers based on the sorted order.
        val reassignedGroupMap = mutableMapOf<String, String>()
        var newIdentifier = 1
        for (entry in sortedGroupSizeMap) {
            reassignedGroupMap[entry.key] = newIdentifier.toString()
            newIdentifier++
        }

        // Update the ticket groups with the new identifiers.
        for (ticket in assignList) {
            if (ticket.group != "SELF" && ticket.group != "") {
                ticket.group = reassignedGroupMap[ticket.group] ?: ticket.group
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
                    DBF.updateData(ticket)
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
            ticket.firstName ?: "",
            ticket.lastName ?: "",
            ticket.age ?: 0,
            0, 0, 0, 0,
            1, 1, 1, 1,
            0, 0, 0, 0
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
                if (ticket.age!! > 12) {
                    redTeenAmount++
                } else if (ticket.age < 8) {
                    redTiniesAmount++
                }
                redAmount++

            } else if (ticket.teamColor == "Blue" && ticket.benched == 0) {
                bluePowerLevel += ticket.powerLevel
                if (ticket.age!! > 12) {
                    blueTeenAmount++
                } else if (ticket.age < 8) {
                    blueTiniesAmount++
                }
                blueAmount++
            }
        }

        binding.redTeamPowerText.text = redPowerLevel.toString()
        binding.blueTeamPowerText.text = bluePowerLevel.toString()

        binding.redTeamAmountText.text = redAmount.toString()
        binding.blueTeamAmountText.text = blueAmount.toString()

        binding.redTeamTeensText.text = redTeenAmount.toString()
        binding.blueTeamTeensText.text = blueTeenAmount.toString()

        binding.redTeamTiniesText.text = redTiniesAmount.toString()
        binding.blueTeamTiniesText.text = blueTiniesAmount.toString()
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
        //selectedPlayer = selectedTicket.ticketId?.let { DBF.getPlayer(it) }!!

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

            //binding.playerExpText.text = "${selectedPlayer.totalExp} EXP kvar"
            val roleInText = DBF.getRoleByNumber(selectedTicket.currentRole ?: 0)
            binding.ticketRoleText.text = roleInText

            if (selectedTicket.benched == 0) {
                binding.playerOnOffSwitch.isChecked = true
                binding.playerOnOffSwitch2.isChecked = true
            } else if (selectedTicket.benched == 1) {
                binding.playerOnOffSwitch.isChecked = false
                binding.playerOnOffSwitch2.isChecked = false
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

        dialogView.findViewById<Button>(R.id.checkinAcceptButton).setOnClickListener {
            // Update locally
            ticket.checkedIn = 1
            updateTicketLists()

            // Update database
            DBF.updateData(ticket)

            alertDialog.dismiss()

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
        guardianName.text = ticket.bookerName
        val guardianPhone: TextView = dialogView.findViewById(R.id.ti_guardianPhone)
        guardianPhone.text = ticket.bookerPhoneNr
        val bookerName: TextView = dialogView.findViewById(R.id.ti_bookerName)
        bookerName.text = ticket.bookerName
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
                ticket.expPersonal = ticket.expPersonal?.plus(number.toInt())

                // Update database
                DBF.updateData(selectedTicket)

                alertDialog.dismiss()
            }
        }
        dialogView.findViewById<Button>(R.id.ae_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    fun autoSetRoleAmounts() {
        val redTeamSize = redTeam.size
        val blueTeamSize = blueTeam.size

        // Find the smaller team size
        val smallerTeamSize = min(redTeamSize, blueTeamSize)

        // Update role amounts based on the smaller team size, and role prioritization
        healerAmount = min(smallerTeamSize / 4, 4)
        mageAmount = min((smallerTeamSize + 2) / 4, 4)
        rogueAmount = min((smallerTeamSize + 3) / 4, 4)
        knightAmount = min((smallerTeamSize + 1) / 4, 4)

        // Set the TextView values
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
        if (selectedTicket.benched == 0) {
            binding.playerOnOffSwitch.isChecked = true
            binding.playerOnOffSwitch2.isChecked = true
        } else {
            binding.playerOnOffSwitch.isChecked = false
            binding.playerOnOffSwitch2.isChecked = false
        }
    }

    private fun switchTeam() {
        // Update locally
        if (selectedTicket.teamColor == "Red") selectedTicket.teamColor =
            "Blue" else selectedTicket.teamColor = "Red"
        updateTicketLists()
        deselectPlayer()
        selectedTicketTVH.select()
        autoSetRoleAmounts()

        // Update database
        DBF.updateData(selectedTicket)
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
        editText.setText(ticket.group, TextView.BufferType.EDITABLE)
        editText.requestFocus()

        dialogView.findViewById<Button>(R.id.groupPopup_acceptButton).setOnClickListener {
            val groupName = editText.text.toString().lowercase()
            if (groupName != "") {
                // Check if the input groupName is a valid group number
                if (groupName.toIntOrNull() != null) {
                    ticket.group = groupName
                } else {
                    // Generate a new group number for the person
                    val usedGroupNumbers =
                        assignList.mapNotNull { it.group.toIntOrNull() }.toMutableList()
                    var newGroupNumber = 1
                    while (usedGroupNumbers.contains(newGroupNumber)) {
                        newGroupNumber++
                    }
                    ticket.group = newGroupNumber.toString()
                }
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
            DBF.updateData(ticket)

            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.an_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    fun manualPlayerLink(ticket: Ticket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.manual_player_link, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()

        // Get the references for the TextViews to display ticket information
        val name: TextView = dialogView.findViewById(R.id.mpl_ticketNameText)
        val age: TextView = dialogView.findViewById(R.id.mpl_ageText)
        val bookerName: TextView = dialogView.findViewById(R.id.mpl_bookerNameText)
        val bookerEmail: TextView = dialogView.findViewById(R.id.mpl_bookerEmailText)
        val bookerAddress: TextView = dialogView.findViewById(R.id.mpl_bookerAdressText)
        val bookerPhone: TextView = dialogView.findViewById(R.id.mpl_bookerPhoneText)

        // Populate the TextViews with ticket information
        name.text = ticket.fullName
        age.text = ticket.age.toString()
        bookerName.text = ticket.bookerName
        bookerEmail.text = ticket.bookerEmail
        bookerAddress.text = ticket.bookerAdress
        bookerPhone.text = ticket.bookerPhoneNr

        // Get the reference for the RecyclerView and set its layout manager
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.mpl_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch the players and create an instance of PlayerListItemAdapter
        val players = playerDatabase.getAll() // You need to implement this method to fetch the list of players

        val playerListItems = players.mapNotNull { player ->
            if (player.firstName != null && player.lastName != null && player.age != null) {
                PlayerListItem(
                    firstName = player.firstName!!,
                    lastName = player.lastName!!,
                    age = player.age!!,
                    bookerNames = player.bookerNames,
                    bookerPhones = player.bookerPhones,
                    bookerEmails = player.bookerEmails,
                    bookerAddresses = player.bookerAddresses
                )
            } else null
        }.toMutableList()

        val adapter = PlayerListItemAdapter(playerListItems)
        recyclerView.adapter = adapter

        // Get the reference for the buttons
        val acceptButton = dialogView.findViewById<Button>(R.id.mpl_acceptButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.mpl_cancelButton)

        // Set click listeners for the buttons
        cancelButton.setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        acceptButton.setOnClickListener {
            // Here you can handle what happens when the "Accept" button is clicked
            // For example, you might want to update the ticket with the selected player
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
        if (assignList.isEmpty()) {
            return
        }

        // Sort the list by group and full name
        assignList = assignList.sortedWith(
            compareBy(
                Ticket::group,
                Ticket::fullName
            )
        ) as MutableList<Ticket>

        // Move all solo players (with the "SELF" group) to the end of the list
        val soloPlayers = assignList.filter { it.group == "SELF" || it.group == "" }
        val nonSoloPlayers = assignList.filter { it.group != "SELF" && it.group != "" }
        assignList = (nonSoloPlayers + soloPlayers as MutableList<Ticket>) as MutableList<Ticket>
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

    private fun sortTeamsByRole() {
        blueTeam.sortBy { it.currentRole }
        blueBench.sortBy { it.currentRole }
        redTeam.sortBy { it.currentRole }
        redBench.sortBy { it.currentRole }
    }

    // PICK ROLES
    private fun randomizeRoles() {
        val blueTeamSuccess = pickTeamRoles(blueTeam)
        val redTeamSuccess = pickTeamRoles(redTeam)

        if (blueTeamSuccess && redTeamSuccess) {
            assignRoles(blueTeam)
            assignRoles(redTeam)
            updatePlayerStats(blueTeam)
            updatePlayerStats(redTeam)
            updateRound(true)
        } else if (!blueTeamSuccess && !redTeamSuccess) {
            Toast.makeText(
                context,
                "Failed to generate roles for any team.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            val team = if (!redTeamSuccess) "red" else "blue"

            Toast.makeText(
                context,
                "Failed to generate roles for $team team",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun pickTeamRoles(team: MutableList<Ticket>): Boolean {
        // Get the amount of special roles
        val roleAmounts = arrayOf(
            binding.healerAmountValue.text.toString().toInt(),
            binding.rogueAmountValue.text.toString().toInt(),
            binding.mageAmountValue.text.toString().toInt(),
            binding.knightAmountValue.text.toString().toInt(),
            binding.specialAAmountValue.text.toString().toInt(),
            binding.specialBAmountValue.text.toString().toInt()
        )

        // Find how many special roles should be assigned - for comparison later
        val totalAmount = roleAmounts.sum()

        // Set everybody as warrior
        team.forEach { it.currentRole = 7 }
        blueBench.forEach { it.currentRole = 7 }
        redBench.forEach { it.currentRole = 7 }

        // Handle players with guarantees first
        val guaranteedPlayers = team.filter { it.guaranteedRole!! > 0 && it.guaranteedRole!! <= 6 }
        for (player in guaranteedPlayers) {
            val role = player.guaranteedRole
            if (roleAmounts[role!! - 1] > 0) {
                player.currentRole = role
                roleAmounts[role - 1]--
            }
        }

        // Sort the remaining team members by roundsSpecialRole and take the first totalAmount players
        val remainingPlayers = team.filter { it.currentRole == 7 }
        val prioritizedPlayers = remainingPlayers.sortedBy { it.roundsSpecialRole }
            .take(totalAmount - guaranteedPlayers.size)

        // Assign special roles to the prioritized players
        var roleIndex = 0
        var roleCounter = roleAmounts[roleIndex]

        for (player in prioritizedPlayers) {
            // If roleCounter reaches 0, move to the next role
            while (roleCounter == 0 && roleIndex < roleAmounts.size - 1) {
                roleIndex++
                roleCounter = roleAmounts[roleIndex]
            }

            // Assign the current role and decrement roleCounter
            player.currentRole = roleIndex + 1
            roleCounter--
            roleAmounts[roleIndex] = roleCounter // Update the roleCounter in the roleAmounts array
        }

        // Check if all the desired roles were distributed
        val rolesDistributed = team.count { it.currentRole != 7 }
        if (rolesDistributed != totalAmount) {
            return false
        }

        // Update team lists and return to play
        teamSorting = 2
        updateTicketLists()
        bottomPanel.visibility = View.VISIBLE
        bottomPanelNewRound.visibility = View.GONE
        return true
    }

    private fun assignRoles(team: MutableList<Ticket>) {
        for (player in team) {
            when (player.currentRole) {
                1 -> player.roundsHealer = player.roundsHealer!! + 1
                2 -> player.roundsRogue = player.roundsRogue!! + 1
                3 -> player.roundsMage = player.roundsMage!! + 1
                4 -> player.roundsKnight = player.roundsKnight!! + 1
                5, 6 -> player.roundsSpecial++
            }
        }
    }

    private fun updatePlayerStats(team: MutableList<Ticket>) {
        for (player in team) {
            if (player.currentRole != 7) {
                player.roundsSpecialRole = player.roundsSpecialRole!! + 1
            }

            if (player.currentRole == player.guaranteedRole) {
                player.guaranteedRole = 0
            }
        }
    }

    private fun updateRound(increase: Boolean) {
        if (increase) event.round++ else event.round--
        binding.roundText.text = event.round.toString()
    }

    fun loadTestData() {
        // Generate sample players
        val tickets = listOf(
            Ticket(
                "T1", "John", "Doe", 15, "Jane Doe", "555-123-4567",
                "123 Main St", "Springfield", "john@example.com", null, "", 0, 0, 10, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T2", "Jane", "Doe", 14, "John Doe", "555-987-6543",
                "456 Elm St", "Springfield", "john@example.com", null, "", 0, 0, 8, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T3", "Alice", "Smith", 13, "Bob Smith", "555-456-7890",
                "789 Oak St", "Springfield", "alice@example.com", null, "", 0, 0, 12, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T4", "Bob", "Brown", 12, "Alice Brown", "555-321-0987",
                "321 Birch St", "Springfield", "alice@example.com", null, "", 0, 0, 9, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T5", "Charlie", "Johnson", 11, "Diana Johnson", "555-654-3210",
                "654 Pine St", "Springfield", "alice@example.com", null, "", 0, 0, 6, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T6", "Diana", "Miller", 10, "Charlie Miller", "555-852-1470",
                "852 Maple St", "Springfield", "diana@example.com", null, "", 0, 0, 5, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T7", "Eva", "Taylor", 9, "David Taylor", "555-555-5555",
                "10 Oak St", "Springfield", "eva@example.com", null, "", 0, 0, 11, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T0", "Edward", "Wilson", 8, "Emma Wilson", "555-789-4561",
                "741 Vine St", "Springfield", "edward@example.com", null, "", 0, 0, 7, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T8", "Frank", "Adams", 7, "Frank Adams", "555-123-7890",
                "369 Oak St", "Springfield", "edward@example.com", null, "", 0, 0, 10, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E2"
            ),
            Ticket(
                "T9", "George", "Garcia", 6, "George Garcia", "555-456-1234",
                "852 Chestnut St", "Springfield", "george@example.com", null, "", 0, 0, 9, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E2"
            ),
            Ticket(
                "T10", "Hannah", "Scott", 5, "Henry Scott", "555-789-0123",
                "753 Main St", "Springfield", "hannah@example.com", null, "", 0, 0, 12, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E2"
            ),
            Ticket(
                "T11", "Fohn", "Doe", 15, "Jane Doe", "555-123-4567",
                "123 Main St", "Springfield", "john@example.com", null, "", 0, 0, 10, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T12", "Fane", "Doe", 14, "John Doe", "555-987-6543",
                "456 Elm St", "Springfield", "john@example.com", null, "", 0, 0, 8, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T13", "Flice", "Smith", 13, "Bob Smith", "555-456-7890",
                "789 Oak St", "Springfield", "alice@example.com", null, "", 0, 0, 12, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T14", "Fob", "Brown", 12, "Alice Brown", "555-321-0987",
                "321 Birch St", "Springfield", "alice@example.com", null, "", 0, 0, 9, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T15",
                "Fharlie",
                "Johnson",
                11,
                "Diana Johnson",
                "555-654-3210",
                "654 Pine St",
                "Springfield",
                "alice@example.com",
                "Hon är allergisk mot citrusfrukter",
                "",
                0,
                0,
                6,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                "E1"
            ),
            Ticket(
                "T16", "Fiana", "Miller", 10, "Charlie Miller", "555-852-1470",
                "852 Maple St", "Springfield", "diana@example.com", null, "", 0, 0, 5, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T17",
                "Feva",
                "Taylor",
                9,
                "David Taylor",
                "555-555-5555",
                "10 Oak St",
                "Springfield",
                "eva@example.com",
                "Han är kompis med Frank Adams och vill vara på samma lag som honom",
                "",
                0,
                0,
                11,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                "E1"
            ),
            Ticket(
                "", "Fedward", "Wilson", 8, "Emma Wilson", "555-789-4561",
                "741 Vine St", "Springfield", "edward@example.com", null, "red", 0, 0, 7, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E1"
            ),
            Ticket(
                "T19", "Drank", "Adams", 7, "Frank Adams", "555-123-7890",
                "369 Oak St", "Springfield", "edward@example.com", null, "blue", 1, 0, 10, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E2"
            ),
            Ticket(
                "T20", "Feorge", "Garcia", 6, "George Garcia", "555-456-1234",
                "852 Chestnut St", "Springfield", "george@example.com", null, "", 1, 0, 9, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E2"
            ),
            Ticket(
                "T21", "Fannah", "Scott", 5, "Henry Scott", "555-789-0123",
                "753 Main St", "Springfield", "hannah@example.com", null, "", 0, 0, 12, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E2"
            ),
            Ticket(
                "T25", "Aliviera", "Barnham", 5, "Henry Scott", "555-789-0123",
                "753 Main St", "Springfield", "hannah@example.com", null, "", 0, 0, 12, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, "E2"
            )
        )

        // Insert sample tickets into the ticketDatabase
        tickets.forEach { ticketDatabase.insert(it) }


        val players = listOf(
            Player(
                "12345", "Fane", "Doe", 15, 205, 1000,
                5, 5, 1, 1, 1, 1, 0, 0, 0, 0,
                mutableListOf("susanne", "thorvald"),mutableListOf("susanne@email.com", "thorvald@email.com"),
                mutableListOf("0918239013", "128309312"),mutableListOf("nyckeldalen 3", "spårvagnen 4"),
            ),
            Player(
                "54321", "Jane", "Doe", 15, 205, 1000,
                5, 5, 1, 1, 1, 1, 0, 0, 0, 0,
                mutableListOf("susanne", "thorvald"),mutableListOf("susanne@email.com", "thorvald@email.com"),
                mutableListOf("0918239013", "128309312"),mutableListOf("nyckeldalen 3", "spårvagnen 4"),
            )
        )
        // Insert sample player into the playerDatabase
        players.forEach { playerDatabase.insert(it) }
    }

    fun dismissKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun checkTeamSizes() {
        val diff = abs(redTeam.size - blueTeam.size)
        var largestTeam = ""
        if (redTeam.size > blueTeam.size) {
            largestTeam = "red"
        } else if (blueTeam.size > redTeam.size) {
            largestTeam = "blue"
        }
        if (diff > 2) {
            callNotification("There is a significant size difference to the teams.\nConsider moving someone from $largestTeam team.")
        }
    }

    private fun callNotification(message: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.note_popup, null)

        val builder = AlertDialog.Builder(context).setView(dialogView)

        notification = builder.show()

        val textHolder: TextView = dialogView.findViewById(R.id.notePopupText)
        textHolder.text = message

        dialogView.findViewById<Button>(R.id.notePopupAcceptButton).setOnClickListener {
            notification.dismiss()
        }
    }

    private fun benchTicket() {
        if (selectedTicket.benched == 0) {
            selectedTicket.benched = 1
        } else {
            selectedTicket.benched = 0
        }
        updateTicketLists()
        autoSetRoleAmounts()
    }

    fun layoutFunction() {
        dismissKeyboard()
        deselectPlayer()
    }
}