package com.example.heroadmin

fun getTicket(ticketId: String): Ticket {

    // Find ticket in database by ticketId, return an array of its contents
    // val arrayContents = [insert code here]

    // Placeholder "found" ticket
    val arrayContents = mutableListOf(
        "ticket123",
        "12345",
        "Bob",
        "Polo",
        16,
        false,
        "None",
        false,
        0,
        false,
        "None",
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
        "None",
        0,
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
        arrayContents[27] as String,   // Guaranteed Role
        arrayContents[28] as Int,      // EXP personal
        arrayContents[29] as Int,      // Tabard number
    )
}

fun getEventIds(): List<String> {
    // Find array of events in database
    // [insert code here]

    // Placeholder "found" array
    val array = listOf("event123")

    return array
}

fun getEvent(eventId: String): Event {
    // Find event in database by eventId, return an array of its contents
    // var arrayContents = [insert code here]

    // Placeholder "found" event
    val arrayContents = mutableListOf(
        "event123",
        "13:00",
        "14:30",
        "this here event is much fun for everyhopa",
        "vanligt event",
        "",
        48,
        listOf("ticket123"),
        0,
        0,
        10,
        5,
        5,
    )

    return Event(
        arrayContents[0] as String,         // eventId
        arrayContents[1] as String,         // startTime
        arrayContents[2] as String,         // endTime
        arrayContents[3] as String,         // description
        arrayContents[4] as String,         // title
        arrayContents[5] as String,         // reportText
        arrayContents[6] as Int,            // player max amount
        arrayContents[7] as List<String>,   // ticketId array
        arrayContents[8] as Int,            // Blue team EXP
        arrayContents[9] as Int,            // Red team EXP
        arrayContents[10] as Int,           // EXP for Attendance
        arrayContents[11] as Int,           // EXP for Costume
        arrayContents[12] as Int,           // EXP for Recruitment
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
        val currTicket : Ticket = getTicket(ticket.playerId)
        allTickets.add(currTicket)
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
        155,
        listOf(1, 0, 0),
        listOf(1, 0, 0),
        listOf(1, 0, 0),
        listOf(1, 0, 0),
        listOf(1, 0, 0),
        mutableListOf("0767667090", "+46738255553"),

    )

    return Player(
        arrayContents[0] as String,         // playerId
        arrayContents[1] as String,         // first name
        arrayContents[2] as String,         // last name
        arrayContents[3] as Int,            // age
        arrayContents[4] as Int,            // total exp
        arrayContents[5] as List<Int>,      // healer levels
        arrayContents[6] as List<Int>,      // mage levels
        arrayContents[7] as List<Int>,      // rogue levels
        arrayContents[8] as List<Int>,      // knight levels
        arrayContents[9] as List<Int>,      // warrior levels
        arrayContents[10] as MutableList<String>,  // guardian phone numbers
    )
}

fun getTeamPlayers(tickets : MutableList<Ticket>, getBlue : Boolean) : MutableList<Ticket>? {
    // Specify which team you're getting
    var team = "red"
    if (getBlue) {
        team = "blue"
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

    // Check if you got any
    if (teamList.isEmpty()){
        return null
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
            role = "Magiker"
        }
        3 -> {
            role = "Odåga"
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

fun switchTeam(ticket : Ticket){
    if (ticket.teamColor == "red") {
        ticket.teamColor = "blue"
    }
    else {
        ticket.teamColor = "red"
    }

    // [Send to database]

    // [Update team lists]
}