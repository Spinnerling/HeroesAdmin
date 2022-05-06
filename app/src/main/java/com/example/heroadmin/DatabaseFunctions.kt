package com.example.heroadmin

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject


class DatabaseFunctions(var context: Context?) {
    lateinit var currEvent : Event
    lateinit var currTicket : Ticket

    fun apiCallGet(
        url: String,
        responseFunction: (eventsJson: JSONObject) -> Unit
    ) {
        val requestQueue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
            responseFunction(JSONObject(response))
        },
            {
                Log.i("test", "Error! Failed call to api: "+ url)
            }
        )
        requestQueue.cache.clear()
        requestQueue.add(stringRequest)
    }

    fun apiCallPost(url : String, parcel : HashMap<String, String>){
        val requestQueue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                Log.i("test", "Posted parcel to "+ url )
            },
            Response.ErrorListener{ error ->
                Log.i("test", "Error! Failed call to api: "+ url)
            }
        ){
            override fun getParams(): HashMap<String, String> {
                // When updating data, include id in parcel
                return parcel
            }
        }
        requestQueue.add(stringRequest)
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
        Log.i("test", eventJson.getInt("EXP_Blueteam").toString() )

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

    fun getTicketGuardian(ticket: Ticket) {
        currTicket = ticket

        // Find ticket's guardian among previous guardians
        var matchingId = ""
        val phoneNumber = getGuardian(ticket)
    }

    private fun getGuardian(ticket: Ticket) {
        // Find phone nr
        var formattedNumber = formatPhoneNumber(ticket.guardianPhoneNr)

        // Hitta i databasen
        // var phone = findGuardianByPhone(formattedNumber)
        var phone = ""

        if (phone == "") {
            // Hitta i databasen
            //phone = findGuardianPhoneByName(ticket.guardianName)
            phone = "0700000000"
        }

        getPlayersByGuardian(phone)
    }

    private fun getPlayersByGuardian(phoneNumber: String) {
        apiCallGet("https://talltales.nu/API/api/guardian_players.php?id=$phoneNumber", ::findMatchingPlayers)
    }

    private fun findMatchingPlayers(json: JSONObject) {
        // Find matching name among players
        val dataArray: JSONArray = json.getJSONArray("data")
        val playerList = MutableList(dataArray.length()) {
            dataArray.getString(it)
        }

        for (playerId in playerList) {
            getPlayerName(playerId)
        }
    }

    private fun formatPhoneNumber(number: String): String {
        // pls
        return number
    }

    private fun getPlayerName(playerId: String){
        // Hitta i databasen
        apiCallGet("https://talltales.nu/API/api/player.php") { response ->
            if (response["fullname"] == currTicket.fullName) {
                currTicket.playerId = playerId
            }
        }
    }

    fun updateDBTicket(ticket: Ticket){
        apiCallGet( "", ::checkSuccess)
    }

    private fun checkSuccess(json : JSONObject){

    }
}