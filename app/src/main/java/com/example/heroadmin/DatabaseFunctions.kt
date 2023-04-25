package com.example.heroadmin

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.serialization.encodeToString
import com.android.volley.NoConnectionError


class DatabaseFunctions(private val context: Context) {
    lateinit var currEvent: Event
    lateinit var currTicket: Ticket
    lateinit var allTickets: MutableList<Ticket>

    fun apiCallGet(
        url: String,
        responseFunction: (eventsJson: JSONObject) -> Unit,
        errorFunction: () -> Unit
    ) {
        val requestQueue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                responseFunction(JSONObject(response))
            },
            { error ->
                errorFunction()
                Log.i("test", "Error! Failed call to api: " + url)
            }
        )
        requestQueue.cache.clear()
        requestQueue.add(stringRequest)
    }

    fun apiCallPost(url: String, jsonString: String) {
        val mySingleton = MySingleton.getInstance(context)
        Log.i("test", "Sending JSON string: $jsonString")

        val putRequest: StringRequest = object : StringRequest(
            Method.PUT, url,
            Response.Listener { response ->
                // response
                Log.i("Put Success", "$response")
            },
            Response.ErrorListener { error ->
                // error
                Log.i("Put Error", "$error")

                if (error is NoConnectionError) {
                    // Handle no connection error
                    Log.i("Put Error", "No internet connection")
                    connectionLost()
                } else {
                    // Handle other error types
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                return headers
            }

            override fun getBody(): ByteArray {
                Log.i("json", jsonString)
                return jsonString.toByteArray(charset("UTF-8"))
            }
        }

        mySingleton.addToRequestQueue(putRequest)
    }

    fun connectionLost() {

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
        // Deserialize the JSON to the Event class
        val event = Json.decodeFromString<Event>(eventJson.toString())

        // Return the event
        return event
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
        // Find player in the database by playerId, and get the JSON string of the player's data
        // This will be replaced with the actual JSON string from the database
        val jsonString = """
        {
            "playerId": "player123",
            "firstName": "Bobb",
            "lastName": "Polo",
            "age": 16,
            "exp2021": 1830,
            "exp2022": 1830,
            "exp2023": 0,
            "healerLevel": 1,
            "mageLevel": 1,
            "rogueLevel": 1,
            "knightLevel": 1,
            "warriorHealer": 0,
            "warriorRogue": 0,
            "warriorMage": 0,
            "warriorKnight": 0
        }
    """

        // Deserialize the JSON string to a Player object
        val player = Json.decodeFromString<Player>(jsonString)

        return player
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
        player.age = ticket.age ?:0
        // Add guardian to player
    }

    fun getTicketGuardians(ticketList: MutableList<Ticket>) {
        allTickets = ticketList

        for (ticket in allTickets) {
            if (ticket.playerId != "") {
                continue
            }

            // Find ticket's guardian among previous guardians by their phone number
            val formattedNumber = ticket.bookerPhoneNr?.let { formatPhoneNumber(it) }
            ticket.bookerPhoneNr = formattedNumber
            apiCallGet(
                "https://talltales.nu/API/api/guardian_players.php?id=$formattedNumber",
                ::findPlayersByGuardian, {}
            )
            apiCallGet(
                "https://talltales.nu/API/api/guardian_players.php?id=$formattedNumber",
                ::findGuardiansByName, {}
            )
        }
    }

    private fun findGuardiansByName(response: JSONObject) {
        val dataArray: JSONArray = response.getJSONArray("data")
        val guardianList = MutableList(dataArray.length()) {
            dataArray.getJSONObject(it)
        }

        // Get all the guardian's players
        for (guardian in guardianList) {
            apiCallGet(
                "https://talltales.nu/API/api/guardian.php?Name=${guardian.getString("GuardianID")}",
                ::findPlayersByGuardian, {}
            )
        }
    }

    private fun findPlayersByGuardian(response: JSONObject) {
        val dataArray: JSONArray = response.getJSONArray("data")
        val playerList = MutableList(dataArray.length()) {
            dataArray.getString(it)
        }

        // Get all the guardian's players
        for (playerId in playerList) {
            apiCallGet("https://talltales.nu/API/api/player.php", ::compareNames, {})
        }
    }

    private fun compareNames(response: JSONObject) {
        val dataArray: JSONArray = response.getJSONArray("data")
        val playerList = MutableList(dataArray.length()) {
            dataArray.getJSONObject(it)
        }

        // Compare names
        for (player in playerList) {
            for (ticket in allTickets) {
                if (ticket.playerId != "") {
                    continue
                }

                if (player.getString("First_Name") == ticket.firstName && player["Last_Name"] == ticket.lastName) {
                    ticket.playerId = player.getString("PlayerId")
                }
            }
        }
    }

    private fun formatPhoneNumber(number: String): String {
        return PhoneNumberUtils.formatNumber(number)
    }

    fun setTicketTeamColor(ticket: Ticket, setBlue: Boolean) {
        ticket.teamColor = if (setBlue) "Blue" else "Red"

        // Update database
        val jsonString = createTicketJsonString(ticket)
        apiCallPost( "https://talltales.nu/API/api/update-ticket.php", jsonString)
    }

    fun createTicketJsonString(ticket: Ticket): String {
        return Json.encodeToString(ticket)
    }
}