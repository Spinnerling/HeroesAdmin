package com.example.heroadmin

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject

class DatabaseFunctions(var context: Context?) {
    lateinit var currEvent : Event

    fun apiBaseCall(
        method: Int,
        url: String,
        responseFunction: (eventsJson: JSONObject) -> Unit
    ) {
        val jsonObjectRequest = JsonObjectRequest(
            method, url, null,
            { response: JSONObject ->
                responseFunction(response)
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
    }


    fun getEventIds(context: Context?): List<String> {
        // Find array of events in database

        val url = "https://www.talltales.nu/API/api/read.php"
        var list = mutableListOf<String>()
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                list = getEventIdArray(response)
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

    private fun getEventIdArray(response: JSONObject?): MutableList<String> {
        val eventIdArray = mutableListOf<String>()
        val array: JSONArray = response!!.getJSONArray("data")
        val length = array.length()
        if (length > 0) {
            var index = 0
            while (index < length) {
                val item = array.getJSONObject(index)
                val id = item.getString("id")
                Log.i("test", id + " is the id")
                eventIdArray.add(id)
                index++
            }
        }
        return eventIdArray
    }

    fun getEventArray(response: JSONObject): MutableList<Event> {
        val eventArray = mutableListOf<Event>()
        val array: JSONArray = response.getJSONArray("data")
        val listLength = array.length()
        if (listLength > 0) {
            var listIndex = 0
            while (listIndex < listLength) {

                val eventJson = array.getJSONObject(listIndex)

                val event = parseEventJson(eventJson)

                eventArray.add(event)
                listIndex++
            }
        }
        return eventArray
    }

    private fun parseEventJson(eventJson: JSONObject): Event {
        // Create list of Ticket IDs
        val jsonArray = eventJson.getJSONArray("TicketIDs")
        val list = MutableList(jsonArray.length()) {
            jsonArray.getString(it)
        }

        currEvent = Event(
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
            list
        )

        // Create an event out of the JSON data
        return currEvent
    }

    fun getAllPlayers(event: Event): MutableList<Player> {
        // Get the event's tickets
        var allTicketIds = mutableListOf<String>()

        if (event.tickets.isNotEmpty()) {
            allTicketIds = event.tickets
        }

        // Create an array of the players connected to the tickets
        val allPlayers: MutableList<Player> = mutableListOf()
        for (i in allTicketIds.indices) {
            /*val ticket: Ticket = getTicket(allTicketIds[i])
            val currPlayer: Player = getPlayer(ticket.playerId)
            allPlayers += currPlayer*/
        }

        return allPlayers
    }

    fun getPlayer(playerId: String): Player {

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

    fun getPlayerEXP(playerId: String): Int {
        // Find all tickets tied to the player, and add their total exp together
        // val exp = [insert code here]

        // Placeholder "found" exp
        val exp = 123

        return exp
    }

    fun getRoleByNumber(number: Int): String {
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


    fun mergeTicketAndPlayer(player: Player, ticket: Ticket) {
        player.age = ticket.age
        // Add guardian to player
    }

    fun findTicketPlayerId(ticket: Ticket): String {
        // Find ticket's guardian among previous guardians
        var matchingId = ""
        val phoneNumber = getGuardian(ticket)

        // If no match found, return empty, wait for manual matching
        if (phoneNumber == "") {
            return matchingId
        }

        // Otherwise, find all players connected to guardian
        val playerArray: Array<String> = getGuardianPlayerArray(phoneNumber)

        // Find matching name among players
        for (playerId in playerArray) {
            val name = getPlayerName(playerId)
            if (name == ticket.fullName) {
                matchingId = playerId
            }
        }

        return matchingId
    }

    fun getGuardian(ticket: Ticket): String {
        // Find phone nr
        var formattedNumber = formatPhoneNumber(ticket.guardianPhoneNr)

        // Hitta i databasen
        // var phone = findGuardianByPhone(formattedNumber)
        var phone = ""

        if (phone == "") {
            // Hitta i databasen
            //phone = findGuardianPhoneByEmail(ticket.guardianEmail)
            phone = "0700000000"
        }
        return phone
    }

    fun getGuardianPlayerArray(phoneNumber: String): Array<String> {
        // hitta i databasen
        // val ids = getPlayerArray(phoneNumber)

        // placeholder
        val ids = arrayOf("12345", "12346", "12347")

        return ids
    }

    fun formatPhoneNumber(number: String): String {
        // pls
        return number
    }

    fun getPlayerName(playerId: String): String {
        // Hitta i databasen
        // val firstName = getPlayerFirstName(playerId)
        // val lastName = getPlayerLastName(playerId)

        // Placeholder
        val firstName = "Bob"
        val lastName = "Gold"
        val fullName = "${firstName} ${lastName}"

        return fullName
    }
}