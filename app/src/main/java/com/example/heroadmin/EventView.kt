package com.example.heroadmin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heroadmin.databinding.FragmentEventViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.min

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
    var allTickets: MutableList<Ticket> = mutableListOf()
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
    private lateinit var bottomPanelSetWinner: LinearLayout
    private lateinit var playerRoleButtonPanel: LinearLayout
    private lateinit var loadingDialogue: AlertDialog
    private var healerAmount: Int = 0
    private var rogueAmount: Int = 0
    private var mageAmount: Int = 0
    private var knightAmount: Int = 0
    private var specialAAmount: Int = 0
    private var specialBAmount: Int = 0
    private var hasTeamItemSelected: Boolean = false
    private var assignSorting = 3
    private var checkInSorting = 0
    private var teamSorting = 0
    private var redPowerLevel = 0
    private var bluePowerLevel = 0
    private var redTeenAmount = 0
    private var blueTeenAmount = 0
    private var redTiniesAmount = 0
    private var blueTiniesAmount = 0
    private var autoRoleAmounts = true
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    public var allTicketsMatched = false
    var devMode: Boolean = false
    private var winnerPanel = false
    private var gameWinner = ""

    // Colors (set in OnResume)
    private var noWinnerUnselectedColor = 0
    private var noWinnerColor = 0
    private var redUnselectedColor = 0
    private var redWinnerColor = 0
    private var blueUnselectedColor = 0
    private var blueWinnerColor = 0
    private var tieActiveColor = 0
    private var tieUnselectedColor = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_view, container, false)
        v = inflater.inflate(R.layout.fragment_event_view, container, false)
        DBF = DatabaseFunctions(v.context)
        DBF.setEventView(this)
        args = EventViewArgs.fromBundle(requireArguments())
        currEventId = args.passedEventId
        currActivity = (activity as MainActivity)
        DBF.currActivity = currActivity
        event = currActivity.event

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        devMode = currActivity.devMode
        // Find elements
        bottomPanel = binding.bottomPanel
        bottomPanelNewRound = binding.bottomPanelNewRound
        bottomPanelPlayer = binding.bottomPanelPlayer
        bottomPanelSetWinner = binding.bottomPanelSetWinner
        playerRoleButtonPanel = binding.playerRoleButtonPanel

        loadingPopup()
        getEvent()
        checkConnection()
        updateBottomPanel(0)

        binding.scrollingPanel.setOnTouchListener { _, _ ->
            pressLayoutFunction()
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
            deselectTeamItem(true)
        }

        binding.newRoundButton.setOnClickListener {
            deselectTeamItem(true)
            updateBottomPanel(1)
            checkTeamSizes()
        }

        binding.cancelNewRoundButton.setOnClickListener {
            deselectTeamItem(true)
            updateBottomPanel(0)
            dismissKeyboard()
        }

        binding.switchTeamButton.setOnClickListener {
            switchTeam()
        }

        binding.switchTeamButton2.setOnClickListener {
            switchTeam()
        }

        binding.levelUpButton.setOnClickListener {
            if (selectedTicket.playerId != null && selectedTicket.playerId != "") {
                if (selectedTicket.playerId == "null") {
                    callNotification("Matcha om spelaren via spelarens INFO-knapp först!")
                } else {
                    if (event.round > 0 && event.status != "Avslutat") {
                        val message =
                            if (event.gameWinner == "") "No Game Winner set. Do you want to set winners before levelling up?"
                            else if (event.clickWinner == "") "No Click Winner set. Do you want to set winners before levelling up?"
                            else "Error!? Big wtf! You should probably set winners."

                        callChoice(
                            message,
                            "Set Winners",
                            "Level Up",
                            ::openWinnerPopup,
                            ::goToLevelUp
                        )
                    } else {
                        goToLevelUp()
                    }
                }
            } else {
                callNotification("Re-Match player first via INFO button, please.\nIf none")
            }
        }

        binding.blueWinsPlus.setOnClickListener {
            event.blueGameWins = event.blueGameWins + 1
            updateGameWins()
        }
        binding.blueWinsMinus.setOnClickListener {
            if (event.blueGameWins > 0) {
                event.blueGameWins = event.blueGameWins - 1
                updateGameWins()
            }
        }
        binding.redWinsPlus.setOnClickListener {
            event.redGameWins = event.redGameWins + 1
            updateGameWins()
        }
        binding.redWinsMinus.setOnClickListener {
            if (event.redGameWins > 0) {
                event.redGameWins = event.redGameWins - 1
                updateGameWins()
            }
        }

        binding.ticketInfoButton.setOnClickListener {
            openTicketInfo()
        }

        binding.recruitsButton.setOnClickListener {
            checkInTicket(selectedTicket)
        }

        binding.rollRoundButton2.setOnClickListener {
            val h = binding.healerAmountValue.text.toString().toInt()
            val r = binding.rogueAmountValue.text.toString().toInt()
            val m = binding.mageAmountValue.text.toString().toInt()
            val k = binding.knightAmountValue.text.toString().toInt()
            val a = binding.specialAAmountValue.text.toString().toInt()
            val b = binding.specialBAmountValue.text.toString().toInt()
            if ((h + r + m + k + a + b) > blueTeam.size || (h + r + m + k + a + b) > redTeam.size) {
                callNotification("There are more roles than players!\nEdit your role amounts.")
            } else {
                randomizeRoles()
                dismissKeyboard()
                winnerPanel = true
                deselectTeamItem(true)
                binding.gameWinAccept.isEnabled = false
                updateBottomPanel(0)
            }
        }

        val roleAmounts = mutableListOf(
            binding.healerAmountValue,
            binding.rogueAmountValue,
            binding.mageAmountValue,
            binding.knightAmountValue,
            binding.specialAAmountValue,
            binding.specialBAmountValue
        )

        for (role in roleAmounts) {
            role.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {

                    autoRoleAmounts = false
                    binding.autoSetSwitch.isChecked = false
                }
                false // Return 'false' to allow the touch event to be passed to the underlying view
            }
            role.setOnClickListener {
                autoRoleAmounts = false
                binding.autoSetSwitch.isChecked = false
            }
        }

        binding.healerButton.setOnClickListener {
            selectedTicket.guaranteedRole = 1
        }
        binding.rogueButton.setOnClickListener {
            selectedTicket.guaranteedRole = 2
        }
        binding.mageButton.setOnClickListener {
            selectedTicket.guaranteedRole = 3
        }
        binding.knightButton.setOnClickListener {
            selectedTicket.guaranteedRole = 4
        }
        binding.specialAButton.setOnClickListener {
            selectedTicket.guaranteedRole = 5
        }
        binding.specialBButton.setOnClickListener {
            selectedTicket.guaranteedRole = 6
        }

        binding.assignTeamOrgByNameButton.setOnClickListener {
            assignSorting = 0
            updateTicketLists()
        }
        binding.assignTeamOrgByAgeButton.setOnClickListener {
            assignSorting = 1
            updateTicketLists()
        }
        binding.assignTeamOrgByMatchButton.setOnClickListener {
            assignSorting = if (allTicketsMatched) 3 else 2
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
        binding.checkInOrgByGroup.setOnClickListener {
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
        binding.setWinnerButton.setOnClickListener {
            openWinnerPopup()
        }
        binding.refreshButton.setOnClickListener {
            loadingDialogue.show()
            getEvent()
            binding.refreshButton.isEnabled = false
        }
        binding.klippkortButton.setOnClickListener {
            //Future shenanigans, save for later
//            callNotification("This button does nothing.\nI promise.")
//            testFunction()
            openKlippkortsLink()
        }
        binding.roll20.setOnClickListener {
            roll20rounds()
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
        binding.autoSetSwitch.isChecked = autoRoleAmounts
        binding.autoSetSwitch.setOnClickListener {
            if (autoRoleAmounts) {
                autoRoleAmounts = false
            } else {
                autoRoleAmounts = true
                autoSetRoleAmounts()
            }
        }

        if (devMode) {
            binding.devModeLayout.visibility = View.VISIBLE
        } else {
            binding.devModeLayout.visibility = View.GONE
        }

        binding.fullReset.setOnClickListener {
            event.round = 0
            event.clickWinner = ""
            event.gameWinner = ""
            event.blueGameWins = 0
            event.redGameWins = 0
            checkGameEnded()
            allTickets.forEach {
                it.teamColor = ""
                it.checkedIn = 0
                it.roundsHealer = 0
                it.roundsRogue = 0
                it.roundsMage = 0
                it.roundsKnight = 0
                it.roundsWarrior = 0
                it.roundsSpecialRole = 0
                it.guaranteedRole = 0
                it.benched = 0
                it.recruits = 0
                it.expPersonal = 0
                it.currentRole = 0
                it.lastRole = 0
            }
            updateGameWins()
            binding.roundText.text = event.round.toString()
            initializeTicketGroups()
            updateTicketLists()
            DBF.updateTicketArray(allTickets)
        }
        binding.resetToCheckin.setOnClickListener {
            event.round = 0
            event.clickWinner = ""
            event.gameWinner = ""
            event.blueGameWins = 0
            event.redGameWins = 0
            checkGameEnded()
            allTickets.forEach {
                it.checkedIn = 0
                it.roundsHealer = 0
                it.roundsRogue = 0
                it.roundsMage = 0
                it.roundsKnight = 0
                it.roundsWarrior = 0
                it.roundsSpecialRole = 0
                it.guaranteedRole = 0
                it.benched = 0
                it.recruits = 0
                it.expPersonal = 0
                it.currentRole = 0
                it.lastRole = 0
            }
            updateGameWins()
            binding.roundText.text = event.round.toString()
            initializeTicketGroups()
            updateTicketLists()
            DBF.updateTicketArray(allTickets)
        }
        binding.resetRoles.setOnClickListener {
            resetRoles()
        }

        binding.checkinAll.setOnClickListener {
            autoAssignTeams()
            allTickets.forEach {
                it.checkedIn = 1
            }
            updateTicketLists()
        }

        binding.gameWinRed.setOnClickListener {
            gameWinner = "Red"
            updateGameWinButtons()
        }
        binding.gameWinBlue.setOnClickListener {
            gameWinner = "Blue"
            updateGameWinButtons()
        }
        binding.gameWinTie.setOnClickListener {
            gameWinner = "Tie"
            updateGameWinButtons()
        }
        binding.gameWinAccept.setOnClickListener {
            setRoundWinner()
        }

        noWinnerUnselectedColor = ContextCompat.getColor(requireContext(), R.color.grey)
        noWinnerColor = ContextCompat.getColor(requireContext(), R.color.white)
        redUnselectedColor = ContextCompat.getColor(requireContext(), R.color.light_pink)
        redWinnerColor = ContextCompat.getColor(requireContext(), R.color.winning_red)
        blueUnselectedColor = ContextCompat.getColor(requireContext(), R.color.light_blue)
        blueWinnerColor = ContextCompat.getColor(requireContext(), R.color.winning_blue)
        tieActiveColor = ContextCompat.getColor(requireContext(), R.color.purple_deep)
        tieUnselectedColor = ContextCompat.getColor(requireContext(), R.color.purple_200)
    }

    private fun loadingPopup() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.loading_popup, null)

        val builder =
            AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
                .setView(dialogView)
        val cancelButton: Button = dialogView.findViewById(R.id.loading_cancelButton)

        cancelButton.setOnClickListener {
            findNavController().navigate(
                EventViewDirections.actionEventViewToEventAdminFrag(
                    currEventId
                )
            )
        }

        loadingDialogue = builder.show()
    }

    private fun getEvent() {
        Log.i("gettingEvent", "Calling API for event: $currEventId")
        DBF.apiCallGet(
            "https://www.talltales.nu/API/api/get-event.php?eventID=$currEventId",
            ::refreshEvent,
            {
                Log.i("gettingEvent", "Could not get event")
                refreshEventLocal()
            }, 3
        )
    }

    private fun refreshEvent(response: JSONObject) {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        Log.d("start", "JSON input for Event: ${response.toString()}")
        val eventData = response.getJSONObject("data")

        event = json.decodeFromString<Event>(eventData.toString())
        Log.i("start", "Refreshing event...")

        // Set variables
        binding.blueWinsValue.text = event.blueGameWins.toString()
        binding.redWinsValue.text = event.redGameWins.toString()
        binding.roundText.text = event.round.toString()

        checkGameEnded()

        fetchEventTickets()
    }

    private fun refreshEventLocal() {
        Log.i("start", "Refreshing event locally...")
        event = currActivity.event

        // Set variables
        binding.blueWinsValue.text = event.blueGameWins.toString()
        binding.redWinsValue.text = event.redGameWins.toString()
        binding.roundText.text = event.round.toString()

        checkGameEnded()

        fetchEventTicketsLocal()
    }

    private fun fetchEventTickets() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i("playerLink", "Starting fetchEventTickets")
            try {
                val tickets = getTickets(event.eventId) // Fetch the Ticket data
                Log.i("playerLink", "Got Tickets")

                // Switch to the Main dispatcher for UI updates
                withContext(Dispatchers.Main) {


                    // Process tickets
                    processTickets(tickets)
                    Log.i(
                        "playerLink",
                        "Linking done. allTickets size: ${allTickets.size}"
                    )  // Log the size of 'allTickets'
                    checkForDoubles()
                    updateTicketLists()
                    initializeTicketGroups()
                    checkConnection()
                    loadingDialogue.dismiss()
                    binding.refreshButton.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchEventTicketsLocal() {
        Log.i("playerLink", "Starting fetchEventTickets")
        ticketDatabase.logAllIds("playerLink")
        val tickets = ticketDatabase.getByIds(event.ticketIDs).filterNotNull()
        if (tickets.isNotEmpty()) {
            // Process tickets without playerId or suggestions
            processTickets(tickets as List<Ticket>)

            checkForDoubles()
            updateTicketLists()
            initializeTicketGroups()
        }

        loadingDialogue.dismiss()
        binding.refreshButton.isEnabled = true
    }

    // Function to get full Ticket objects based on an event ID
    private suspend fun getTickets(eventId: String, retryCount: Int = 3): List<Ticket> {
        return suspendCancellableCoroutine { continuation ->
            DBF.apiCallGetArray(
                "https://www.talltales.nu/API/api/get-tickets.php?eventId=$eventId",
                { response ->
                    try {
                        val tickets =
                            json.decodeFromString<List<Ticket>>(response.toString())
                        continuation.resume(tickets)
                    } catch (e: Exception) {
                        Log.e("playerLink", "Error in decoding or resuming the continuation: ", e)
                        continuation.resume(emptyList())
                    }
                },
                {
                    // Handle error case here
                    continuation.resume(emptyList()) // Resume the suspended coroutine with an empty list
                    Log.i("playerLink", "Failed to get Tickets for Event: $eventId")
                },
                retryCount
            )
            Log.i("playerLink", "API Called")
        }
    }


    private fun processTickets(tickets: List<Ticket>) {
        Log.i("processTickets", "Processing Tickets: ${tickets.size} tickets")
        // TODO: Add tickets with a playerId to a list
        for (ticket in tickets) {
            if (ticket.playerId != null && ticket.playerId != "") {
                Log.i("processTickets", "${ticket.firstName} has player")
            } else if (ticket.suggestions.isNullOrEmpty()) {
                // Creates new player
                createNewPlayer(ticket)
                Log.i("processTickets", "Created new player for ${ticket.firstName}")
            } else {
                Log.i("processTickets", "${ticket.firstName} has suggestions")
            }
            ticketDatabase.insert(ticket)
        }

        // TODO: Send ApiCall list of playerIDs to get the players
        // TODO: put players in playerDatabase

        // Update the shared 'allTickets' list
        allTickets.clear()
        allTickets.addAll(tickets)
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
        val playerListItems = ticket.suggestions?.map { player ->
            PlayerListItem(
                playerID = player.playerID,
                firstName = player.firstName,
                lastName = player.lastName,
                age = player.age,
                bookerNames = player.bookerNames,
                bookerPhones = player.bookerPhones,
                bookerEmails = player.bookerEmails,
                bookerAddresses = player.bookerAddresses
            )
        }?.toMutableList()

// Declare selectedItem variable here
        var adapter: PlayerListItemAdapter? = null
        var selectedItem: PlayerListItem? = null

        adapter = PlayerListItemAdapter(
            playerListItems ?: mutableListOf(), // Use the null-safe operator here
            object : PlayerListItemAdapter.OnItemTouchListener {

                override fun onTouch(
                    view: View,
                    event: MotionEvent,
                    position: Int,
                    playerListItem: PlayerListItem
                ) {
                    // ... onTouch code ...
                    adapter?.toggleSelection(position)
                    selectedItem = if (adapter?.selectedPosition != -1) playerListItem else null

                    // Get the reference for the acceptButton
                    val acceptButton: Button = dialogView.findViewById(R.id.mpl_acceptButton)

                    // Change the color of the acceptButton based on the selected item count
                    if (adapter?.selectedPosition != -1) {
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

        CoroutineScope(Job() + Dispatchers.Main).launch {
            delay(2)
            adapter.notifyDataSetChanged()
        }

        recyclerView.adapter = adapter
        // Get the reference for the buttons
        val acceptButton = dialogView.findViewById<Button>(R.id.mpl_acceptButton)
        val newPlayerButton = dialogView.findViewById<Button>(R.id.mpl_newPlayerButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.mpl_cancelButton)

        // Set click listeners for the buttons
        cancelButton.setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        // Set click listeners for the buttons
        newPlayerButton.setOnClickListener {
            createNewPlayer(ticket)
            ticketDatabase.update(ticket)
            DBF.updateTicketArray(allTickets)
            updateTicketLists()
            alertDialog.dismiss()
        }

        // Set the click listener for the accept button
        acceptButton.setOnClickListener {
            val currentSelectedItem = selectedItem
            if (currentSelectedItem != null) {
                // Update the playerId of the ticket
                ticket.playerId = currentSelectedItem.playerID

                // Save the updated ticket
                DBF.updateTicketArray(allTickets)
                ticketDatabase.update(ticket)
                lifecycleScope.launch {
                    try {
                        DBF = DatabaseFunctions(v.context)
                        val player = DBF.getPlayer(ticket.playerId!!)
                        if (player != null) {
                            player.updateUsedExp()
                            playerDatabase.insert(player)
                        }
                    } catch (e: Exception) {
                        if (ticket.playerId != null && ticket.playerId != "")
                            Log.i("getPlayer", "Could not get player from database")
                        else
                            Log.i("getPlayer", "Could not find playerId")
                    }
                }
                updateTicketLists()

                // Close the alertDialog
                alertDialog.dismiss()
            } else {
                // Show a message that no player is selected, or handle the case where a new player should be created
            }
        }
    }

    private fun deselectTeamItem(updatePanel: Boolean) {
        if (!hasTeamItemSelected) {
            return
        }
        selectedTicketTVH.deselect()
        hasTeamItemSelected = false
        if (updatePanel) updateBottomPanel(0)
    }

    fun updateTicketLists() {
        assignList = mutableListOf()
        checkInList = mutableListOf()
        redTeam = mutableListOf()
        blueTeam = mutableListOf()
        redBench = mutableListOf()
        blueBench = mutableListOf()

        for (ticket in allTickets) {
            // Skip this ticket if it's a double
            if (ticket.double) continue

            when {
                ticket.teamColor == "None" || ticket.teamColor == "" || ticket.teamColor == null -> {
                    assignList.add(ticket)
                    Log.i("tickets", "Updating assignList")
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

        // Check if assignList tickets should be auto-grouped and auto-set into a team
        val ticketsToBeRemoved = mutableListOf<Ticket>()

        for (aTicket in assignList) {
            val teamLists = redTeam + blueTeam
            val emailToFind = aTicket.bookerEmail
            val matchingTicket =
                teamLists.firstOrNull { it.bookerEmail.equals(emailToFind, ignoreCase = true) }

            if (matchingTicket != null) {
                aTicket.teamColor = matchingTicket.teamColor
                val chosenList = if (aTicket.teamColor == "Red") redTeam else blueTeam
                chosenList.add(aTicket)
                ticketsToBeRemoved.add(aTicket)
            }
        }

        assignList.removeAll(ticketsToBeRemoved)


        // Check if autoAssign button should be visible
        val allTicketsHavePlayerId = assignList.all { it.playerId != null && it.playerId != "" }

        binding.assignTeamAutoAssignButton.visibility = if (allTicketsHavePlayerId) {
            allTicketsMatched = true
            View.VISIBLE
        } else {
            allTicketsMatched = false
            View.GONE
        }

        Log.i("sorting", "sorting by: $assignSorting")
        // Sort
        when (assignSorting) {
            0 -> sortAssignByName()
            1 -> sortAssignByAge()
            2 -> sortAssignByMatched()
            3 -> sortAssignByGroup()
        }

        when (checkInSorting) {
            0 -> sortCheckInByName()
            1 -> sortCheckInByAge()
            2 -> sortCheckInByGroup()
            3 -> sortCheckInByColor()
        }

        when (teamSorting) {
            0 -> sortTeamsByName()
            2 -> sortTeamsByRole()
        }

        checkConnection()
        setAssignTeamAdapter()
        setCheckInAdapter()
        setTeamAdapters()
        checkListVisibilities()
        updateTeamPower()
        autoSetRoleAmounts()
    }

    private fun initializeTicketGroups() {
        Log.i("initializeTicketGroups", "Starting initialization. Ticket amount: ${assignList.size}")

        // Determine the maximum existing group number from allTickets
        val maxExistingGroupNumber = allTickets.maxOfOrNull { it.group?.toIntOrNull() ?: 0 } ?: 0
        val emailToGroupMap = mutableMapOf<String, Int>()

        // Initialize emailToGroupMap with existing groups from allTickets
        for (ticket in allTickets) {
            val bookerEmail = ticket.bookerEmail
            val groupNumber = ticket.group?.toIntOrNull()
            if (bookerEmail != null && groupNumber != null) {
                emailToGroupMap[bookerEmail] = groupNumber
            }
        }

        val usedGroupNumbers = (1..maxExistingGroupNumber).toSet().toMutableSet() // Include existing group numbers
        val groupToSizeMap = mutableMapOf<Int, Int>()
        var newGroupNumber = maxExistingGroupNumber + 1 // Start from the next in line

        // First pass: Assign group numbers and calculate group sizes.
        for (ticket in assignList) {
            val bookerEmail = ticket.bookerEmail ?: continue
            val groupNumber = emailToGroupMap[bookerEmail] ?: run {
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

            Log.i("initializeTicketGroups", "First Pass")
        }

        // Second pass: Assign group sizes to tickets now that groups are made.
        for (ticket in assignList) {
            if (ticket.group == "") {
                ticket.groupSize = 1
                continue
            }

            val groupNumber = ticket.group?.toIntOrNull() ?: continue
            val groupSize = groupToSizeMap[groupNumber] ?: 1
            ticket.groupSize = groupSize
            if (groupSize == 1) { // If the group size is 1, set the group as ""
                ticket.group = ""
            }
            Log.i("initializeTicketGroups", "Second Pass")
        }

        // Create a map with group identifier as key and group size as value.
        val groupSizeMap = mutableMapOf<String, Int>()
        for (ticket in assignList) {
            val group = ticket.group
            if (group != null && group != "SELF" && group != "") {
                groupSizeMap[group] = ticket.groupSize
            }
            Log.i("initializeTicketGroups", "Third pass. Done ticket: ${ticket.ticketId}")
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
            if (ticket.group != "SELF" && ticket.group != "" && ticket.group != null) {
                ticket.group = reassignedGroupMap[ticket.group] ?: ticket.group
                checkForGroupTeam(ticket)
            }
        }

//        DBF.updateTicketArray(allTickets)

        Log.i("initializeTicketGroups", "Ending initialization")
        assignSorting = if (allTicketsMatched) 3 else 2

        updateTicketLists()
    }

    private fun checkForGroupTeam(ticket: Ticket) {
        if (!ticket.group.isNullOrEmpty() && ticket.group != "SELF" && ticket.teamColor.isNullOrEmpty()) {
            allTickets.forEach { matchTicket ->
                if (ticket.group == matchTicket.group && !matchTicket.teamColor.isNullOrEmpty()) {
                    ticket.teamColor = matchTicket.teamColor
                    return
                }
            }
        }
    }

    private fun checkForDoubles() {
        for (i in allTickets.indices) {
            for (j in i + 1 until allTickets.size) {
                val firstTicket = allTickets[i]
                val secondTicket = allTickets[j]

                if ((firstTicket.firstName == secondTicket.firstName &&
                            firstTicket.lastName == secondTicket.lastName &&
                            firstTicket.bookerPhone == secondTicket.bookerPhone) ||
                    (firstTicket.firstName == secondTicket.firstName &&
                            firstTicket.lastName == secondTicket.lastName &&
                            firstTicket.bookerEmail == secondTicket.bookerEmail)
                ) {
                    secondTicket.double = true
                }
            }
        }
    }


    private fun updateTicketGroups(tickets: MutableList<Ticket>) {
        // Create a map to store group - size mapping
        val groupSizeMap = mutableMapOf<String, Int>()

        // Calculate the size for each group
        tickets.forEach { ticket ->
            val group = ticket.group
            if (group != null && group != "SELF" && group != "") {
                groupSizeMap[group] = groupSizeMap.getOrDefault(group, 0) + 1
            }
        }

        // Assign the calculated size to each ticket's groupSize property
        tickets.forEach { ticket ->
            ticket.groupSize = groupSizeMap.getOrDefault(ticket.group, 1)
        }
        DBF.updateTicketArray(allTickets)
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
            ticket.group = groupName
            updateTicketGroups(allTickets)
            updateTicketLists()
            alertDialog.dismiss()
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
                    ticketDatabase.update(ticket)
                }

                // Check if Ticket is connected to a Player
                if (ticket.playerId == "") {
                    createNewPlayer(ticket)
                }
            }
        }

        if (setDatabase) {
            // Update database
            DBF.updateTicketArray(allTickets)
        }

        updateTeamPower()
        updateEventStatus()
    }

    private fun createNewPlayer(ticket: Ticket): Player {
        val newPlayerId = UUID.randomUUID().toString()

        val newPlayer = Player(
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
        ticket.playerId = newPlayerId
        playerDatabase.insert(newPlayer)
        val playerString = DBF.createJsonString(newPlayer)
        DBF.apiCallPost(
            "https://www.talltales.nu/API/api/create-player.php",
            {},
            {},
            playerString
        )
        return newPlayer
    }

    private fun autoAssignLoop() {
        var loop = 0
        var bestDifference = 1000
        val versionList: MutableList<Ticket> = assignList
        var bestBlueList: MutableList<Ticket> = mutableListOf()
        var bestRedList: MutableList<Ticket> = mutableListOf()

        while (loop < 50) {
            // Randomize and save version of list
            versionList.shuffle()

            // Assign teams with that list (which also sorts it)
            autoAssignTeams()

            // Check how well that went
            val difference = abs(bluePowerLevel - redPowerLevel)
            if (difference < bestDifference) {

                // If better than before, save that version of team lists
                bestDifference = difference

                bestRedList = mutableListOf()
                bestBlueList = mutableListOf()

                for (ticket in versionList) {
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
            if (ticket.group != "" && ticket.group != null && !groupList.contains(ticket.group)) {
                groupList.add(ticket.group!!)
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
            if (ticket.group == "" || ticket.group == null) {
                if (bluePowerLevel > redPowerLevel) {
                    ticket.teamColor = "Red"
                } else {
                    ticket.teamColor = "Blue"
                }
            }
            updateTeamPower()
        }
        DBF.updateTicketArray(allTickets)
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
            TeamRecyclerAdapter(redTeam, this)
        blueTeamAdapter =
            TeamRecyclerAdapter(blueTeam, this)
        redBenchAdapter =
            TeamRecyclerAdapter(redBench, this)
        blueBenchAdapter =
            TeamRecyclerAdapter(blueBench, this)

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

    fun checkInTicket(ticket: Ticket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.checkin_popup, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()
        val name: TextView = dialogView.findViewById(R.id.checkInPopupNameText)
        name.text = ticket.fullName
        val recruitsEditText: EditText = dialogView.findViewById(R.id.cp_recruitsAmount)
        recruitsEditText.setText(ticket.recruits.toString())

        val acceptButton = dialogView.findViewById<Button>(R.id.checkinAcceptButton)

        if (ticket.teamColor == "Blue") {
            name.setBackgroundResource(R.color.teamBlueColor)
        } else {
            name.setBackgroundResource(R.color.teamRedColor)
        }

        //Set Klippkort visibility
        val klippkortCheckBox: CheckBox = dialogView.findViewById(R.id.checkInKlippkortCheck)
        if (ticket.klippkort == 1 && ticket.checkedIn == 0) {
            klippkortCheckBox.visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.checkInKlippkortTitle).visibility =
                View.VISIBLE
            acceptButton.isEnabled = false
        } else {
            klippkortCheckBox.visibility = View.GONE
            dialogView.findViewById<TextView>(R.id.checkInKlippkortTitle).visibility = View.GONE
            acceptButton.isEnabled = true
        }

        klippkortCheckBox.buttonTintList = ColorStateList.valueOf(Color.BLACK)

        klippkortCheckBox.setOnClickListener {
            if (klippkortCheckBox.visibility == View.VISIBLE) acceptButton.isEnabled =
                klippkortCheckBox.isChecked

        }

        dialogView.findViewById<Button>(R.id.checkinAcceptButton).setOnClickListener {
            // Update ticket
            ticket.recruits = recruitsEditText.text.toString().toInt()
            ticket.checkedIn = 1

            // Update database
            DBF.updateTicketArray(allTickets)

            // Update lists and UI
            updateTicketLists()
            autoSetRoleAmounts()
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
        val ticketId: TextView = dialogView.findViewById(R.id.ti_ticketId)
        ticketId.text = ticket.ticketId
        val playerId: TextView = dialogView.findViewById(R.id.ti_playerId)
        playerId.text = ticket.playerId ?: "No playerId found"
        val ticketNote: TextView = dialogView.findViewById(R.id.ti_Note)
        ticketNote.text = ticket.note
        val bookerPhone: TextView = dialogView.findViewById(R.id.ti_booker_phone)
        bookerPhone.text = ticket.bookerPhone
        val bookerName: TextView = dialogView.findViewById(R.id.ti_bookerName)
        bookerName.text = ticket.bookerName
        val bookerEmail: TextView = dialogView.findViewById(R.id.ti_bookerEmail)
        bookerEmail.text = ticket.bookerEmail
        val rematchButton: Button = dialogView.findViewById<Button>(R.id.ti_rematchButton)

        if (ticket.suggestions?.isEmpty() == false) {
            rematchButton.visibility = View.INVISIBLE
        } else {
            rematchButton.visibility = View.VISIBLE
        }

        // Close window
        dialogView.findViewById<Button>(R.id.ti_closeButton).setOnClickListener {
            playerInfoDialog.dismiss()
        }
        rematchButton.setOnClickListener {
            manualPlayerLink(ticket)
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
                DBF.updateTicketArray(allTickets)
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
        deselectTeamItem(true)
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
        ticket.teamColor = null
        DBF.updateTicketArray(allTickets)
        updateTicketLists()
    }


    private fun openWinnerPopup() {
        // Show popup
        val dialogView = LayoutInflater.from(context).inflate(R.layout.set_winner_popup, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val alertDialog = builder.show()

        // Set variables
        var currClickWinner = ""
        var currGameWinner = ""

        val clickNoWinner = dialogView.findViewById<Button>(R.id.sw_Win1NoWinner)
        val clickRedWinner = dialogView.findViewById<Button>(R.id.sw_Win1RedButton)
        val clickBlueWinner = dialogView.findViewById<Button>(R.id.sw_Win1BlueButton)
        val clickTieWinner = dialogView.findViewById<Button>(R.id.sw_Win1TieButton)

        val gameNoWinner = dialogView.findViewById<Button>(R.id.sw_Win2NoWinner)
        val gameRedWinner = dialogView.findViewById<Button>(R.id.sw_Win2RedButton)
        val gameBlueWinner = dialogView.findViewById<Button>(R.id.sw_Win2BlueButton)
        val gameTieWinner = dialogView.findViewById<Button>(R.id.sw_Win2TieButton)

        fun updateWinnerButtonColors() {

            clickNoWinner.apply {
                setBackgroundColor(if (currClickWinner == "") noWinnerColor else noWinnerUnselectedColor)
                elevation = if (currClickWinner == "") 10f else 0f
                alpha = if (currClickWinner == "") 1f else 0.7f
            }

            clickRedWinner.apply {
                setBackgroundColor(if (currClickWinner == "Red") redWinnerColor else redUnselectedColor)
                elevation = if (currClickWinner == "Red") 10f else 0f
                alpha = if (currClickWinner == "Red") 1f else 0.7f
            }

            clickBlueWinner.apply {
                setBackgroundColor(if (currClickWinner == "Blue") blueWinnerColor else blueUnselectedColor)
                elevation = if (currClickWinner == "Blue") 10f else 0f
                alpha = if (currClickWinner == "Blue") 1f else 0.7f
            }

            clickTieWinner.apply {
                setBackgroundColor(if (currClickWinner == "Tie") tieActiveColor else tieUnselectedColor)
                elevation = if (currClickWinner == "Tie") 10f else 0f
                alpha = if (currClickWinner == "Tie") 1f else 0.7f
            }

            gameNoWinner.apply {
                setBackgroundColor(if (currGameWinner == "") noWinnerColor else noWinnerUnselectedColor)
                elevation = if (currGameWinner == "") 10f else 0f
                alpha = if (currGameWinner == "") 1f else 0.7f
            }

            gameRedWinner.apply {
                setBackgroundColor(if (currGameWinner == "Red") redWinnerColor else redUnselectedColor)
                elevation = if (currGameWinner == "Red") 10f else 0f
                alpha = if (currGameWinner == "Red") 1f else 0.7f
            }

            gameBlueWinner.apply {
                setBackgroundColor(if (currGameWinner == "Blue") blueWinnerColor else blueUnselectedColor)
                elevation = if (currGameWinner == "Blue") 10f else 0f
                alpha = if (currGameWinner == "Blue") 1f else 0.7f
            }

            gameTieWinner.apply {
                setBackgroundColor(if (currGameWinner == "Tie") tieActiveColor else tieUnselectedColor)
                elevation = if (currGameWinner == "Tie") 10f else 0f
                alpha = if (currGameWinner == "Tie") 1f else 0.7f
            }
        }

        clickNoWinner.setOnClickListener {
            currClickWinner = ""
            updateWinnerButtonColors()
        }

        clickRedWinner.setOnClickListener {
            currClickWinner = "Red"
            updateWinnerButtonColors()
        }

        clickBlueWinner.setOnClickListener {
            currClickWinner = "Blue"
            updateWinnerButtonColors()
        }

        clickTieWinner.setOnClickListener {
            currClickWinner = "Tie"
            updateWinnerButtonColors()
        }

        gameNoWinner.setOnClickListener {
            currGameWinner = ""
            updateWinnerButtonColors()
        }

        gameRedWinner.setOnClickListener {
            currGameWinner = "Red"
            updateWinnerButtonColors()
        }

        gameBlueWinner.setOnClickListener {
            currGameWinner = "Blue"
            updateWinnerButtonColors()
        }

        gameTieWinner.setOnClickListener {
            currGameWinner = "Tie"
            updateWinnerButtonColors()
        }

        // Check for already established winners
        if (!event.clickWinner.isNullOrEmpty()) {
            currClickWinner = event.clickWinner
        }
        if (!event.gameWinner.isNullOrEmpty()) {
            currGameWinner = event.gameWinner
        }

//        // Auto-check for a game winner
//        currGameWinner = if (event.redGameWins != 0 || event.blueGameWins != 0){
//            if (event.redGameWins > event.blueGameWins) {
//                "Red"
//            } else if (event.blueGameWins > event.redGameWins) {
//                "Blue"
//            } else {
//                "Tie"
//            }
//        } else {
//            ""
//        }

        updateWinnerButtonColors()

        dialogView.findViewById<Button>(R.id.sw_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.sw_saveButton).setOnClickListener {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            event.clickWinner = currClickWinner
            event.gameWinner = currGameWinner
            DBF.updateData(event)
            eventDatabase.update(event)
            alertDialog.dismiss()
            checkGameEnded()
        }
    }


    fun autoSetRoleAmounts() {
        if (!autoRoleAmounts) return

        val redTeamSize = redTeam.size
        val blueTeamSize = blueTeam.size

        // Find the smaller team size
        val smallerTeamSize = min(redTeamSize, blueTeamSize)
        val max = 3

        val healerAmount = min(smallerTeamSize / 4, max)
        val mageAmount =
            min((smallerTeamSize / 4) + (if (smallerTeamSize % 4 > 1) 1 else 0), max)
        val rogueAmount =
            min((smallerTeamSize / 4) + (if (smallerTeamSize % 4 > 0) 1 else 0), max)
        val knightAmount =
            min((smallerTeamSize / 4) + (if (smallerTeamSize % 4 > 2) 1 else 0), max)

        // Set the TextView values
        binding.healerAmountValue.setText(healerAmount.toString())
        binding.mageAmountValue.setText(mageAmount.toString())
        binding.rogueAmountValue.setText(rogueAmount.toString())
        binding.knightAmountValue.setText(knightAmount.toString())
    }

    fun selectTicket(ticket: Ticket) {
        if (hasTeamItemSelected) {
            deselectTeamItem(false)
        }
        hasTeamItemSelected = true
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
        if (selectedTicket.checkedIn == 1) {
            selectedTicket.switchedTeams += 1
        }
        updateTicketLists()
        deselectTeamItem(true)
        selectedTicketTVH.select()
        autoSetRoleAmounts()

        // Update database
        DBF.updateTicketArray(allTickets)
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
            DBF.updateTicketArray(allTickets)
            ticketDatabase.update(ticket)

            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.an_cancelButton).setOnClickListener {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

// SORTING FUNCTIONS

    private fun sortAssignByMatched() {
        // Partition the list into ticket.suggestions being null and non-null
        val (nonNullSuggestions, nullSuggestions) = assignList.partition { it.suggestions != null && it.suggestions!!.isNotEmpty() }
        assignList =
            (nonNullSuggestions.sortedBy { it.suggestions?.firstOrNull()?.playerID } + nullSuggestions).toMutableList()
    }

    private fun sortAssignByName() {
        Log.i("tickets", "Sorting by name")
        assignList.sortBy { it.fullName }
    }

    private fun sortAssignByAge() {
        assignList.sortBy { it.age }
    }

    private fun sortAssignByGroup() {
        if (assignList.isEmpty()) {
            return
        }
        Log.i("tickets", "Sorting by group")
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

    private fun sortCheckInByGroup() {
        checkInList.sortBy { it.group }
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
            updateTicketStats(blueTeam)
            updateTicketStats(redTeam)
            updateRound(true)
            updateEventStatus()
            DBF.updateTicketArray(allTickets)
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
        // Step 1: Preprocessing and Initialization
        // Fetch the total number of each role from the user interface
        val originalRoleAmounts = arrayOf(
            binding.healerAmountValue.text.toString().toInt(),
            binding.rogueAmountValue.text.toString().toInt(),
            binding.mageAmountValue.text.toString().toInt(),
            binding.knightAmountValue.text.toString().toInt(),
            binding.specialAAmountValue.text.toString().toInt(),
            binding.specialBAmountValue.text.toString().toInt()
        )

        // Save the original total amount of roles to distribute
        val originalTotalAmount = originalRoleAmounts.sum()

        // Reset roles for all players in the team
        team.forEach {
            it.lastRole = it.currentRole
            it.currentRole = 7 // Default role (Warrior)
        }

        // Assign guaranteed roles
        var guarantees = 0
        val guaranteedTickets = team.filter { it.guaranteedRole in 1..6 }
        for (ticket in guaranteedTickets) {
            val role = ticket.guaranteedRole
            // If there are available roles of the guaranteed type, assign it to the ticket
            if (originalRoleAmounts[role - 1] > 0) {
                ticket.currentRole = role
                originalRoleAmounts[role - 1]--
                guarantees++
            } else Log.e(
                "pickTeamRoles",
                "Could not assign guaranteed role to ${ticket.firstName}"
            )
        }

        // Step 2: Prioritize Players
        // Sort and filter team down to a prioritized list
        val ticketAmount = originalTotalAmount - guarantees // Number of tickets to prioritize
        val prioritizedTickets = team
            .filter { it.currentRole == 7 } // Remove ticket whose guarantees succeeded
            .sortedWith(compareBy<Ticket> { it.lastRole == 7 }.reversed() // Put all with lastRole warrior first
                .thenBy { it.roundsSpecialRole }) // Sort both sides (lastRole warriors & specials) on rounds as special roles
            .take(ticketAmount) // Continue with only the prioritized amount of tickets

        // Log prioritized tickets' names
        val remainingTicketNames = prioritizedTickets.joinToString(", ") { it.firstName }
        Log.i("testRoles", "Prioritized tickets: $remainingTicketNames")

        // Step 3: Create Lottery Hats
        val originalLotteryHats =
            MutableList(originalRoleAmounts.size) { mutableListOf<Ticket>() }

        for (ticket in prioritizedTickets) {
            var hatAmounts = 0 // Store amount of hats ticket is put in

            for (i in originalRoleAmounts.indices) { // For each role

                // Store how many rounds the ticket has been the role
                val roundsInRole = when (i) {
                    0 -> ticket.roundsHealer
                    1 -> ticket.roundsRogue
                    2 -> ticket.roundsMage
                    3 -> ticket.roundsKnight
                    else -> 0 // Special A and Special B do not matter
                }

                // Check how many rounds the ticket is allowed to play this role
                val roundsList = listOf(
                    ticket.roundsHealer,
                    ticket.roundsRogue,
                    ticket.roundsMage,
                    ticket.roundsKnight
                )
                var maxRounds = roundsList.maxOrNull() ?: 0

                // If all have been played the same amount of times, increment max amount
                if (roundsList.all { it == maxRounds }) {
                    maxRounds++
                }

                // Add ticket to the lotteryHat if they have played fewer than maxRounds in the role
                // and they did not play the same role last round
                if (roundsInRole < maxRounds && ticket.lastRole != i + 1) {
                    originalLotteryHats[i].add(ticket) // Add ticket to hat
                    hatAmounts++ // Increment amount of hats ticket is in
                    Log.i(
                        "${ticket.firstName}",
                        "${ticket.firstName} was entered to hat $i \nroundsInRole: $roundsInRole should be less than maxRounds: $maxRounds,  lastRole: ${ticket.lastRole}"
                    )
                } else {
                    Log.i(
                        "${ticket.firstName}",
                        "${ticket.firstName} skipped hat $i \nroundsInRole: $roundsInRole should be less than maxRounds: $maxRounds,  lastRole: ${ticket.lastRole}"
                    )
                }

            }

            // Check if ticket was placed in 0 hats
            if (hatAmounts == 0) {
                Log.e("pickTeamRoles", "Could not enter ${ticket.firstName} into any hat")
            }
//            else Log.i("pickTeamRoles", "Entered ${ticket.firstName} into $hatAmounts hats")
        }

        // Check if any hat is empty
        if (originalLotteryHats.any { it.isEmpty() }) {
            Log.e("pickTeamRoles", "At least one hat is empty.")
        } else {
            val roleSizes = originalLotteryHats.map { it.size }
        }

        // Step 4: Assign Roles
        val assignmentAttempts = 20
        var remainingRoles = arrayOf<Int>()
        for (attempt in 1..assignmentAttempts) {

            // Reset all roles and role amounts before each attempt
            prioritizedTickets.forEach {
                it.currentRole = 7 // Default role (Warrior)
            }

            val roleAmounts = originalRoleAmounts.clone()
            val temporaryLotteryHats = originalLotteryHats.map { it.toMutableList() }

            // Compute a map from tickets to counts
            val hatCounts = mutableMapOf<Ticket, Int>()
            for (ticket in prioritizedTickets) {
                var count = 0
                for (hat in temporaryLotteryHats) {
                    if (ticket in hat) count++
                }
                hatCounts[ticket] = count
            }

            // Sort the tickets based on the counts in ascending order
            val sortedTickets = prioritizedTickets.sortedBy { hatCounts[it] }

            // Log the order of sortedTickets
            val sortedTicketNames =
                sortedTickets.joinToString(", ") { it.firstName + " (hats: " + hatCounts[it] + ")" }

            // Assign roles to players based on the sorted tickets
            for (ticket in sortedTickets.indices) {
                val minRoleTicket = sortedTickets[ticket]

//                LogHatsForTicket(minRoleTicket, temporaryLotteryHats)

                // Find the hat with the smallest size that the ticket is in
                val minSizeHatIndex = temporaryLotteryHats.withIndex()
                    .filter { (index, hat) -> minRoleTicket in hat && roleAmounts[index] > 0 }
                    .minByOrNull { (_, hat) -> hat.size }?.index ?: continue

                minRoleTicket.currentRole = minSizeHatIndex + 1
                roleAmounts[minSizeHatIndex]--

                // Remove this ticket from all other lotteryHats to prevent duplicate assignments
                temporaryLotteryHats.forEach { it.remove(minRoleTicket) }

                if (roleAmounts[minSizeHatIndex] == 0) {
                    // Remove this role from all remaining tickets' roles
                    sortedTickets.forEach { ticket ->
                        if (ticket in temporaryLotteryHats[minSizeHatIndex]) temporaryLotteryHats[minSizeHatIndex].remove(
                            ticket
                        )
                    }
                }
            }

            // Check if any roleAmounts are left
            if (roleAmounts.any { it > 0 }) {
                Log.e("pickTeamRoles", "Could not assign all roles on attempt $attempt.")
                if (attempt == assignmentAttempts) {
                    Log.e("pickTeamRoles", "Continuing to fallback...")
                    remainingRoles = roleAmounts.map { it }.toTypedArray()
                }
            } else {
                Log.i("pickTeamRoles", "Roles assigned successfully on attempt $attempt")
                break
            }
        }

        // Fallback mechanism: Assign any remaining roles without any regard to previous roles
        if (remainingRoles.any { it > 0 }) {
            val lastTickets = team.filter { it.currentRole == 7 }
            lastTickets.forEach { ticket ->
                for (i in remainingRoles.indices) {
                    if (remainingRoles[i] > 0) {
                        Log.i(
                            "pickTeamRoles",
                            "Fallback-setting ${ticket.firstName} to role ${i + 1}"
                        )
                        ticket.currentRole = i + 1
                        remainingRoles[i]--
                        break
                    }
                }
            }
        }

        // Step 5: Check Role Assignment
        // Check if all roles have been successfully assigned
        val rolesDistributed = team.count { it.currentRole != 7 }
        if (rolesDistributed != originalTotalAmount) {
            // If not all roles were distributed, return false and handle the error
            Log.e(
                "pickTeamRoles",
                "Not all roles could be distributed! originalTotalAmount: $originalTotalAmount rolesDistributed: $rolesDistributed"
            )
            return false
        }

        Log.i("Success", "Success!")
        // Update the database, sort the tickets, update the UI, and return true
        DBF.updateTicketArray(allTickets)
        teamSorting = 2
        updateTicketLists()
        updateBottomPanel(0)
        return true
    }

    private fun updateTicketStats(team: MutableList<Ticket>) {
        for (ticket in team) {
            if (ticket.currentRole != 7) {
                ticket.roundsSpecialRole = ticket.roundsSpecialRole + 1
                when (ticket.currentRole) {
                    1 -> ticket.roundsHealer = ticket.roundsHealer + 1
                    2 -> ticket.roundsRogue = ticket.roundsRogue + 1
                    3 -> ticket.roundsMage = ticket.roundsMage + 1
                    4 -> ticket.roundsKnight = ticket.roundsKnight + 1
                }
            } else ticket.roundsWarrior = ticket.roundsWarrior + 1

            if (ticket.currentRole == ticket.guaranteedRole) {
                ticket.guaranteedRole = 0
            }
        }
    }

    private fun updateRound(increase: Boolean) {
        if (increase) event.round = event.round + 1 else event.round = event.round - 1
        binding.roundText.text = event.round.toString()
        DBF.updateData(event)
        eventDatabase.update(event)
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
        val noteAButton = dialogView.findViewById<Button>(R.id.noteAButton)
        val noteBButton = dialogView.findViewById<Button>(R.id.noteBButton)
        noteBButton.visibility = View.GONE

        noteAButton.setOnClickListener {
            notification.dismiss()
        }
    }

    private fun callChoice(
        message: String,
        buttonAText: String,
        buttonBText: String,
        aFunction: () -> Unit,
        bFunction: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.note_popup, null)

        val builder = AlertDialog.Builder(context).setView(dialogView)

        val notification = builder.show()

        val textHolder: TextView = dialogView.findViewById(R.id.notePopupText)
        val noteAButton = dialogView.findViewById<Button>(R.id.noteAButton)
        val noteBButton = dialogView.findViewById<Button>(R.id.noteBButton)

        textHolder.text = message
        noteAButton.text = buttonAText
        noteBButton.text = buttonBText
        noteBButton.visibility = View.VISIBLE

        noteAButton.setOnClickListener {
            aFunction()
            notification.dismiss()
        }

        noteBButton.setOnClickListener {
            bFunction()
            notification.dismiss()
        }
    }

    private fun benchTicket() {
        if (selectedTicket.benched == 0) {
            selectedTicket.benched = 1
        } else {
            selectedTicket.benched = 0
        }

        //Check if someone in ticket's team is warrior
        var someoneIsWarrior = false
        val team = if (selectedTicket.teamColor == "Red") redTeam else blueTeam
        for (ticket in team) {
            if (ticket.currentRole == 7) {
                someoneIsWarrior = true
                break
            }
        }
        if (!winnerPanel && event.round > 0 && someoneIsWarrior && selectedTicket.currentRole != 7) {
            callChoice(
                "Do you want to randomize the role to someone else?",
                "No",
                "Yes",
                {},
                { randomizeRoleToTeam() })
        }

        DBF.updateTicketArray(allTickets)
        updateTicketLists()
        autoSetRoleAmounts()
    }

    private fun randomizeRoleToTeam() {
        val team = if (selectedTicket.teamColor == "Red") redTeam else blueTeam

        // Get a list of warriors
        val warriors: MutableList<Ticket> = mutableListOf()
        for (ticket in team) {
            if (ticket.currentRole == 7) {
                warriors.add(ticket)
            }
        }

        // Pick the one who's been a special role the least amount of times
        warriors.sortedBy { it.roundsSpecialRole }
        val chosenWarrior = warriors[0]

        // Notify game master who gets the role
        val role = when (selectedTicket.currentRole) {
            1 -> "Helare"
            2 -> "Odåga"
            3 -> "Magiker"
            4 -> "Riddare"
            5 -> "SpecialA"
            6 -> "SpecialB"
            else -> "Okänd"
        }

        callNotification("${chosenWarrior.fullName}\nNew Role:\n$role")

        // Set both benched ticket and new ticket as their new roles
        chosenWarrior.currentRole = selectedTicket.currentRole
        selectedTicket.currentRole = 7

        // Update lists
        updateTicketLists()
    }

    private fun pressLayoutFunction() {
        dismissKeyboard()
        deselectTeamItem(true)
    }

    private fun checkGameEnded() {
        Log.i("eventWin", "Event's winners: ${event.gameWinner}, ${event.clickWinner}")
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

    private fun updateEventStatus() {
        if (allTickets.size < 1) return

        var ticketTeamDivision = false
        var ticketCheckIn = false

        for (ticket in allTickets) {
            if (ticket.teamColor != "") {
                ticketTeamDivision = true
            }

            if (ticket.checkedIn == 1) {
                ticketCheckIn = true
            }

            if (ticketTeamDivision && ticketCheckIn) break // if both conditions are met, break the loop
        }

        event.status = when {
            event.clickWinner != "" && event.gameWinner != "" -> "Avslutat"
            event.round > 0 -> "Spel påbörjat"
            ticketCheckIn -> "Checkar in"
            ticketTeamDivision -> "Lagindelning"
            else -> "Ej påbörjat"
        }
        DBF.updateData(event)
        eventDatabase.update(event)
        Log.i("status", "Event Status: ${event.status}")
    }

    private fun updatePlayerFromTicket(player: Player, ticket: Ticket) {
        player.firstName = ticket.firstName
        player.lastName = ticket.lastName
        player.age = ticket.age
    }

    fun checkConnection() {
        if (currActivity.connectionProblems || !isInternetAvailable()) {
            binding.evNoConnection.visibility = View.VISIBLE
            binding.levelUpButton.visibility = View.GONE
            binding.playerExpText.text = "Connection Problems\nCan't level up"
            binding.playerExpText.visibility = View.VISIBLE
        } else {
            binding.evNoConnection.visibility = View.GONE
            binding.levelUpButton.visibility = View.VISIBLE
            binding.playerExpText.visibility = View.GONE
            resetLocalDatabase()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    private fun resetLocalDatabase() {
        Log.i("Connection", "Resetting Local Database")
        // Save players in this Event
        val players: MutableList<Player> = mutableListOf()
        for (ticket in allTickets) {
            if (ticket.playerId == null || ticket.playerId == "" || ticket.playerId == "null") continue
            val player = playerDatabase.getById(ticket.playerId!!)
            if (player != null) {
                players.add(player)
            }
        }

        // Clear databases
        ticketDatabase.clearCache()
        playerDatabase.clearCache()
        eventDatabase.clearCache()

        // Add this event's content to local database
        eventDatabase.insert(event)

        for (player in players) {
            playerDatabase.insert(player)
        }

        for (ticket in allTickets) {
            ticketDatabase.insert(ticket)
        }
    }

    private fun roll20rounds() {
        val outerRounds = 20
        var h = outerRounds
        while (h > 0) {
            val rounds = 10
            var i = rounds
            while (i > 0) {
                Log.i("pickTeamRoles", "OuterRound $h. Starting round $i")
                pickTeamRoles(redTeam)
                updateTicketStats(redTeam)
                redTeam.forEach { ticket ->
                    if (ticket.lastRole == ticket.currentRole) {
                        Log.i(
                            "rolesTwice",
                            "${ticket.firstName} was role ${ticket.currentRole} twice in a row"
                        )
                    }
                    val roles =
                        "H:${ticket.roundsHealer}, R:${ticket.roundsRogue}, M:${ticket.roundsMage}, K:${ticket.roundsKnight}, W:${ticket.roundsWarrior}"
                    Log.i("testRoles", "${ticket.firstName} $roles")
                }
                i--
            }
            resetRoles()
            h--
        }
    }

    private fun openKlippkortsLink() {
        val url =
            "https://docs.google.com/spreadsheets/d/1PIfFF9iT4sxmK3O-rSVmMrUyKLagrbhoMCcDHAdbJF8/edit#gid=0"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun resetRoles() {
        event.round = 0
        event.clickWinner = ""
        event.gameWinner = ""
        event.blueGameWins = 0
        event.redGameWins = 0
        checkGameEnded()
        allTickets.forEach {
            it.roundsHealer = 0
            it.roundsRogue = 0
            it.roundsMage = 0
            it.roundsKnight = 0
            it.roundsWarrior = 0
            it.roundsSpecialRole = 0
            it.guaranteedRole = 0
//            it.benched = 0
            it.currentRole = 0
            it.lastRole = 0
        }
        updateGameWins()
        binding.roundText.text = event.round.toString()
        updateTicketLists()
        DBF.updateTicketArray(allTickets)
    }

    private fun updateGameWinButtons() {
        binding.gameWinBlue.apply {
            setBackgroundColor(if (gameWinner == "Blue") blueWinnerColor else blueUnselectedColor)
            elevation = if (gameWinner == "Blue") 10f else 0f
            alpha = if (gameWinner == "Blue") 1f else 0.7f
        }
        binding.gameWinRed.apply {
            setBackgroundColor(if (gameWinner == "Red") redWinnerColor else redUnselectedColor)
            elevation = if (gameWinner == "Red") 10f else 0f
            alpha = if (gameWinner == "Red") 1f else 0.7f
        }
        binding.gameWinTie.apply {
            setBackgroundColor(if (gameWinner == "Tie") tieActiveColor else tieUnselectedColor)
            elevation = if (gameWinner == "Tie") 10f else 0f
            alpha = if (gameWinner == "Tie") 1f else 0.7f
        }
        binding.gameWinAccept.isEnabled = true
    }

    private fun setRoundWinner() {
        if (gameWinner == "") return
        winnerPanel = false
        updateBottomPanel(0)
        when (gameWinner) {
            "Red" -> {
                event.redGameWins += 1
            }

            "Blue" -> {
                event.blueGameWins += 1
            }

            "Tie" -> {
                event.redGameWins += 1
                event.blueGameWins += 1
            }
        }
        gameWinner = ""
        binding.gameWinAccept.isEnabled = false
        updateGameWins()
        updateGameWinButtons()
    }

    private fun updateGameWins() {
        binding.redWinsValue.text = event.redGameWins.toString()
        binding.blueWinsValue.text = event.blueGameWins.toString()
        DBF.updateData(event)
        eventDatabase.update(event)
    }

    fun goToLevelUp() {
        Log.i(
            "test",
            "Kom in med ${selectedTicket.firstName} playerId: ${selectedTicket.playerId}"
        )
        findNavController().navigate(
            EventViewDirections.actionEventViewToLevelUpFragment(
                selectedTicket.playerId!!,
                event.eventId,
                selectedTicket.ticketId
            )
        )
    }

    fun updateBottomPanel(setState: Int) {
        // State 0 = BottomPanel
        // State 1 = New Round
        // State 2 = Team Item Click
        when (setState) {
            1 -> {
                bottomPanel.visibility = View.GONE
                bottomPanelNewRound.visibility = View.VISIBLE
                playerRoleButtonPanel.visibility = View.INVISIBLE
            }

            2 -> {
                bottomPanel.visibility = View.GONE
                if (bottomPanelNewRound.visibility == View.VISIBLE) {
                    playerRoleButtonPanel.visibility = View.VISIBLE
                    // Set team button color
                    val color =
                        if (selectedTicket.teamColor == "Blue") R.color.teamRedColor else R.color.teamBlueColor
                    context?.resources?.let {
                        binding.switchTeamButton2.setBackgroundColor(
                            it.getColor(color)
                        )
                    }
                } else {
                    bottomPanelSetWinner.visibility = View.GONE
                    bottomPanelPlayer.visibility = View.VISIBLE
                    binding.playerNameText.text = selectedTicket.fullName

                    if (selectedTicket.teamColor == "Blue") {
                        context?.resources?.let {
                            binding.switchTeamButton.setBackgroundColor(
                                it.getColor(
                                    R.color.teamBlueColor
                                )
                            )
                        }
                    } else {
                        context?.resources?.let {
                            binding.switchTeamButton.setBackgroundColor(
                                it.getColor(
                                    R.color.teamRedColor
                                )
                            )
                        }
                    }

                    if (selectedTicket.playerId == null || selectedTicket.playerId == "") {
                        binding.levelUpButton.visibility = View.GONE
                        binding.playerExpText.visibility = View.VISIBLE
                        binding.playerExpText.text = "No playerId found\nMatch via INFO button!"
                    } else if (!currActivity.connectionProblems) {
                        binding.levelUpButton.visibility = View.VISIBLE
                        binding.playerExpText.visibility = View.GONE
                    }

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

            else -> {
                if (winnerPanel) {
                    bottomPanel.visibility = View.GONE
                    bottomPanelSetWinner.visibility = View.VISIBLE
                } else {
                    bottomPanel.visibility = View.VISIBLE
                    bottomPanelSetWinner.visibility = View.GONE
                }
                bottomPanelNewRound.visibility = View.GONE
                bottomPanelPlayer.visibility = View.GONE
                playerRoleButtonPanel.visibility = View.INVISIBLE
            }
        }
    }
}