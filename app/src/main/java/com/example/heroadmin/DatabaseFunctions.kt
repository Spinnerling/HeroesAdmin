package com.example.heroadmin

import android.content.Context
import android.os.Handler
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.serialization.encodeToString
import com.android.volley.NoConnectionError
import com.android.volley.VolleyLog
import com.example.heroadmin.LocalDatabaseSingleton.playerDatabase
import com.example.heroadmin.LocalDatabaseSingleton.ticketDatabase
import kotlinx.serialization.json.jsonObject
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.UnsupportedEncodingException


class DatabaseFunctions(val context: Context) {
    var currActivity: MainActivity? = null
    lateinit var currEvent: Event
    lateinit var currTicket: Ticket
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val mySingleton = MySingleton.getInstance(context)
    private var eventView : EventView? = null
    private var levelUpView : LevelUpFragment? = null

    fun setEventView(view : EventView){
        eventView = view
    }

    fun setLevelUpView(view : LevelUpFragment){
        levelUpView = view
    }

    fun apiCallGet(
        url: String,
        responseFunction: (eventsJson: JSONObject) -> Unit,
        errorFunction: () -> Unit,
        retryCount: Int = 3 // default retry count is 3
    ) {
        Log.i("apiCallGet", "Calling API at url: $url")
        val requestQueue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(Method.GET, url,
            { response ->
                Log.d("apiCallGet", "Får ner GET response: $response")
                responseFunction(JSONObject(response))
                foundConnection()
            },
            { error ->
                if (retryCount > 0) {
                    Log.i("apiCallGet", "Failed call to api: $url. Retrying after delay. Retries left: $retryCount")
                    Handler().postDelayed({
                        apiCallGet(url, responseFunction, errorFunction, retryCount - 1)
                    }, 3000) // retry after 3 seconds
                } else {
                    errorFunction()
                    lostConnection()
                    Log.i("apiCallGet", "Error! Failed call to api: $url. Error: $error")
                }
            }
        ) {
            fun getCacheHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Cache-Control"] = "no-cache"
                return headers
            }
        }
        requestQueue.cache.clear()
        requestQueue.add(stringRequest)
    }

    fun apiCallGetArray(
        url: String,
        responseFunction: (eventsJson: JSONArray) -> Unit,
        errorFunction: () -> Unit,
        retryCount: Int = 3 // default retry count is 3
    ) {
        Log.i("apiCallGet", "Calling API at url: $url")
        val requestQueue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(Method.GET, url,
            { response ->
                Log.d("ticketSort", "Får ner GET ARRAY response: $response")
                try {
                    val jsonResponse = JSONObject(response) // Convert response to JSONObject first
                    val ticketsArray = jsonResponse.getJSONArray("tickets") // Access the 'tickets' JSONArray
                    responseFunction(ticketsArray)  // Pass the JSONArray to the response function
                } catch (e: Exception) {
                    Log.e("apiCallGet", "Error in responseFunction: ", e)
                }
                foundConnection()
            },
            { error ->
                if (retryCount > 0) {
                    Log.i("apiCallGet", "Failed call to api: $url. Retrying after delay. Retries left: $retryCount")
                    Handler().postDelayed({
                        apiCallGetArray(url, responseFunction, errorFunction, retryCount - 1)
                    }, 3000) // retry after 3 seconds
                } else {
                    try {
                        errorFunction()
                    } catch (e: Exception) {
                        Log.e("apiCallGet", "Error in errorFunction: ", e)
                    }
                    Log.i("apiCallGet", "Error! Failed call to api: $url. Error: $error")
                    lostConnection()
                }
            }
        ) {
            fun getCacheHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Cache-Control"] = "no-cache"
                return headers
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            20000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.cache.clear()
        requestQueue.add(stringRequest)
    }

    fun apiCallPost(url: String,
                   responseFunction: (eventsJson: JSONObject) -> Unit,
                   errorFunction: () -> Unit,
                   jsonString: String) {
        val mySingleton = MySingleton.getInstance(context)
        Log.i("apiCallPost", "apiCallPost: $jsonString")

        val postRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                // Handle the response from the server
                Log.i("apiCallPost", "Post success! $response")
                responseFunction(JSONObject(response))
                foundConnection()
            },
            Response.ErrorListener { error ->
                // Handle error cases
                if (error is NoConnectionError) {
                    // Handle no connection error
                    Log.i("apiCallPost", "Post error: No internet connection")
                    lostConnection()
                } else {
                    // Handle other error types
                    Log.i("apiCallPost", "Post error: $error")
                    errorFunction()
                    lostConnection()
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
            20000,
            3,
            2f
        )

        // Set the cache to false
        postRequest.setShouldCache(false)
        mySingleton.addToRequestQueue(postRequest)
    }

    fun apiCallPut(url: String, jsonString: String, callback: VolleyCallback) {
        Log.i("apiCallPut", "Skickar PUT: $jsonString till $url")

        val putRequest: StringRequest = object : StringRequest(
            Method.PUT, url,
            Response.Listener { response ->
                // response
                Log.i("apiCallPut", "Received response: $response")
                callback.onSuccess(response)
                foundConnection()
            },
            Response.ErrorListener { error ->
                Log.e("apiCallPut", "Error in request: ${error.message}", error)
                lostConnection()
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

    interface VolleyCallback {
        fun onSuccess(result: String?)
        fun onError(error: String?)
    }

    fun getEventArray(responseJson: JSONObject): MutableList<Event> {
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
                Log.d("DatabaseFunctions", "Added $i event: " + event.title)
                i++
                LocalDatabaseSingleton.eventDatabase.insert(event)
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
        Log.i("getPlayer", "Getting player by id: $playerId")
        val url = "https://www.talltales.nu/API/api/get-player.php?playerId=$playerId"

        apiCallGet(url,
            { responseJson ->
                val jsonElement = Json.parseToJsonElement(responseJson.toString())
                val jsonObject = jsonElement.jsonObject
                val currPlayerId = jsonObject["playerId"]
                if (currPlayerId != null) {
                    val player = json.decodeFromJsonElement<Player>(jsonElement)
                    Log.i("getPlayer", "Successfully got player by id: $currPlayerId")
                    continuation.resume(player)
                } else {
                    Log.e("getPlayer", "'playerId' field missing in response: $responseJson")
                    continuation.resumeWithException(RuntimeException("'playerId' field missing in response"))
                }
            },
            {
                continuation.resumeWithException(RuntimeException("Error getting player with id: $playerId"))

                Log.i("getPlayer", "Could not get player by id: $playerId")
            }
        )
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

    fun setTicketTeamColor(ticket: Ticket, setBlue: Boolean) {
        ticket.teamColor = if (setBlue) "Blue" else "Red"

        // Update database
        updateData(ticket)
        ticketDatabase.update(ticket)
    }

    inline fun <reified T> createJsonString(data: T): String {
        val json = Json { encodeDefaults = true }
        return json.encodeToString(data)
    }

    inline fun <reified T : Any> updateData(data: T) {
        val className = T::class.java.simpleName.lowercase(Locale.ROOT)
//  Log.i("apiCallPut","Updating $className data...")
        val jsonString = createJsonString(data)
        val url = "https://www.talltales.nu/API/api/update-$className.php"
        apiCallPut(url, jsonString, object : VolleyCallback {
            override fun onSuccess(result: String?) {
                Log.i("test", "test")
            }

            override fun onError(error: String?) {
                Log.i("test", "test")
            }
        })
    }

    fun updateTicketArray(tickets : MutableList<Ticket>) {
        ticketDatabase.updateList(tickets)
        Log.i("apiCallPut", "data: $tickets")
        val jsonString = createJsonString(tickets)
        Log.i("apiCallPut", "Updating Ticket Array: $jsonString")
        val url = "https://www.talltales.nu/API/api/update-tickets.php"
        apiCallPut(url, jsonString, object : VolleyCallback {
            override fun onSuccess(result: String?) {
                Log.i("apiCallPut", "Request completed: $result")
            }

            override fun onError(error: String?) {
                Log.i("apiCallPut", "Request error: $error")
                eventView?.checkConnection()
            }
        })
    }

    fun lostConnection() {
        Log.i("Connection", "Lost Connection!")
        currActivity?.connectionProblems = true
        if (eventView != null){
            eventView?.checkConnection()
        }
        if (levelUpView != null){
            levelUpView!!.lostConnection()
        }
    }

    fun foundConnection() {
        if (currActivity == null) {
            Log.i("Connection", "Could not find currActivity")
            return
        }
        if (!currActivity!!.connectionProblems) {
            Log.i("Connection", "Had no connection problems to begin with")
            return
        }

        Log.i("Connection", "Got Connection!")
        var p = false
        if (eventView != null){
            if (eventView!!.allTickets.isNotEmpty()){
                p = true
            }
            eventView!!.checkConnection()
        }
        if (levelUpView != null){
            p = true
        }
        if (p){
            var tickets = listOf<Ticket>()
            currActivity!!.connectionProblems = false
            updateData(currActivity!!.event)
            tickets = ticketDatabase.getByIds(currActivity!!.event.ticketIDs) as List<Ticket>
            updateTicketArray(tickets as MutableList<Ticket>)
        }
    }

//    fun updateEventStatus(currEvent: Event) {
//        if (currEvent.ticketAmount < 1) return
//
//        var ticketTeamDivision = false
//        var ticketCheckIn = false
//
//        for (ticket in allTickets) {
//            if (ticket.teamColor != "") {
//                ticketTeamDivision = true
//            }
//
//            if (ticket.checkedIn == 1) {
//                ticketCheckIn = true
//            }
//
//            if (ticketTeamDivision && ticketCheckIn) break // if both conditions are met, break the loop
//        }
//
//        currEvent.status = when {
//            currEvent.reportText != "" -> "Rapporterat"
//            currEvent.clickWinner != "" || currEvent.gameWinner != "" -> "Avslutat"
//            currEvent.round > 0 -> "Spel påbörjat"
//            ticketCheckIn -> "Checkar in"
//            ticketTeamDivision -> "Lagindelning"
//            else -> "Ej påbörjat"
//        }
////        eventDatabase.update(event) //TODO: remove Local
//        updateData(currEvent)
//        Log.i("status", "Event Status: ${currEvent.status}")
//    }
}