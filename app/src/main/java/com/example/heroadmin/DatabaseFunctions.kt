package com.example.heroadmin

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
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
import com.android.volley.VolleyLog
import com.example.heroadmin.LocalDatabaseSingleton.playerDatabase
import kotlinx.coroutines.delay
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.UnsupportedEncodingException


class DatabaseFunctions(val context: Context) {
    private lateinit var currActivity: MainActivity
    lateinit var currEvent: Event
    lateinit var currTicket: Ticket
    lateinit var allTickets: MutableList<Ticket>
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val mySingleton = MySingleton.getInstance(context)

    fun apiCallGet(
        url: String,
        responseFunction: (eventsJson: JSONObject) -> Unit,
        errorFunction: () -> Unit
    ) {
        Log.i("apiCall", "Calling API at url: $url")
        val requestQueue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                Log.d("apiCall", "response: $response")
                responseFunction(JSONObject(response))
            },
            { error ->
                errorFunction()
                Log.i("apiCall", "Error! Failed call to api: $url. Error: $error")
            }
        )
        requestQueue.cache.clear()
        requestQueue.add(stringRequest)
    }

    fun apiCallPost(url: String,
                   responseFunction: (eventsJson: JSONObject) -> Unit,
                   errorFunction: () -> Unit,
                   jsonString: String) {
        val mySingleton = MySingleton.getInstance(context)
        Log.i("test", "Sending JSON string: $jsonString")

        val postRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                // Handle the response from the server
                Log.i("Post Success", "$response")
                responseFunction(JSONObject(response))
            },
            Response.ErrorListener { error ->
                // Handle error cases
                if (error is NoConnectionError) {
                    // Handle no connection error
                    Log.i("Post Error", "No internet connection")
                    connectionLost()
                } else {
                    // Handle other error types
                    Log.i("Post Error", "$error")
                    errorFunction()
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
                return try {
                    Log.i("apiCallPost", "POST/getBody called")
                    jsonString.toByteArray(Charsets.UTF_8)
                } catch (uee: UnsupportedEncodingException) {
                    Log.e("apiCallPost", "Unsupported Encoding while trying to get the bytes of $jsonString using UTF-8", uee)
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using UTF-8", jsonString)
                    byteArrayOf()
                }
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Set the cache to false

        postRequest.setShouldCache(false)
        mySingleton.addToRequestQueue(postRequest)
    }

    fun apiCallPut(url: String, jsonString: String, callback: VolleyCallback) {
        Log.i("apiCallPut", "Sending JSON string: $jsonString to URL: $url")

        val putRequest: StringRequest = object : StringRequest(
            Method.PUT, url,
            Response.Listener { response ->
                // response
                Log.i("apiCallPut", "Received response: $response")
                callback.onSuccess(response)
            },
            Response.ErrorListener { error ->
                Log.e("apiCallPut", "Error in request: ${error.message}", error)
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return try {
                    Log.i("apiCallPut", "PUT/getBody called")
                    jsonString.toByteArray(Charsets.UTF_8)
                } catch (uee: UnsupportedEncodingException) {
                    Log.e("apiCallPut", "Unsupported Encoding while trying to get the bytes of $jsonString using UTF-8", uee)
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using UTF-8", jsonString)
                    byteArrayOf()
                }
            }
        }

        putRequest.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Set the cache to false
        putRequest.setShouldCache(false)

        mySingleton.addToRequestQueue(putRequest)
    }

//        fun apiCallPut(url: String, jsonString: String) {
//        val mySingleton = MySingleton.getInstance(context)
//        Log.i("apiCallPut", "Sending JSON string: $jsonString")
//
//        val putRequest: StringRequest = object : StringRequest(
//            Method.PUT, url,
//            Response.Listener { response ->
//                // response
//                Log.i("apiCallPut", "Response:  $response")
//            },
//            Response.ErrorListener { error ->
//                // error
//                Log.i("apiCallPut", "Error: $error")
//
//                if (error is NoConnectionError) {
//                    // Handle no connection error
//                    Log.i("apiCallPut", "No internet connection")
//                    connectionLost()
//                } else {
//                    // Handle other error types
//                }
//            }
//        ) {
//            override fun getHeaders(): Map<String, String> {
//                val headers: MutableMap<String, String> = HashMap()
//                headers["Content-Type"] = "application/json"
//                headers["Accept"] = "application/json"
//                return headers
//            }
//
//            override fun getBody(): ByteArray {
//                Log.i("json", jsonString)
//                return jsonString.toByteArray(charset("UTF-8"))
//            }
//        }
//
//        mySingleton.addToRequestQueue(putRequest)
//    }

    interface VolleyCallback {
        fun onSuccess(result: String?)
        fun onError(error: String?)
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

    fun getEventArray(responseJson: JSONObject, tjson: Json): MutableList<Event> {
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

    suspend fun getPlayer(playerId: String): Player? = suspendCoroutine { continuation ->
        Log.i("player", "Getting player by id: $playerId")
        val url = "https://www.talltales.nu/API/api/get-player.php?playerId=$playerId"

        apiCallGet(url,
            { responseJson ->
                val jsonElement = Json.parseToJsonElement(responseJson.toString())
                if (!jsonElement.jsonObject.isEmpty()) {
                    val player = json.decodeFromString<Player>(responseJson.toString())
                    Log.i("player", "Successfully got player by id: $playerId")
                    continuation.resume(player)
                } else {
                    continuation.resume(null)
                    Log.i("player", "Got an empty json")
                }
            },
            {
                continuation.resumeWithException(RuntimeException("Error getting player with id: $playerId"))

                Log.i("player", "Could not get player by id: $playerId")
            }
        )

//        return playerDatabase.getById(playerId)!!
    }

    fun getPlayerLocal(playerId: String): Player?{
        return playerDatabase.getById(playerId)!!
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

        // Create a Set to store unique phone numbers
        val uniqueNumbers = mutableSetOf<String>()

        for (ticket in allTickets) {
            if (ticket.playerId != "") {
                continue
            }

            // Add phone number to set
            ticket.bookerPhone?.let { formatPhoneNumber(it) }?.also { uniqueNumbers.add(it) }
        }

        for (number in uniqueNumbers) {
            apiCallGet(
                "https://talltales.nu/API/api/booker_players.php?id=$number",
                { response ->
                    findPlayersByBooker(response)
                    findBookersByName(response)
                },
                {}
            )
        }
    }

    private fun findBookersByName(response: JSONObject) {
        val dataArray: JSONArray = response.getJSONArray("data")
        val bookerList = MutableList(dataArray.length()) {
            dataArray.getJSONObject(it)
        }

        // Get all the booker's players
        for (booker in bookerList) {
            apiCallGet(
                "https://talltales.nu/API/api/booker.php?Name=${booker.getString("BookerID")}",
                ::findPlayersByBooker, {}
            )
        }
    }

    private fun findPlayersByBooker(response: JSONObject) {
        val dataArray: JSONArray = response.getJSONArray("data")
        val playerList = MutableList(dataArray.length()) {
            dataArray.getString(it)
        }

        // Get all the booker's players
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
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val phoneNumber: Phonenumber.PhoneNumber

        try {
            phoneNumber = phoneNumberUtil.parse(number, "your_default_region_code")
        } catch (e: Exception) {
            // Handle parsing exception
            return number
        }

        return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
    }

    fun setTicketTeamColor(ticket: Ticket, setBlue: Boolean) {
        ticket.teamColor = if (setBlue) "Blue" else "Red"

        // Update database
        updateData(ticket)
    }

    inline fun <reified T> createJsonString(data: T): String {
        val json = Json { encodeDefaults = true }
        return json.encodeToString(data)
    }

    inline fun <reified T> updateData(data: T) {
        val className = T::class.java.simpleName.lowercase(Locale.ROOT)
//  Log.i("apiCallPut","Updating $className data...")
        val jsonString = createJsonString(data)
        val url = "https://www.talltales.nu/API/api/update-$className.php"
        apiCallPut(url, jsonString, object : VolleyCallback {
            override fun onSuccess(result: String?) {
                Log.i("apiCallPut", "Request completed: $result")
            }

            override fun onError(error: String?) {
                Log.i("apiCallPut", "Request error: $error")
            }
        })
    }

//    inline fun <reified T> updateData(data: T) {
//        val className = T::class.java.simpleName.lowercase(Locale.ROOT)
////        Log.i("apiCallPut","Updating $className data...")
//        val jsonString = createJsonString(data)
//        val url = "https://www.talltales.nu/API/api/update-$className.php"
//        apiCallPut(url, jsonString)
//    }

    @Serializable
    data class EventToSend(
        @SerialName("eventID") var eventId: String,
        var reportText: String? = null,
        var clickWinner: String? = null,
        var gameWinner: String? = null,
        var expAttendanceValue: Int = 20,
        var expClickWinValue: Int = 10,
        var expGameWinValue: Int = 5,
        var expTeamChangeValue: Int = 5,
        var blueGameWins: Int = 0,
        var redGameWins: Int = 0,
        var round: Int = 0,
        var status: String? = "Not started"
    )

    sealed class MatchResult {
        data class DefiniteMatch(val playerId: String) : MatchResult()
        data class Suggestions(val suggestions: List<PlayerListItem>) : MatchResult()
        object NoMatch : MatchResult()
    }

    suspend fun matchTicketToPlayer(ticket: Ticket): MatchResult {
        Log.i("playerLink", "Matching ticket: ${ticket.firstName}")
        return suspendCoroutine { continuation ->
            val url = "https://www.talltales.nu/API/api/check-match.php"

            apiCallPost(
                url, { response ->

                    Log.i("playerLink", "${ticket.firstName}: $response")
                    // Based on the response, call onResult with the appropriate MatchResult subclass instance
                    when (val matchType = response.getString("matchType")) {
                        "definite" -> {
                            val playerId = response.getString("playerId")
                            continuation.resume(MatchResult.DefiniteMatch(playerId))
                        }

//                        "suggestions" -> {
//                            val suggestedPlayers = response.getJSONArray("suggestedPlayers")
//                            val playerList = mutableListOf<PlayerListItem>()
//
//                            for (i in 0 until suggestedPlayers.length()) {
//                                val playerJson = suggestedPlayers.getJSONObject(i)
//                                val player = json.decodeFromString<PlayerListItem>(playerJson.toString())
//                                playerList.add(player)
//                            }
//
//                            continuation.resume(MatchResult.Suggestions(playerList))
//                        }

                        "noMatch" -> {
                            continuation.resume(MatchResult.NoMatch)
                            Log.i("playerLink", "No match found: $response")
                        }

                        else -> {
                            continuation.resume(MatchResult.NoMatch) // You can decide how to handle this case
                            Log.i("playerLink", "${ticket.fullName} had an error with matchType: $matchType")
                        }
                    }
                },
                errorFunction = {
                    // Handle error case here
                    Log.e("playerLink", "API call failed")
                    continuation.resume(MatchResult.NoMatch) // You can decide how to handle this case
                },
                createJsonString(ticket)
            )
        }
    }

    suspend fun matchTicketToPlayerLocal(ticket: Ticket, playerDatabase: LocalDatabase<Player, String>): MatchResult {
        Log.i("matches", "Started matchTicketToPlayerLocal")
        delay(500) // Add delay to simulate network latency

        // Make a copy of the database values before filtering
        val allPlayers = playerDatabase.getAll().toList()
        val players = allPlayers.filter { player ->
            player.firstName == ticket.firstName && player.lastName == ticket.lastName
        }

        return when {
            players.isEmpty() -> {
                Log.i("matches", "No matches found for ticket: ${ticket.firstName}")
                MatchResult.NoMatch
            }
            players.size == 1 -> {
                val player = players.first()
                Log.i("matches", "Definite match found: ${player.playerId} for ticket: ${ticket.firstName}")
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
                Log.i("matches", "Multiple matches found for ticket: $ticket. Suggestions: $playerListItems")
                MatchResult.Suggestions(playerListItems)
            }
        }
    }
}