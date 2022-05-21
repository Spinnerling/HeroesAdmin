package com.example.heroadmin

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject


class DatabaseFunctions(var context: Context?) {
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

    fun apiCallPost(url: String, parcel: JSONObject) {
        val requestQueue = Volley.newRequestQueue(context)
        Log.i("test", "Sending Parcel with info:\n" +
                "Name: " + parcel.getString("First_Name") + "\n" +
                "Team Color: " + parcel.getString("Team_Color") + "\n" +
                "Checked in: " + parcel.getString("Checked_In") + "\n" +
                "Tabard Nr: " + parcel.getString("Tabard_Nr"))
        val putRequest: JsonObjectRequest =
            object : JsonObjectRequest(
                Method.PUT, url, parcel,
                Response.Listener { response ->
                    // response
                    Log.i("Put Success", "$response")

                },
                Response.ErrorListener { error ->
                    // error
                    Log.i("Put Error", "$error")
                }
            ) {

                override fun getHeaders(): Map<String, String> {
                    val headers: MutableMap<String, String> =
                        HashMap()
                    headers["Content-Type"] = "application/json"
                    headers["Accept"] = "application/json"
                    return headers
                }

                override fun getBody(): ByteArray {
                    Log.i("json", parcel.toString())
                    return parcel.toString().toByteArray(charset("UTF-8"))
                }

            }

        requestQueue.add(putRequest)
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
        // Create list of Ticket IDs
        val jsonArray = eventJson.getJSONArray("TicketIDs")
        val list = MutableList(jsonArray.length()) {
            jsonArray.getString(it)
        }

        Log.i("test", "Event: " + eventJson.getString("Event_Start_date"))

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

    fun getTicketGuardians(ticketList: MutableList<Ticket>) {
        allTickets = ticketList

        for (ticket in allTickets) {
            if (ticket.playerId != "") {
                continue
            }

            // Find ticket's guardian among previous guardians by their phone number
            val formattedNumber = formatPhoneNumber(ticket.guardianPhoneNr)
            ticket.guardianPhoneNr = formattedNumber
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
        if (setBlue) {
            ticket.teamColor = "Blue"
        } else {
            ticket.teamColor = "Red"
        }

        // Update database
        val parcel = createTicketMap(ticket)
        apiCallPost("https://talltales.nu/API/api/update-ticket.php", parcel)
    }


    fun createTicketMap(ticket: Ticket): JSONObject {
        val parcel = JSONObject()
        parcel.put("Ticket_ID", ticket.ticketId)
        parcel.put("First_Name", ticket.firstName)
        parcel.put("Last_Name", ticket.lastName)
        parcel.put("Age", ticket.age.toString())
        parcel.put("KP_Phone_Nr", ticket.guardianPhoneNr)
        parcel.put("KP_Name", ticket.guardianName)
        parcel.put("Booking_Mail", ticket.bookerEmail)
        parcel.put("Booking_Name", ticket.bookerFullName)
        parcel.put("Team_Color", ticket.teamColor)
        parcel.put("Tabard_Nr", ticket.tabardNr)
        parcel.put("Note", ticket.note)
        parcel.put("Checked_In", ticket.checkedIn)
        parcel.put("Recruits", ticket.recruits)
        parcel.put("EXP_Personal", ticket.expPersonal)
        parcel.put("Benched", ticket.benched)
        parcel.put("Guaranteed_Role", ticket.guaranteedRole)
        parcel.put("Rounds_M", ticket.roundsMage)
        parcel.put("Rounds_O", ticket.roundsRogue)
        parcel.put("Rounds_K", ticket.roundsWarrior)
        parcel.put("Rounds_H", ticket.roundsHealer)
        parcel.put("Rounds_R", ticket.roundsKnight)
        parcel.put("Respawns", ticket.hasRespawn)
        parcel.put("Current_Role", ticket.currentRole)
        parcel.put("Player_ID", ticket.playerId)

        return parcel
    }
}