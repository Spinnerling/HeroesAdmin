package com.example.heroadmin

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject

fun apiBaseCall (context : Context?, method : Int, url : String) : JSONObject{
    val response : JSONObject = JSONObject()

    val jsonObjectRequest = JsonObjectRequest(
        method, url, null,
        { response: JSONObject ->

        },
        { error ->
            Log.i("test", "Failed api call to: " + url)
            Log.i("test", "Error: ".format(error.toString()))
        }
    )

    context.let {
        if (it != null) {
            MySingleton.getInstance(it).addToRequestQueue(jsonObjectRequest)
        }
    }
    return response
}

fun getTicket(ticketId: String): Ticket {

    // Find ticket in database by ticketId, return an array of its contents
    // val arrayContents = [insert code here]

    // Placeholder "found" ticket
    val arrayContents = mutableListOf(
        "ticket123",
        "12345",
        "Bob",
        "Gold",
        16,
        false,
        "None",
        false,
        0,
        false,
        0,
        0,
        "Lena",
        "Fagerlös",
        "070 123 45 67",
        "asdasdasd@email.com",
        "Booker",
        "Person",
        "498 456 12 12",
        "elmailo@adress.end",
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
        "This is a note of much worthy notingness where you really should read the thing that is says."
    )

    // Place the contents of array into a Ticket class

    return Ticket(
        arrayContents[0] as String,    // ticketId
        arrayContents[1] as String,    // playerId
        arrayContents[2] as String,    // first name
        arrayContents[3] as String,    // last name
        arrayContents[4] as Int,       // age
        arrayContents[5] as Boolean,   // Checked in
        arrayContents[6] as String,    // Team Color
        arrayContents[7] as Boolean,   // Costume
        arrayContents[8] as Int,       // Recruits
        arrayContents[9] as Boolean,   // Benched
        arrayContents[10] as Int,      // Current Role
        arrayContents[11] as Int,      // Last Role
        arrayContents[12] as String,   // Contact First name
        arrayContents[13] as String,   // Contact Last Name
        arrayContents[14] as String,   // Contact Phone
        arrayContents[15] as String,   // Contact Email
        arrayContents[16] as String,   // Booker First Name
        arrayContents[17] as String,   // Booker Last Name
        arrayContents[18] as String,   // Booker Phone
        arrayContents[19] as String,   // Booker Email
        arrayContents[20] as Int,      // Rounds as Healer
        arrayContents[21] as Int,      // Rounds as Mage
        arrayContents[22] as Int,      // Rounds as Rogue
        arrayContents[23] as Int,      // Rounds as Knight
        arrayContents[24] as Int,      // Rounds as Warrior
        arrayContents[25] as Int,      // Rounds as Special
        arrayContents[26] as Int,      // Respawns left
        arrayContents[27] as Int,      // Guaranteed Role
        arrayContents[28] as Int,      // EXP personal
        arrayContents[29] as Int,      // Tabard number
        arrayContents[30] as String,   // Note
    )
}



fun getEventIds(context : Context?): List<String> {
    // Find array of events in database

    val url = "https://www.talltales.nu/API/api/read.php"
    var list = mutableListOf<String>()
    val jsonObjectRequest = JsonObjectRequest(
        Request.Method.GET, url, null,
        { response ->
            list = getEventArray(response)
        },
        { error ->
            Log.i("test", "Failed: ".format(error.toString()))
        }
    )

    context.let {
        if (it != null) {
            MySingleton.getInstance(it).addToRequestQueue(jsonObjectRequest)
        }
    }
    return list
}

private fun getEventArray(response: JSONObject?) : MutableList<String> {
    val eventIdArray = mutableListOf<String>()
    val array : JSONArray = response!!.getJSONArray("data")
    val length = array.length()
    if (length > 0){
        var index = 0
        while (index < length){
            val item = array.getJSONObject(index)
            val id = item.getString("id")
            Log.i("test", id + " is the id")
            eventIdArray.add(id)
            index++
        }
    }
    return eventIdArray
}

fun getEvent(eventId: String): Event {
    // Find event in database by eventId, return an array of its contents
    // var arrayContents = [insert code here]

    // Placeholder "found" event
    val arrayContents = mutableListOf("A123", "Stockholm", "24/3 -22", "2020", "2021", "15:09", "IN PLAY", "", "Torsdagsspel", "Kom och programmera, din blötvattensfisk!",
        listOf("ticket123", "ticket234", "ticket345", "ticket567", "ticket678", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789", "ticket789")
        ,0, 32,35,0, 0,10,2,5 )

    return Event(
        arrayContents[0] as String,         // eventId
        arrayContents[1] as String,         // venue
        arrayContents[2] as String,         // actualDate
        arrayContents[3] as String,         // startTime
        arrayContents[4] as String,         // endTime
        arrayContents[5] as String,         // actualStartTime
        arrayContents[6] as String,         // status
        arrayContents[7] as String,         // reportText
        arrayContents[8] as String,         // title
        arrayContents[9] as String,         // description
        arrayContents[10] as List<String>,   // ticketId array
        arrayContents[11] as Int,            // round
        arrayContents[12] as Int,            // player amount
        arrayContents[13] as Int,            // player max amount
        arrayContents[14] as Int,            // Blue team EXP
        arrayContents[15] as Int,            // Red team EXP
        arrayContents[16] as Int,           // EXP for Attendance
        arrayContents[17] as Int,           // EXP for Costume
        arrayContents[18] as Int,           // EXP for Recruitment
    )
}

fun getAllTickets(eventId: String) : MutableList<Ticket> {
    // Get the event's tickets
    val event = getEvent(eventId)
    val allTicketIds = event.tickets

    // Create an array of the players connected to the tickets
    var allTickets : MutableList<Ticket> = mutableListOf()
    for (i in allTicketIds.indices) {
        val ticket : Ticket = getTicket(allTicketIds[i])
        findTicketUserId(ticket)
        allTickets.add(ticket)
    }

    return allTickets
}

fun getAllPlayers(eventId : String) : MutableList<Player> {
    // Get the event's tickets
    val event = getEvent(eventId)
    val allTicketIds = event.tickets

    // Create an array of the players connected to the tickets
    var allPlayers : MutableList<Player> = mutableListOf()
    for (i in allTicketIds.indices) {
        val ticket : Ticket = getTicket(allTicketIds[i])
        val currPlayer : Player = getPlayer(ticket.playerId)
        allPlayers += currPlayer
    }

    return allPlayers
}

fun getPlayer(playerId : String) : Player {

    // Find player in database by playerId, return an array of its contents
    // val arrayContents = [insert code here]

    // Placeholder "found" player
    val arrayContents = mutableListOf(
        "player123",
        "Bobb",
        "Polo",
        16,
        1830,
        listOf(1, 2, 3),
        listOf(2, 0, 0),
        listOf(3, 1, 4),
        listOf(4, 3, 1),
        listOf(1, 0, 0),
        mutableListOf("0767667090", "+46738255553"),

    )

    return Player(
        arrayContents[0] as String,         // playerId
        arrayContents[1] as String,         // first name
        arrayContents[2] as String,         // last name
        arrayContents[3] as Int,            // age
        arrayContents[4] as Int,            // total exp
        arrayContents[5] as MutableList<Int>,      // healer levels
        arrayContents[6] as MutableList<Int>,      // mage levels
        arrayContents[7] as MutableList<Int>,      // rogue levels
        arrayContents[8] as MutableList<Int>,      // knight levels
        arrayContents[9] as MutableList<Int>,      // warrior levels
        arrayContents[10] as MutableList<String>,  // guardian phone numbers
    )
}

fun getTeamTickets(tickets : MutableList<Ticket>, getBlue : Boolean) : MutableList<Ticket> {
    // Specify which team you're getting
    var team = "Red"
    if (getBlue) {
        team = "Blue"
    }

    // Go through each player
    var teamList : MutableList<Ticket> = mutableListOf()
    for (i in tickets.indices) {

        // Add correct players to a new teamList array
        val currTicket = tickets[i]
        if (currTicket.teamColor == team){
            teamList.add(tickets[i])
        }
    }

    return teamList
}

fun getPlayerEXP(playerId : String) : Int {
    // Find all tickets tied to the player, and add their total exp together
    // val exp = [insert code here]

    // Placeholder "found" exp
    val exp = 123

    return exp
}

fun getRoleByNumber(number : Int) : String {
    var role = ""

    when (number) {
        0 -> {
            role = "Undecided"
        }
        1 -> {
            role = "Helare"
        }
        2 -> {
            role = "Odåga"

        }
        3 -> {
            role = "Magiker"
        }
        4 -> {
            role = "Riddare"
        }
        5 -> {
            role = "Special A"
        }
        6 -> {
            role = "Special B"
        }
        7 -> {
            role = "Krigare"
        }
    }

    return role
}


fun mergeTicketAndPlayer(player : Player, ticket : Ticket) {
    player.age = ticket.age
    // Add guardian to player
}

fun findTicketUserId(ticket : Ticket) : String {
    // Find ticket's guardian among previous guardians
    var matchingId = ""
    val phoneNumber = getGuardian(ticket)

    // If no match found, return empty, wait for manual matching
    if (phoneNumber == ""){
        return matchingId
    }

    // Otherwise, find all players connected to guardian
    val playerArray : Array<String> = getGuardianPlayerArray(phoneNumber)

    // Find matching name among players
    for (playerId in playerArray) {
        val name = getPlayerName(playerId)
        if (name == ticket.fullName) {
            matchingId = playerId
        }
    }

    return matchingId
}

fun getGuardian(ticket: Ticket) : String {
    // Find phone nr
    var formattedNumber = formatPhoneNumber(ticket.guardianPhoneNr)

    // Hitta i databasen
    // var phone = findGuardianByPhone(formattedNumber)
    var phone = ""

    if (phone == ""){
        // Hitta i databasen
        //phone = findGuardianPhoneByEmail(ticket.guardianEmail)
        phone = "0700000000"
    }
    return phone
}

fun getGuardianPlayerArray(phoneNumber : String) : Array<String>{
    // hitta i databasen
    // val ids = getPlayerArray(phoneNumber)

    // placeholder
    val ids = arrayOf("12345", "12346", "12347")

    return ids
}

fun formatPhoneNumber(number: String) : String{
    // pls
    return number
}

fun getPlayerName(playerId : String) : String {
    // Hitta i databasen
    // val firstName = getPlayerFirstName(playerId)
    // val lastName = getPlayerLastName(playerId)

    // Placeholder
    val firstName = "Bob"
    val lastName = "Gold"
    val fullName = "${firstName} ${lastName}"

    return fullName
}