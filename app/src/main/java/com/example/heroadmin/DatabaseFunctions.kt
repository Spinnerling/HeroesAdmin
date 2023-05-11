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
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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
                Log.d("check", "response: $response")
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

    fun getEventArray(responseJson: JSONObject, json: Json): MutableList<Event> {
        val eventList = mutableListOf<Event>()

        if (responseJson.has("data")) {
            Log.d("DatabaseFunctions", "Received JSON: $responseJson")
            val eventsJsonArray = responseJson.getJSONArray("data")

            // Add the while loop here
            var i = 0
            while (i < eventsJsonArray.length()) {
                val eventJson = eventsJsonArray.getJSONObject(i)
                val event: Event = json.decodeFromString(Event.serializer(), eventJson.toString())
                eventList.add(event)
                Log.d("DatabaseFunctions", "Added event: " + event.title)
                i++
            }
        } else {
            Log.e("DatabaseFunctions", "No 'data' key found in the JSON response")
        }

        return eventList
    }

    fun parseEventJson(jsonString: String, json: Json): Event {
        // Deserialize the JSON to the Event class
        return json.decodeFromString(Event.serializer(), jsonString)
    }

    fun getAllPlayers(event: Event): MutableList<Player> {
        // Get the event's tickets
        var allTicketIds = mutableListOf<String>()

        if (event.ticketIDs.isNotEmpty()) {
            allTicketIds = event.ticketIDs
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

    fun getTicketBookers(ticketList: MutableList<Ticket>) {
        allTickets = ticketList

        for (ticket in allTickets) {
            if (ticket.playerId != "") {
                continue
            }

            // Find ticket's guardian among previous guardians by their phone number
            val formattedNumber = ticket.bookerPhone?.let { formatPhoneNumber(it) }
            ticket.bookerPhone = formattedNumber
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
        val jsonString = createJsonString(ticket)
        apiCallPost( "https://talltales.nu/API/api/update-ticket.php", jsonString)
    }

    inline fun <reified T> createJsonString(data: T): String {
        return Json.encodeToString(data)
    }

    inline fun <reified T> updateData(data: T) {
        val jsonString = createJsonString(data)
        val className = T::class.java.simpleName.lowercase(Locale.ROOT)
        val endpoint = "https://talltales.nu/API/api/update-$className.php"
        apiCallPost(endpoint, jsonString)
    }

    sealed class MatchResult {
        data class DefiniteMatch(val playerId: String) : MatchResult()
        data class Suggestions(val suggestions: List<PlayerListItem>) : MatchResult()
        object NoMatch : MatchResult()
    }

    suspend fun matchTicketToPlayer(ticket: Ticket): MatchResult {
        return suspendCoroutine { continuation ->
            val url = "https://your.api/endpoint?firstName=${ticket.firstName}&lastName=${ticket.lastName}&age=${ticket.age}&bookerName=${ticket.bookerName}"

            apiCallGet(
                url,
                responseFunction = { response ->
                    // Based on the response, call onResult with the appropriate MatchResult subclass instance
                    when (val matchType = response.getString("matchType")) {
                        "definite" -> {
                            val playerId = response.getString("playerId")
                            continuation.resume(MatchResult.DefiniteMatch(playerId))
                        }
                        "suggestions" -> {
                            val suggestedPlayers = response.getJSONArray("suggestedPlayers")
                            val playerList = mutableListOf<PlayerListItem>()

                            for (i in 0 until suggestedPlayers.length()) {
                                val playerJson = suggestedPlayers.getJSONObject(i)
                                val player = Json.decodeFromString<PlayerListItem>(playerJson.toString())
                                playerList.add(player)
                            }

                            continuation.resume(MatchResult.Suggestions(playerList))
                        }
                        "noMatch" -> {
                            continuation.resume(MatchResult.NoMatch)
                        }
                        else -> {
                            Log.e("matchTicketToPlayer", "Invalid matchType: $matchType")
                            continuation.resume(MatchResult.NoMatch) // You can decide how to handle this case
                            Log.i("check", "{${ticket.fullName} had an error")
                        }
                    }
                },
                errorFunction = {
                    // Handle error case here
                    Log.e("matchTicketToPlayer", "API call failed")
                    continuation.resume(MatchResult.NoMatch) // You can decide how to handle this case
                }
            )
        }
    }

    suspend fun matchTicketToPlayerLocal(ticket: Ticket, playerDatabase: LocalDatabase<Player, String>): MatchResult {

        delay(500) // Add delay to simulate network latency

        val players = playerDatabase.getAll().filter { player ->
            player.firstName == ticket.firstName && player.lastName == ticket.lastName
        }

        return when {
            players.isEmpty() -> MatchResult.NoMatch
            players.size == 1 -> {
                val player = players.first()
                // You can add additional checks here to confirm it's a definite match
                MatchResult.DefiniteMatch(player.playerId)
            }
            else -> {
                val playerListItems = players.map { player ->
                    // Convert player objects to PlayerListItem objects
                    PlayerListItem(
                        playerId = player.playerId,
                        firstName = player.firstName!!,
                        lastName = player.lastName!!,
                        age = player.age!!
                    )
                }
                MatchResult.Suggestions(playerListItems)
            }
        }
    }

    fun getHighestPlayerId(onComplete: (String?) -> Unit) {
        val url = "https://your.api/endpoint/getHighestPlayerId"

        apiCallGet(
            url,
            responseFunction = { response ->
                val highestId = response.getString("highestId")
                onComplete(highestId)
            },
            errorFunction = {
                Log.e("getHighestPlayerId", "API call failed")
                onComplete(null)
            }
        )
    }
}