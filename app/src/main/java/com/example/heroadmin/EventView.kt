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
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import org.json.JSONObject
import kotlin.math.abs
import kotlinx.serialization.json.Json
import org.json.JSONArray
import java.util.UUID

class EventView : Fragment() {
    private lateinit var currActivity: MainActivity
    private lateinit var binding: FragmentEventViewBinding
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
    private lateinit var args: EventViewArgs
    private lateinit var currEventId: String
    private lateinit var event: Event
    val ticketDatabase = LocalDatabaseSingleton.ticketDatabase
    val playerDatabase = LocalDatabaseSingleton.playerDatabase
    val eventDatabase = LocalDatabaseSingleton.eventDatabase
    private var allTickets: MutableList<Ticket> = mutableListOf()
    private var redTeam: MutableList<Ticket> = mutableListOf()
    private var blueTeam: MutableList<Ticket> = mutableListOf()
    private var redBench: MutableList<Ticket> = mutableListOf()
    private var blueBench: MutableList<Ticket> = mutableListOf()
    private var assignList: MutableList<Ticket> = mutableListOf()
    private var checkInList: MutableList<Ticket> = mutableListOf()
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
        getTicketIdsLocal(event)

        // Set variables
        binding.blueWinsValue.text = event.blueGameWins.toString()
        binding.redWinsValue.text = event.redGameWins.toString()
        binding.roundText.text = event.round.toString()

        checkGameEnded()

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
                    selectedTicket.playerId ?: "",
                    event.eventId
                )
            )
        }

        binding.blueWinsPlus.setOnClickListener {
            event.blueGameWins = event.blueGameWins!! + 1
            binding.blueWinsValue.text = event.blueGameWins.toString()
            DBF.updateData(event)
        }
        binding.blueWinsMinus.setOnClickListener {
            if (event.blueGameWins!! > 0) {
                event.blueGameWins = event.blueGameWins!! - 1
                binding.blueWinsValue.text = event.blueGameWins.toString()
                DBF.updateData(event)
            }
        }
        binding.redWinsPlus.setOnClickListener {
            event.redGameWins = event.redGameWins!! + 1
            binding.redWinsValue.text = event.redGameWins.toString()
            DBF.updateData(event)
        }
        binding.redWinsMinus.setOnClickListener {
            if (event.redGameWins!! > 0) {
                event.redGameWins = event.redGameWins!! - 1
                binding.redWinsValue.text = event.redGameWins.toString()
                DBF.updateData(event)
            }
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
                ticketDatabase.update(ticket)
                DBF.updateData(ticket)
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
        binding.deCheckin.setOnClickListener {
            ResetTicket()
        }
        binding.setWinnerButton.setOnClickListener {
            openWinnerPopup()
        }
        binding.refreshButton.setOnClickListener {
            loadingDialogue.show()
            getEventLocal()
            binding.refreshButton.isEnabled = false
        }
        binding.emptyButton.setOnClickListener {
            //Future shenanigans, save for later
        }
        binding.assignTeamAutoAssignButton.setOnClickListener {
            autoAssignLoop()
        }
        binding.eventBackButton.setOnClickListener {
            currActivity.event = event
            findNavController().navigate(
                EventViewDirections.actionEventViewToEventAdminFrag(
                    currEventId
                )
            )
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

    fun getEventLocal() {
        Log.d("check", "getEventLocal started")
        CoroutineScope(Dispatchers.IO).launch {
            val eventId = event.eventId
            val fetchedEvent = eventDatabase.getByPropertyValue({ it.eventId }, eventId)

            withContext(Dispatchers.Main) {
                fetchedEvent?.let {
                    Log.d("check", "Event fetched from local database") // Add this log statement
                    val jsonEvent = JSONObject(Json.encodeToString(Event.serializer(), it))
                    val response = JSONObject().apply {
                        put("data", JSONArray().apply { put(jsonEvent) })
                    }
                    refreshEvent(response)
                } ?: run {
                    Log.e("check", "Event not found in the local database")
                    // Handle the case when the event is not found in the local database
                }
            }
        }
    }

    private fun refreshEvent(response: JSONObject) {
        val json = Json { ignoreUnknownKeys = true }
        Log.d("check", "JSON input: ${response.toString()}")
        val eventData = response.getJSONArray("data").getJSONObject(0)
        event = json.decodeFromString<Event>(eventData.toString())
        val jsonArray = eventData.getJSONArray("TicketIDs")
        val list = MutableList(jsonArray.length()) {
            jsonArray.getString(it)
        }

        event.ticketIDs.clear() // Clear the existing ticket IDs
        event.ticketIDs.addAll(list)

        fetchEventTickets(event.eventId)
    }

    private fun fetchEventTickets(eventId: String) {
        Log.i("tickets", "Started fetchEventTickets")
        CoroutineScope(Dispatchers.IO).launch {
            val ticketIds = getTicketIds(eventId)  // Fetch the ticket IDs
            Log.i("tickets", "Ticket Ids: $ticketIds")  // Log the fetched ticket IDs

            val tickets = getTickets(ticketIds ?: emptyList())  // Fetch the full Ticket objects
            Log.i("tickets", "Fetched Tickets: $tickets")  // Log the fetched tickets

            // Switch to the Main dispatcher for UI updates
            withContext(Dispatchers.Main) {
                // Update the shared 'allTickets' list
                allTickets.clear()
                allTickets.addAll(tickets)

                Log.i("tickets", "allTickets size: ${allTickets.size}")  // Log the size of 'allTickets'

                initializeTicketGroups()
                updateTicketLists()
                DBF.getTicketBookers(allTickets)
                loadingDialogue.dismiss()
                binding.refreshButton.isEnabled = true
            }
        }
    }

    // Function to get ticket IDs for a specific event
    private suspend fun getTicketIds(eventId: String): List<String>? {
        // Define a MutableSharedFlow to collect ticket IDs from API responses
        val ticketIdsFlow = MutableSharedFlow<String>()

        // Find tickets in database by eventId, collect their IDs
        DBF.apiCallGet(
            "https://talltales.nu/API/api/tickets.php?event=$eventId",
            { response ->
                // Assuming your response contains a list of tickets, each with an 'id' field
                val ticketsJsonArray = response.getJSONArray("data")
                for (i in 0 until ticketsJsonArray.length()) {
                    val ticketJson = ticketsJsonArray.getJSONObject(i)
                    val ticketId = ticketJson.getString("ticketId")
                    ticketIdsFlow.tryEmit(ticketId)  // Emit the ticket ID to the flow
                }
            },
            {
                // Handle error case here
                // You may want to emit a special value or close the flow
            }
        )

        // Collect ticket IDs from the flow into a list
        return ticketIdsFlow.toList()
    }

    // Function to get full Ticket objects based on a list of ticket IDs
    private suspend fun getTickets(ticketIds: List<String>): List<Ticket> {
        return coroutineScope {
            ticketIds.map { ticketId ->
                async {
                    // Use the existing getTicket function to fetch each Ticket
                    // Wrapping in a runCatching block to handle exceptions and avoid failing the whole job
                    runCatching {
                        var ticket: Ticket? = null
                        getTicket(ticketId) { fetchedTicket ->
                            ticket = fetchedTicket
                        }
                        ticket
                    }.getOrNull()  // getOrNull will return null if an exception was thrown
                }
            }.awaitAll()
                .filterNotNull()  // awaitAll waits for all async jobs to complete, filterNotNull removes any null Tickets
        }
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

    private fun getTicketIdsLocal(event: Event) {
        event.ticketIDs.let { allTicketIds ->

            allTickets = mutableListOf()

            CoroutineScope(Dispatchers.IO).launch {
                Log.d("check", "All ticket IDs: $allTicketIds")

                val ticketJobs = allTicketIds.map { ticketId ->
                    async {
                        val result = CompletableDeferred<Ticket?>()
                        getTicketLocal(ticketId) { ticket ->
                            result.complete(ticket)
                        }
                        result.await()
                    }
                }

                val fetchedTickets = ticketJobs.awaitAll().filterNotNull()
                Log.d("check", "Fetched tickets: ${fetchedTickets.size}")

                allTickets.clear()
                allTickets.addAll(fetchedTickets)

                // Automatically link Tickets to Players
                val automaticPlayerLinkJobs = allTickets.map { ticket ->
                    async { automaticPlayerLink(ticket) }
                }

                Log.d("check", "Before automaticPlayerLinkJobs.awaitAll()")
                val linkedTickets = automaticPlayerLinkJobs.awaitAll()
                Log.d("check", "After automaticPlayerLinkJobs.awaitAll()")
                Log.d("check", "Linked tickets: ${linkedTickets.size}")
                allTickets.clear()
                allTickets.addAll(linkedTickets)
                withContext(Dispatchers.Main) {
                    Log.i("check", "Is run!")
                    updateTicketLists()
                    initializeTicketGroups()
                    updateTicketLists()
                    DBF.getTicketBookers(allTickets)
                    binding.refreshButton.isEnabled = true
                    Log.d("check", "Dismissing loading dialogue...")
                    loadingDialogue.dismiss()
                }
            }
        } ?: run {
            allTickets = mutableListOf()
        }
    }

    fun getTicketLocal(ticketId: String, callback: (Ticket?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("check", "Getting ticket with ID: $ticketId") // Add this log statement
            val ticket = ticketDatabase.getByPropertyValue({ it.ticketId }, ticketId)

            withContext(Dispatchers.Main) {
                callback(ticket)
            }
        }
    }

    private suspend fun automaticPlayerLink(ticket: Ticket): Ticket {
        return if (ticket.playerId == "" || ticket.playerId == null) {

            when (val result = DBF.matchTicketToPlayerLocal(ticket, playerDatabase)) {
                is DatabaseFunctions.MatchResult.DefiniteMatch -> {
                    val updatedTicket = ticket.copy(playerId = result.playerId)
                    ticketDatabase.update(updatedTicket)
                    Log.i("check", "{${ticket.fullName} found a definite match")
                    updatedTicket
                }

                is DatabaseFunctions.MatchResult.Suggestions -> {
                    val updatedTicket = ticket.copy(suggestions = result.suggestions)
                    ticketDatabase.update(updatedTicket)
                    val amount = result.suggestions.size
                    Log.i("suggestions", "{${ticket.fullName} found {$amount} suggestions")
                    updatedTicket
                }

                is DatabaseFunctions.MatchResult.NoMatch -> {
                    val newPlayer: Player = createNewPlayer(ticket)
                    playerDatabase.insert(newPlayer)
                    val updatedTicket = ticket.copy(playerId = newPlayer.playerId)
                    ticketDatabase.update(updatedTicket)
                    DBF.updateData(ticket)
                    Log.i("check", "{${ticket.fullName} found nothing. Should create player")
                    updatedTicket
                }

                else -> {
                    Log.i("check", "{${ticket.fullName} had an error")
                    ticket
                }
            }
        } else {
            ticket
        }
    }

    fun manualPlayerLink(ticket: Ticket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.manual_player_link, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()

        // Get the references for the TextViews to display ticket information
        val firstName: TextView = dialogView.findViewById(R.id.mpl_ticketFirstNameText)
        val lastName: TextView = dialogView.findViewById(R.id.mpl_ticketLastNameText)
        val age: TextView = dialogView.findViewById(R.id.mpl_ageText)
        val bookerName: TextView = dialogView.findViewById(R.id.mpl_bookerNameText)
        val bookerEmail: TextView = dialogView.findViewById(R.id.mpl_bookerEmailText)
        val bookerAddress: TextView = dialogView.findViewById(R.id.mpl_bookerAdressText)
        val bookerPhone: TextView = dialogView.findViewById(R.id.mpl_bookerPhoneText)

        // Populate the TextViews with ticket information
        firstName.text = ticket.firstName
        lastName.text = ticket.lastName
        age.text = ticket.age.toString()
        bookerName.text = ticket.bookerName
        bookerEmail.text = ticket.bookerEmail
        bookerAddress.text = ticket.bookerAddress
        bookerPhone.text = ticket.bookerPhone

        // Get the reference for the RecyclerView and set its layout manager
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.mpl_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch the players and create an instance of PlayerListItemAdapter
        val playerListItems = ticket.suggestions?.mapNotNull { player ->
            if (player.firstName != null && player.lastName != null && player.age != null) {
                PlayerListItem(
                    playerId = player.playerId,
                    firstName = player.firstName!!,
                    lastName = player.lastName!!,
                    age = player.age!!,
                    bookerNames = player.bookerNames,
                    bookerPhones = player.bookerPhones,
                    bookerEmails = player.bookerEmails,
                    bookerAddresses = player.bookerAddresses
                )
            } else null
        }?.toMutableList()

// Declare selectedItem variable here
        var selectedItem: PlayerListItem? = null

        val adapter = PlayerListItemAdapter(
            playerListItems?.let { it } ?: mutableListOf(), // Use the null-safe operator here
            object : PlayerListItemAdapter.OnItemClickListener {
                override fun onItemClick(
                    position: Int,
                    adapter: PlayerListItemAdapter,
                    playerListItem: PlayerListItem
                ) {
                    // ... onItemClick code ...
                    adapter.toggleSelection(position)
                    selectedItem = if (adapter.selectedPosition != -1) playerListItem else null

                    // Get the reference for the acceptButton
                    val acceptButton: Button = dialogView.findViewById(R.id.mpl_acceptButton)

                    // Change the color of the acceptButton based on the selected item count
                    if (adapter.selectedPosition != -1) {
                        acceptButton.setBackgroundColor(
                            ContextCompat.getColor(
                                context!!,
                                R.color.buttonGreen
                            )
                        ) // Change to the desired color for the selected state
                    } else {
                        acceptButton.setBackgroundColor(
                            ContextCompat.getColor(
                                context!!,
                                R.color.colorUnselected
                            )
                        ) // Change to the desired color for the unselected state
                    }
                }
            }
        )

        recyclerView.adapter = adapter
        // Get the reference for the buttons
        val acceptButton = dialogView.findViewById<Button>(R.id.mpl_acceptButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.mpl_cancelButton)

        // Set click listeners for the buttons
        cancelButton.setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        // Set the click listener for the accept button
        acceptButton.setOnClickListener {
            val currentSelectedItem = selectedItem
            if (currentSelectedItem != null) {
                // Update the playerId of the ticket
                ticket.playerId = currentSelectedItem.playerId

                // Save the updated ticket
                DBF.updateData(ticket)
                ticketDatabase.update(ticket)
                updateTicketLists()

                // Close the alertDialog
                alertDialog.dismiss()
            } else {
                // Show a message that no player is selected, or handle the case where a new player should be created
            }
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
            when {
                ticket.teamColor == "None" || ticket.teamColor == "" -> {
                    assignList.add(ticket)
                }

                ticket.checkedIn == 0 -> {
                    checkInList.add(ticket)
                }

                ticket.teamColor == "Red" -> {
                    if (ticket.benched == 1) redBench.add(ticket) else redTeam.add(ticket)
                }

                ticket.teamColor == "Blue" -> {
                    if (ticket.benched == 1) blueBench.add(ticket) else blueTeam.add(ticket)
                }
            }
        }

        when (assignSorting) {
            0 -> sortAssignByName()
            1 -> sortAssignByAge()
            2 -> sortAssignByUserId()
            3 -> sortAssignByGroup()
        }

        when (checkInSorting) {
            0 -> sortCheckInByName()
            1 -> sortCheckInByAge()
            2 -> sortCheckInByNote()
            3 -> sortCheckInByColor()
        }

        when (teamSorting) {
            0 -> sortTeamsByName()
            2 -> sortTeamsByRole()
        }

        setAssignTeamAdapter()
        setCheckInAdapter()
        setTeamAdapters()
        checkListVisibilities()
        updateTeamPower()
    }

    fun initializeTicketGroups() {
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

        // Second pass: Assign group sizes to tickets now that groups are made.
        for (ticket in assignList) {
            if (ticket.group == "SELF") {
                ticket.groupSize = 1
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
            DBF.updateData(ticket)
        }
    }


    fun updateTicketGroups(tickets: List<Ticket>) {
        // Create a map to store group - size mapping
        val groupSizeMap = mutableMapOf<String, Int>()

        // Calculate the size for each group
        tickets.forEach { ticket ->
            val group = ticket.group
            groupSizeMap[group] = groupSizeMap.getOrDefault(group, 0) + 1
        }

        // Assign the calculated size to each ticket's groupSize property
        tickets.forEach { ticket ->
            ticket.groupSize = groupSizeMap.getOrDefault(ticket.group, 1)
            DBF.updateData(ticket)
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

            // Check if the input groupName is a valid group number
            if (groupName.toIntOrNull() != null) {
                ticket.group = groupName
                updateTicketGroups(allTickets)
                updateTicketLists()
                alertDialog.dismiss()
            } else {
                Toast.makeText(context, "Must be a valid number", Toast.LENGTH_SHORT).show()
            }
        }
        dialogView.findViewById<Button>(R.id.groupPopup_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
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
                    ticketDatabase.update(ticket)
                }

                // Check if Ticket is connected to a Player
                if (ticket.playerId == "") {
                    createNewPlayer(ticket)
                }
            }
        }
        updateTeamPower()
        updateEventStatus()
    }

    private fun createNewPlayer(ticket: Ticket): Player {
        val newPlayerId = UUID.randomUUID().toString()

        return Player(
            playerId = newPlayerId,
            firstName = ticket.firstName ?: "",
            lastName = ticket.lastName ?: "",
            age = ticket.age ?: 0,
            exp2021 = 0,
            exp2022 = 0,
            exp2023 = 0,
            healerLevel = 1,
            rogueLevel = 1,
            mageLevel = 1,
            knightLevel = 1,
            healerUltimateA = false,
            healerUltimateB = false,
            rogueUltimateA = false,
            rogueUltimateB = false,
            mageUltimateA = false,
            mageUltimateB = false,
            knightUltimateA = false,
            knightUltimateB = false,
            warriorHealer = false,
            warriorRogue = false,
            warriorMage = false,
            warriorKnight = false,
            bookerNames = mutableListOf(ticket.bookerName!!),
            bookerEmails = mutableListOf(ticket.bookerEmail!!),
            bookerPhones = mutableListOf(ticket.bookerPhone!!),
            bookerAddresses = mutableListOf(ticket.bookerAddress!!),
        )
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
            DBF.updateData(ticket)
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

    fun checkInTicket(ticket: Ticket) {
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
            autoSetRoleAmounts()

            // Update database
            DBF.updateData(ticket)
            ticketDatabase.update(ticket)
            updateEventStatus()

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
        val bookerPhone: TextView = dialogView.findViewById(R.id.ti_booker_phone)
        bookerPhone.text = ticket.bookerPhone
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
                ticketDatabase.update(selectedTicket)

                alertDialog.dismiss()
            }
        }
        dialogView.findViewById<Button>(R.id.ae_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    private fun ResetTicket() {
        val ticket = selectedTicket
        deselectPlayer()
        val player = ticket.playerId?.let { playerDatabase.getById(it) }
        if (player != null) {
            player.healerLevel = 0
            player.healerUltimateA = false
            player.healerUltimateB = false
            player.rogueLevel = 0
            player.rogueUltimateA = false
            player.rogueUltimateB = false
            player.mageLevel = 0
            player.mageUltimateA = false
            player.mageUltimateB = false
            player.knightLevel = 0
            player.knightUltimateA = false
            player.knightUltimateB = false
            player.warriorHealer = false
            player.warriorRogue = false
            player.warriorMage = false
            player.warriorKnight = false
        }
        ticket.checkedIn = 0
        ticket.teamColor = ""
        DBF.updateData(ticket)
        updateTicketLists()
    }


    fun openWinnerPopup() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.set_winner_popup, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()

        val clickWinButton = dialogView.findViewById<Button>(R.id.sw_clickWinnerButton)
        val gameWinButton = dialogView.findViewById<Button>(R.id.sw_gameWinnerButton)
        var clickWinRed = event.clickWinner == "Red"
        var gameWinRed = event.gameWinner == "Red"
        var clickWinModified = false
        var gameWinModified = false

        // Set initial button colors
        if (event.clickWinner != "") {
            clickWinModified = true
            if (clickWinRed) {
                clickWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamRedColor
                    )
                )
            } else {
                clickWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamBlueColor
                    )
                )
            }
        }
        if (event.gameWinner != "") {
            gameWinModified = true
            if (gameWinRed) {
                gameWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamRedColor
                    )
                )
            } else {
                gameWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamBlueColor
                    )
                )
            }
        }

        clickWinButton.setOnClickListener {
            clickWinModified = true
            clickWinRed = !clickWinRed // Toggle the value
            if (clickWinRed) {
                clickWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamRedColor
                    )
                )
            } else {
                clickWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamBlueColor
                    )
                )
            }
        }
        gameWinButton.setOnClickListener {
            gameWinModified = true
            gameWinRed = !gameWinRed // Toggle the value
            if (gameWinRed) {
                gameWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamRedColor
                    )
                )
            } else {
                gameWinButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teamBlueColor
                    )
                )
            }
        }

        dialogView.findViewById<Button>(R.id.sw_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.sw_saveButton).setOnClickListener {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            // TODO: update event with winners
            event.clickWinner = if (clickWinModified) if (clickWinRed) "Red" else "Blue" else ""
            event.gameWinner = if (gameWinModified) if (gameWinRed) "Red" else "Blue" else ""
            DBF.updateData(event)
            alertDialog.dismiss()
            checkGameEnded()
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
        ticketDatabase.update(selectedTicket)
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
            ticketDatabase.update(ticket)

            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.an_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
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

        // Partition the list into solo and non-solo players
        val (nonSoloPlayers, soloPlayers) = assignList.partition { it.group != "SELF" && it.group != "" }
        assignList = (nonSoloPlayers + soloPlayers) as MutableList<Ticket>
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
            updateTicketStats(blueTeam)
            updateTicketStats(redTeam)
            updateRound(true)
            updateEventStatus()
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
        val roleAmounts = arrayOf(
            binding.healerAmountValue.text.toString().toInt(),
            binding.rogueAmountValue.text.toString().toInt(),
            binding.mageAmountValue.text.toString().toInt(),
            binding.knightAmountValue.text.toString().toInt(),
            binding.specialAAmountValue.text.toString().toInt(),
            binding.specialBAmountValue.text.toString().toInt()
        )

        team.forEach { it.currentRole = 7 }
        blueBench.forEach { it.currentRole = 7 }
        redBench.forEach { it.currentRole = 7 }

        val totalAmount = roleAmounts.sum()

        val guaranteedTickets = team.filter { it.guaranteedRole!! in 1..6 }
        for (ticket in guaranteedTickets) {
            val role = ticket.guaranteedRole!!
            if (roleAmounts[role - 1] > 0) {
                ticket.currentRole = role
                ticket.lastRole = role
                roleAmounts[role - 1]--
            }
            DBF.updateData(ticket)
        }

        val remainingTickets = team.filter { it.currentRole == 7 }
        val prioritizedTickets = remainingTickets.sortedBy { it.roundsSpecialRole }
            .take(totalAmount - guaranteedTickets.size)

        val roleAssignmentTracker = MutableList(roleAmounts.size) { mutableListOf<Ticket>() }

        for (ticket in prioritizedTickets) {
            var roleIndex = 0
            var minRounds = Int.MAX_VALUE

            for (i in roleAmounts.indices) {
                val roundsInRole = when (i) {
                    0 -> ticket.roundsHealer
                    1 -> ticket.roundsRogue
                    2 -> ticket.roundsMage
                    3 -> ticket.roundsKnight
                    else -> ticket.roundsSpecialRole
                }

                if (roleAmounts[i] > 0 && roundsInRole != null && roundsInRole < minRounds) {
                    minRounds = roundsInRole
                    roleIndex = i
                }
            }

            if (minRounds != Int.MAX_VALUE) {
                ticket.currentRole = roleIndex + 1
                ticket.lastRole = roleIndex + 1
                roleAmounts[roleIndex]--
                roleAssignmentTracker[roleIndex].add(ticket)
                DBF.updateData(ticket)
            }
        }

        val remainingRoles = mutableListOf<Int>()
        for (i in roleAmounts.indices) {
            for (j in 0 until roleAmounts[i]) {
                remainingRoles.add(i + 1)
            }
        }

        remainingRoles.shuffle()

        val assignedRoles = mutableSetOf<Int>() // Keep track of already assigned roles

        for (ticket in remainingTickets) {
            val availableRoles =
                remainingRoles.filter { it !in assignedRoles && it != ticket.lastRole }
            if (availableRoles.isNotEmpty()) {
                val randomIndex = (0 until availableRoles.size).random()
                val role = availableRoles[randomIndex]
                ticket.currentRole = role
                ticket.lastRole = role
                assignedRoles.add(role)
                DBF.updateData(ticket)
            }
        }

        val rolesDistributed = team.count { it.currentRole != 7 }
        if (rolesDistributed != totalAmount) {
            return false
        }

        teamSorting = 2
        updateTicketLists()
        bottomPanel.visibility = View.VISIBLE
        bottomPanelNewRound.visibility = View.GONE
        return true
    }

    private fun assignRoles(team: MutableList<Ticket>) {
        for (ticket in team) {
            when (ticket.currentRole) {
                1 -> ticket.roundsHealer = ticket.roundsHealer!! + 1
                2 -> ticket.roundsRogue = ticket.roundsRogue!! + 1
                3 -> ticket.roundsMage = ticket.roundsMage!! + 1
                4 -> ticket.roundsKnight = ticket.roundsKnight!! + 1
                5, 6 -> ticket.roundsSpecial++
            }
        }
    }

    private fun updateTicketStats(team: MutableList<Ticket>) {
        for (ticket in team) {
            if (ticket.currentRole != 7) {
                ticket.roundsSpecialRole = ticket.roundsSpecialRole!! + 1
            }

            if (ticket.currentRole == ticket.guaranteedRole) {
                ticket.guaranteedRole = 0
            }
            DBF.updateData(ticket)
        }
    }

    private fun updateRound(increase: Boolean) {
        if (increase) event.round = event.round!! + 1 else event.round = event.round!! - 1
        binding.roundText.text = event.round.toString()
        DBF.updateData(event)
    }

    fun loadTestData() {
        // Generate sample players
        val tickets = listOf(
            Ticket(
                "T1", "Marcus", "Bildtgård", 15, "Jane Doe", "555-123-4567",
                "123 Main St", "Springfield", "john@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T2", "Annelie", "Öhman", 12, "John Doe", "555-987-6543",
                "456 Elm St", "Springfield", "john@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T3", "Peter", "Losonci", 13, "Bob Smith", "555-456-7890",
                "789 Oak St", "Springfield", "alice@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T4", "Mattias", "Evaldsson Fritz", 12, "Alice Brown", "555-321-0987",
                "321 Birch St", "Springfield", "alice@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T5", "Vandela", "Aghed", 11, "Diana Johnson", "555-654-3210",
                "654 Pine St", "Springfield", "alice@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T6", "Emilia", "Hagman", 10, "Charlie Miller", "555-852-1470",
                "852 Maple St", "Springfield", "diana@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T7", "Renée", "Olsson", 9, "David Taylor", "555-555-5555",
                "10 Oak St", "Springfield", "eva@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T0", "Enoo", "Rasmussen", 8, "Emma Wilson", "555-789-4561",
                "741 Vine St", "Springfield", "edward@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T8", "Elin", "Torndal", 7, "Frank Adams", "555-123-7890",
                "369 Oak St", "Springfield", "edward@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T9", "Julia", "Löf", 6, "George Garcia", "555-456-1234",
                "852 Chestnut St", "Springfield", "george@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T10", "Anna", "Wuolo", 5, "Henry Scott", "555-789-0123",
                "753 Main St", "Springfield", "hannah@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T11", "Fredrik", "Åslund", 15, "Jane Doe", "555-123-4567",
                "123 Main St", "Springfield", "john@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T12", "Karolina", "Nilsson", 14, "John Doe", "555-987-6543",
                "456 Elm St", "Springfield", "john@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T13", "Olof", "Berg", 13, "Bob Smith", "555-456-7890",
                "789 Oak St", "Springfield", "alice@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T14", "Filip", "Lindahl", 12, "Alice Brown", "555-321-0987",
                "321 Birch St", "Springfield", "alice@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T15",
                "Hanna",
                "Nevo",
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
                100,
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
                ""
            ),
            Ticket(
                "T16", "Cecilia", "Lindh", 10, "Charlie Miller", "555-852-1470",
                "852 Maple St", "Springfield", "diana@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T17",
                "Ulrike",
                "Dawod",
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
                100,
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
                ""
            ),
            Ticket(
                "T18", "Isabelle", "Utbult", 8, "Emma Wilson", "555-789-4561",
                "741 Vine St", "Springfield", "edward@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            ),
            Ticket(
                "T31", "Adam", "Asp", 8, "Emma Wilson", "555-789-4561",
                "741 Vine St", "Springfield", "edward@example.com", null, "", 0, 0, 100, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, ""
            )
        )

        // Insert sample tickets into the ticketDatabase
        tickets.forEach { ticketDatabase.insert(it) }


        val players = listOf(
            Player(
                playerId = "ABC123",
                firstName = "Fredrik",
                lastName = "Åslund",
                age = 15,
                exp2021 = 205,
                exp2022 = 1000,
                exp2023 = 1000,
                healerLevel = 1,
                healerUltimateA = false,
                healerUltimateB = false,
                rogueLevel = 1,
                rogueUltimateA = false,
                rogueUltimateB = false,
                mageLevel = 1,
                mageUltimateA = false,
                mageUltimateB = false,
                knightLevel = 1,
                knightUltimateA = false,
                knightUltimateB = false,
                warriorHealer = false,
                warriorRogue = false,
                warriorMage = false,
                warriorKnight = false,
                bookerNames = mutableListOf("susanne", "thorvald"),
                bookerEmails = mutableListOf("susanne@email.com", "thorvald@email.com"),
                bookerPhones = mutableListOf("0918239013", "128309312"),
                bookerAddresses = mutableListOf("nyckeldalen 3", "spårvagnen 4")
            ),
            Player(
                playerId = "ABC124",
                firstName = "Jane",
                lastName = "Doe",
                age = 15,
                exp2021 = 205,
                exp2022 = 1000,
                exp2023 = 5,
                healerLevel = 1,
                healerUltimateA = true,
                rogueLevel = 1,
                rogueUltimateA = true,
                mageLevel = 1,
                mageUltimateA = true,
                knightLevel = 1,
                knightUltimateA = true,
                warriorHealer = false,
                warriorRogue = false,
                warriorMage = false,
                warriorKnight = false,
                bookerNames = mutableListOf("susanne"),
                bookerEmails = mutableListOf("susanne@email.com"),
                bookerPhones = mutableListOf("0918239013"),
                bookerAddresses = mutableListOf("nyckeldalen 3")
            )
        )

        // Insert sample player into the playerDatabase
        players.forEach { playerDatabase.insert(it) }
    }

    private fun dismissKeyboard() {
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
        updateEventStatus()
    }

    private fun callNotification(message: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.note_popup, null)

        val builder = AlertDialog.Builder(context).setView(dialogView)

        val notification = builder.show()

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

        DBF.updateData(selectedTicket)
        updateTicketLists()
        autoSetRoleAmounts()
    }

    fun layoutFunction() {
        dismissKeyboard()
        deselectPlayer()
    }

    fun addOneTicket() {
        ticketDatabase.insert(
            Ticket(
                "T30", "New", "Man", 16, "william Scott", "555-789-0123",
                "753 Main St", "Springfield", "william@example.com", null, "", 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, null, event.eventId
            )
        )
        event.ticketIDs.add("T30")
        Log.i("check", "additional ticket added")

        // Update the event in the local database
        eventDatabase.update(event)
    }

    private fun checkGameEnded() {
        // Check if game is already over, and remove New Round Button
        if (event.clickWinner != "" || event.gameWinner != "") {
            binding.newRoundButton.visibility = View.GONE
            binding.gameEndText.visibility = View.VISIBLE
        } else {
            binding.newRoundButton.visibility = View.VISIBLE
            binding.gameEndText.visibility = View.GONE
        }
        updateEventStatus()
    }

    fun updateEventStatus() {
        if (allTickets.size < 1) return

        var ticketTeamDivision = false
        var ticketCheckIn = false

        for (ticket in allTickets) {
            if (ticket.teamColor != null) {
                ticketTeamDivision = true
            }

            if (ticket.checkedIn == 1) {
                ticketCheckIn = true
            }

            if (ticketTeamDivision && ticketCheckIn) break // if both conditions are met, break the loop
        }

        event.status = when {
            event.clickWinner != "" || event.gameWinner != "" -> "Avslutat"
            event.round != null && event.round!! > 0 -> "Spel påbörjat"
            ticketCheckIn -> "Checkar in"
            ticketTeamDivision -> "Lagindelning"
            else -> "Ej påbörjat"
        }
        eventDatabase.update(event) //TODO: remove Local
        DBF.updateData(event)
        Log.i("status", "Event Status: ${event.status}")
    }
}