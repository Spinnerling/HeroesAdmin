package com.example.heroadmin

fun getTicket(ticketId: String): Ticket {

    // Find ticket in database by ticketId, return an array of its contents
    // val arrayContents = [insert code here]

    // Placeholder "found" ticket
    val arrayContents = listOf(
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
        arrayContents[10] as String,   // Last Role
        arrayContents[11] as String,   // Contact First name
        arrayContents[12] as String,   // Contact Last Name
        arrayContents[13] as String,   // Contact Phone
        arrayContents[14] as String,   // Contact Email
        arrayContents[15] as String,   // Booker First Name
        arrayContents[16] as String,   // Booker Last Name
        arrayContents[17] as String,   // Booker Phone
        arrayContents[18] as String,   // Booker Email
        arrayContents[19] as Int,      // Rounds as Healer
        arrayContents[20] as Int,      // Rounds as Mage
        arrayContents[21] as Int,      // Rounds as Rogue
        arrayContents[22] as Int,      // Rounds as Knight
        arrayContents[23] as Int,      // Rounds as Warrior
        arrayContents[24] as Int,      // Rounds as Special
        arrayContents[25] as Int,      // Respawns left
        arrayContents[26] as String,   // Guaranteed Role
        arrayContents[27] as Int,      // EXP personal
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
    val arrayContents = listOf(
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

fun getPlayer(playerId : String) : Player {

    // Find player in database by playerId, return an array of its contents
    // val arrayContents = [insert code here]

    // Placeholder "found" player
    val arrayContents = listOf(
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
        listOf("0767667090", "+46738255553"),
    )

    return Player(
        arrayContents[0] as String,         // playerId
        arrayContents[0] as String,         // first name
        arrayContents[0] as String,         // last name
        arrayContents[0] as Int,            // age
        arrayContents[0] as Int,            // total exp
        arrayContents[0] as List<Int>,      // healer levels
        arrayContents[0] as List<Int>,      // mage levels
        arrayContents[0] as List<Int>,      // rogue levels
        arrayContents[0] as List<Int>,      // knight levels
        arrayContents[0] as List<Int>,      // warrior levels
        arrayContents[0] as List<String>,   // guardian phone numbers
    )
}

fun mergeTicketAndPlayer(player : Player, ticket : Ticket) {
    player.age = ticket.age
    // Add guardian to player
}