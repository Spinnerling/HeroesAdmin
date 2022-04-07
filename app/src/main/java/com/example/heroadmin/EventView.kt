package com.example.heroadmin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    private lateinit var redTeamPowerText : TextView
    private lateinit var blueTeamPowerText : TextView
    private lateinit var redTeamAmountText : TextView
    private lateinit var blueTeamAmountText : TextView
    private lateinit var redTeamTeensText : TextView
    private lateinit var blueTeamTeensText : TextView
    private lateinit var redTeamTiniesText : TextView
    private lateinit var blueTeamTiniesText : TextView
    lateinit var selectedTicket : Ticket
    private lateinit var selectedPlayer : Player
    private lateinit var playerOnOffSwitch : Switch
    lateinit var selectedTicketTVH : TeamViewHolder
    private lateinit var playerExpText : TextView
    private lateinit var bottomPanel : LinearLayout
    private lateinit var bottomPanelPlayer : LinearLayout
    private lateinit var bottomPanelNewRound : LinearLayout
    private lateinit var playerRoleButtonPanel : LinearLayout
    private var healerAmount : Int = 0
    private var rogueAmount : Int = 0
    private var mageAmount : Int = 0
    private var knightAmount : Int = 0
    private var specialAAmount : Int = 0
    private var specialBAmount : Int = 0
    private var firstPlayerSelected : Boolean = false

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

        // Set variables
        eventInfoDate.text = "Date: ${event.actualDate}"
        eventInfoTime.text = "Start: ${event.actualStartTime}"
        eventInfoVenue.text = "Venue: ${event.venue}"
        eventInfoPlayerAmount.text = "Tickets: ${allTickets.size} / ${event.playerMax}"


        updateTicketLists()
        autoSetRoleAmounts()

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
            deselectPlayer()
        }

        newRoundButton.setOnClickListener{
            deselectPlayer()
            bottomPanel.visibility = View.GONE
            bottomPanelNewRound.visibility = View.VISIBLE

        }

        cancelNewRoundButton.setOnClickListener{
            deselectPlayer()
            bottomPanel.visibility = View.VISIBLE
            bottomPanelNewRound.visibility = View.GONE
            playerRoleButtonPanel.visibility = View.INVISIBLE
        }

        switchTeamButton.setOnClickListener{
            switchTeam()
        }

        switchTeamButton2.setOnClickListener{
            switchTeam()
        }

        spendExpButton.setOnClickListener{
            findNavController().navigate(EventViewDirections.actionEventViewToLevelUpFragment(selectedTicket.playerId))
        }

        binding.ticketInfoButton.setOnClickListener{
            openTicketInfo()
        }

        binding.rollRoundButton2.setOnClickListener{
            randomizeRoles()
        }
    }

    private fun deselectPlayer() {
        if (!firstPlayerSelected){return}
        if (bottomPanelPlayer.visibility == View.VISIBLE){
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
        redTeamAdapter = TeamRecyclerAdapter(redTeam, { position -> onTeamItemClick(position)}, this)
        blueTeamAdapter = TeamRecyclerAdapter(blueTeam, { position -> onTeamItemClick(position)}, this)
        redBenchAdapter = TeamRecyclerAdapter(redBench, { position -> onTeamItemClick(position)}, this)
        blueBenchAdapter = TeamRecyclerAdapter(blueBench, { position -> onTeamItemClick(position)}, this)

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
        if (!firstPlayerSelected){
            deselectPlayer()
        }
        selectedPlayer = getPlayer(selectedTicket.ticketId)

        if (bottomPanelNewRound.visibility == View.VISIBLE){
            playerRoleButtonPanel.visibility = View.VISIBLE
        }
        else {
            binding.bottomPanel.visibility = View.GONE
            binding.bottomPanelPlayer.visibility = View.VISIBLE
            binding.playerNameText.text = selectedTicket.fullName
            playerExpText.text = "${selectedPlayer.totalExp} EXP kvar"
            val roleInText = getRoleByNumber(selectedTicket.currentRole)
            binding.ticketRoleText.text = roleInText
            playerOnOffSwitch.isChecked = !selectedTicket.benched
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

    fun setTicketTabardNumber(ticket : Ticket) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.checkin_popup,null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)

        val alertDialog = builder.show()
        val name : TextView = dialogView.findViewById<TextView>(R.id.checkInPopupNameText)
        name.text = ticket.fullName
        val userNo = dialogView.findViewById<EditText>(R.id.checkInPopupEditText)
        userNo.requestFocus()

        dialogView.findViewById<Button>(R.id.checkinAcceptButton).setOnClickListener{
            val number = userNo.text.toString()
            if (number != ""){
                ticket.tabardNr = number.toInt()
                ticket.checkedIn = true;
                updateTicketLists()

                alertDialog.dismiss()
            }
        }
        dialogView.findViewById<Button>(R.id.checkinCancelButton).setOnClickListener{
            Toast.makeText(context,"Cancelled",Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    fun openTicketInfo() {
        var ticket = selectedTicket
        val dialogView = LayoutInflater.from(context).inflate(R.layout.ticket_info,null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val playerInfoDialog = builder.show()

        // Fill with info
        val name : TextView = dialogView.findViewById<TextView>(R.id.ti_playerName)
        name.text = ticket.fullName
        val userAge = dialogView.findViewById<TextView>(R.id.ti_playerAge)
        userAge.text = ticket.age.toString()
        val userId = dialogView.findViewById<TextView>(R.id.ti_playerUserId)
        userId.text = ticket.playerId
        val ticketNote = dialogView.findViewById<TextView>(R.id.ti_Note)
        ticketNote.text = ticket.note
        val guardianName = dialogView.findViewById<TextView>(R.id.ti_guardianName)
        guardianName.text = ticket.guardianFullName
        val guardianPhone = dialogView.findViewById<TextView>(R.id.ti_guardianPhone)
        guardianPhone.text = ticket.guardianPhoneNr
        val guardianEmail = dialogView.findViewById<TextView>(R.id.ti_guardianEmail)
        guardianEmail.text = ticket.guardianEmail
        val bookerName = dialogView.findViewById<TextView>(R.id.ti_bookerName)
        bookerName.text = ticket.bookerFullName
        val bookerPhone = dialogView.findViewById<TextView>(R.id.ti_bookerPhone)
        bookerPhone.text = ticket.bookerPhoneNr
        val bookerEmail = dialogView.findViewById<TextView>(R.id.ti_bookerEmail)
        bookerEmail.text = ticket.bookerEmail

        // Close window
        dialogView.findViewById<Button>(R.id.ti_closeButton).setOnClickListener{
            playerInfoDialog.dismiss()
        }
    }

    fun autoSetRoleAmounts() {
        if (allPlayers.isEmpty()) {
            return
        }

        healerAmount = allPlayers.size / 16
        mageAmount = (allPlayers.size + 4) / 16
        rogueAmount = (allPlayers.size + 12) / 16
        knightAmount = (allPlayers.size + 8) / 16
        binding.healerAmountValue.setText(healerAmount.toString())
        binding.mageAmountValue.setText(mageAmount.toString())
        binding.rogueAmountValue.setText(rogueAmount.toString())
        binding.knightAmountValue.setText(knightAmount.toString())
    }

    fun selectTicket(ticket : Ticket) {
        if (firstPlayerSelected) { deselectPlayer() }
        firstPlayerSelected = true;
        ticket.selected = true
        selectedTicket = ticket
    }

    fun switchTeam(){
        selectedTicket.teamColor = "None"
        updateTicketLists()
        deselectPlayer()
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

    private fun randomizeRoles() {
        pickTeamRoles(redTeam)
        pickTeamRoles(blueTeam)
    }

    private fun pickTeamRoles(team: MutableList<Ticket>) {
        healerAmount = binding.healerAmountValue.text.toString().toInt()
        rogueAmount = binding.rogueAmountValue.text.toString().toInt()
        mageAmount = binding.mageAmountValue.text.toString().toInt()
        knightAmount = binding.knightAmountValue.text.toString().toInt()
        specialAAmount = binding.specialAAmountValue.text.toString().toInt()
        specialBAmount = binding.specialBAmountValue.text.toString().toInt()

        var finishedHealers = mutableListOf<Ticket>()
        var finishedrogue = mutableListOf<Ticket>()
        var finishedmage = mutableListOf<Ticket>()
        var finishedknight = mutableListOf<Ticket>()
        var finishedspecialA = mutableListOf<Ticket>()
        var finishedspecialB = mutableListOf<Ticket>()


        fun setRole(ticket: Ticket, role : Int){
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

        val thirdList = mutableListOf<Ticket>()

        val totalAmount = healerAmount + rogueAmount + mageAmount + knightAmount + specialAAmount + specialBAmount
        team.sortBy { it.roundsSpecialRole }
        val secondList = team.slice(0.. totalAmount).toList()

        for (ticket in secondList) {
            if (ticket.guaranteedRole != 0){
                setRole(ticket, ticket.guaranteedRole)
            }
            else{
                thirdList.add(ticket)
            }
        }
        // Repeat
        var tempTotal = 0
        var loops = 0

        while (tempTotal != totalAmount) {
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


            for (ticket in thirdList) {
                if (ticket.roundsHealer < ticket.allowedTimesPerRole) {
                    tempHealers.add(ticket)
                    Log.i("test", "Put in tempHealer")
                }
                if (ticket.roundsRogue < ticket.allowedTimesPerRole) {
                    temprogue.add(ticket)
                    Log.i("test", "Put in temprogue")
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
            tempHealers.shuffle()
            temprogue.shuffle()
            tempmage.shuffle()
            tempknight.shuffle()
            tempspecialA.shuffle()
            tempspecialB.shuffle()

            val pickedPlayerList = mutableListOf<Ticket>()

            for (ticket in tempHealers){
                if (finishedHealers.size < healerAmount && !pickedPlayerList.contains(ticket)){
                    pickedPlayerList.add(ticket)
                    finishedHealers.add(ticket)
                }
            }
            Log.i("test", finishedHealers.size.toString() + " in healers")
            for (ticket in temprogue){
                if (finishedrogue.size < rogueAmount && !pickedPlayerList.contains(ticket)){
                    pickedPlayerList.add(ticket)
                    finishedrogue.add(ticket)
                }
            }
            Log.i("test", finishedrogue.size.toString() + " in rogues")
            for (ticket in tempmage){
                if (finishedmage.size < mageAmount && !pickedPlayerList.contains(ticket)){
                    pickedPlayerList.add(ticket)
                    finishedmage.add(ticket)
                }
            }
            Log.i("test", finishedmage.size.toString() + " in mages")
            for (ticket in tempknight){
                if (finishedknight.size < knightAmount && !pickedPlayerList.contains(ticket)){
                    pickedPlayerList.add(ticket)
                    finishedknight.add(ticket)
                }
            }
            Log.i("test", finishedknight.size.toString() + " in knights")
            for (ticket in tempspecialA){
                if (finishedspecialA.size < specialAAmount && !pickedPlayerList.contains(ticket)){
                    pickedPlayerList.add(ticket)
                    finishedspecialA.add(ticket)
                }
            }
            for (ticket in tempspecialB){
                if (finishedspecialB.size < specialBAmount && !pickedPlayerList.contains(ticket)){
                    pickedPlayerList.add(ticket)
                    finishedspecialB.add(ticket)
                }
            }
            tempTotal = finishedHealers.size + finishedrogue.size + finishedmage.size + finishedknight.size + finishedspecialA.size + finishedspecialB.size
            loops++
            if (loops > 99){
                Log.i("test", "Pick Roles function looped 100 times without finding a match!")
                break
            }
        }

        for (ticket in finishedHealers){
            ticket.currentRole = 1
            Log.i("test", ticket.firstName + " got healer")
        }
        for (ticket in finishedrogue){
            ticket.currentRole = 2
            Log.i("test", ticket.firstName + " got rogue")
        }
        for (ticket in finishedmage){
            ticket.currentRole = 3
            Log.i("test", ticket.firstName + " got mage")
        }
        for (ticket in finishedknight){
            ticket.currentRole = 4
            Log.i("test", ticket.firstName + " got knight")
        }
        for (ticket in finishedspecialA){
            ticket.currentRole = 5
            Log.i("test", ticket.firstName + " got specialA")
        }
        for (ticket in finishedspecialB){
            ticket.currentRole = 6
            Log.i("test", ticket.firstName + " got specialB")
        }

        for (ticket in team){
            if (ticket.roundsHealer == ticket.roundsKnight && ticket.roundsHealer == ticket.roundsMage && ticket.roundsHealer == ticket.roundsRogue){
                ticket.allowedTimesPerRole++
            }
        }

        updateTicketLists()
        Log.i("test", "Pick Roles done!")

    }



    // For each team:
    // Get the amount of special roles
    // Get the next players in line to be special
    // If any of the players have a guaranteed role, set it as the role and remove one from the amount to be picked as that role. Also remove the person from the players list.

    //Repeat:
        // Put each player in the role lists they're allowed to be
        // Shuffle each rolelist
        // Pick out players who have not already been picked
        // Check if all roles have correct amount, otherwise rinse & repeat.

    // If a player has been all special roles an equal amount, increase the amount of times they can be special
    // Set role to player's currRole
}