package com.example.heroadmin

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.heroadmin.databinding.FragmentEventListBinding
import org.json.JSONObject
import kotlinx.serialization.json.Json
import org.json.JSONArray

class EventList : Fragment() {
    // Initialize the binding object
    private lateinit var binding: FragmentEventListBinding
    private lateinit var v: View
    private lateinit var eventAdapter: EventRecyclerAdapterKt
    private lateinit var eventArray: MutableList<Event>
    private var eventListList: MutableList<MutableList<Event>> = mutableListOf()
    private lateinit var venues: Array<String>
    private lateinit var dropdownMenu: AutoCompleteTextView
    private lateinit var DBF: DatabaseFunctions
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private var displayPastEvents: Boolean = false
    private val SHARED_PREFS = "sharedPrefs"
    private val VENUE_KEY = "venue"
    val eventDatabase = LocalDatabaseSingleton.eventDatabase

    private lateinit var sharedPreferences: SharedPreferences

    override fun onResume() {
        super.onResume()

        sharedPreferences =
            requireActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        dropdownMenu = binding.dropDownMenu
        venues = resources.getStringArray(R.array.venues)
        val venuesArrayAdapter = ArrayAdapter(v.context, R.layout.dropdown_item, venues)
        dropdownMenu.setAdapter(venuesArrayAdapter)

        // Load the saved venue, if any
        loadVenue()

//        createSampleEvents()
        //loadEventsLocally()
        loadEvents()

        // Set an onItemSelectedListener for the dropdownMenu
        dropdownMenu.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedVenue = parent.getItemAtPosition(position).toString()
                saveVenue(selectedVenue)
                setEventAdapter(selectedVenue, displayPastEvents)
            }

        binding.mainActivityBtComingEvents.setOnClickListener {
            setEventAdapter(dropdownMenu.text.toString(), false)
        }

        binding.mainActivityBtPastEvents.setOnClickListener {
            setEventAdapter(dropdownMenu.text.toString(), true)
        }

        binding.refreshButton.setOnClickListener {
            loadEvents()
//            loadEventsLocally()
        }
    }

    private fun loadEvents() {
        binding.eventStatusText.text = "Loading Events..."
        DBF.apiCallGet(
            "https://www.talltales.nu/API/api/get-event.php",
            { eventsJson -> getEvents(eventsJson, json) },
            {
                binding.eventStatusText.text = "Kunde ej koppla upp till databasen"
            }
        )
    }

    private fun loadEventsLocally() {
        binding.eventStatusText.text = "Loading Events Locally..."

        val events = eventDatabase.getAll()
        val eventsJsonArray = JSONArray()

        for (event in events) {
            val eventJson = JSONObject(this.eventDatabase.toJson(event))
            eventsJsonArray.put(eventJson)
        }

        val eventsJsonObject = JSONObject().apply {
            put(
                "data",
                eventsJsonArray
            ) // Change "events" to "data" to match the expected key in getEventArray
        }

        getEvents(eventsJsonObject, Json)
    }

    private fun saveVenue(venue: String) {
        val editor = sharedPreferences.edit()
        editor.putString(VENUE_KEY, venue)
        editor.apply()
        Log.d("EventList", "Saved venue: $venue")
    }

    private fun loadVenue() {
        val savedVenue = sharedPreferences.getString(VENUE_KEY, null)
        Log.d("EventList", "Loaded venue: $savedVenue")
        savedVenue?.let {
            dropdownMenu.setText(it, false)
        }
    }

    private fun getEvents(eventsJson: JSONObject, json: Json) {
        eventArray = DBF.getEventArray(eventsJson, json)
        eventListList.clear()

        // Create an empty event list for each venue and type (past and future), put into ListList
        for (venue in venues) {
            val pastList: MutableList<Event> = mutableListOf()
            val futureList: MutableList<Event> = mutableListOf()
            eventListList.add(pastList)
            eventListList.add(futureList)
        }

        if (eventListList.size != venues.size * 2) {
            Toast.makeText(context, "Did not get all the lists", Toast.LENGTH_SHORT).show()
        }

        val currentTime = System.currentTimeMillis()

        // Divvy up all the events into correct event list (past or future)
        for (event in eventArray) {

            Log.i("check", "Venues: ${event.venue}, Event: ${event.title}")
            // Fix venue name
            when (event.venue) {
                "917" -> event.venue = "Visby"
                "10691" -> event.venue = "Stockholm"
                "23307" -> event.venue = "Göteborg"
                "2643" -> event.venue = "Göteborg"
                "23314" -> event.venue = "Örebro"
                "23312" -> event.venue = "Malmö"
                "23310" -> event.venue = "Uppsala"
            }

            for (i in venues.indices) {
                // If event's venue is correct for the list, add event to past or future list
                if (event.venue == venues[i]) {
                    if (event.startTime!! < currentTime.toString()) {
                        eventListList[i * 2].add(event) // Add to past list
                    } else {
                        eventListList[i * 2 + 1].add(event) // Add to future list
                    }
                }
            }
        }
        // Call setEventAdapter() with the loaded venue
        val loadedVenue = sharedPreferences.getString(VENUE_KEY, null)
        loadedVenue?.let {
            setEventAdapter(it, displayPastEvents)
        }
        Log.i(
            "test",
            "GetEvent run. Venues: " + venues.size.toString() + " " + eventListList.size.toString()
        )

        checkList()
    }

    private fun noEventConnection() {
        callNotification("Could not access database")
    }

    private fun callNotification(message: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.note_popup, null)

        val builder = AlertDialog.Builder(context).setView(dialogView)

        val notification = builder.show()

        val textHolder: TextView = dialogView.findViewById(R.id.notePopupText)
        textHolder.text = message

        dialogView.findViewById<Button>(R.id.notePopupAcceptButton).setOnClickListener {
            notification.dismiss()
        }
    }

    private fun setEventAdapter(venue: String, displayPastEvents: Boolean) {
        // Create an empty list to be replaced
        var list = mutableListOf<Event>()

        // Find the list that corresponds to the dropdown menu's option
        if (eventListList.size == venues.size * 2) {
            for (i in venues.indices) {
                var string = dropdownMenu.text.toString()
                if (string == venues[i]) {
                    list = if (displayPastEvents) {
                        eventListList[i * 2] // Use past event list
                    } else {
                        eventListList[i * 2 + 1] // Use future event list
                    }
                }
            }
        }

        // Fill list with events
        eventAdapter = EventRecyclerAdapterKt(list) { position -> onEventItemClick(position) }
        val layoutManager = LinearLayoutManager(v.context)
        val eventList = binding.eventList
        eventList.layoutManager = layoutManager
        eventList.itemAnimator = DefaultItemAnimator()
        eventList.adapter = eventAdapter
        Log.i("checkList", "Setting adapter for venue: $venue, list size: ${list.size}")

        checkList()
    }

    private fun checkList() {
        val selectedVenue = dropdownMenu.text.toString()
        var list = mutableListOf<Event>()

        // Find the list that corresponds to the dropdown menu's option
        for (i in venues.indices) {
            if (selectedVenue == venues[i]) {
                list = if (displayPastEvents) {
                    eventListList[i * 2] // Use past event list
                } else {
                    eventListList[i * 2 + 1] // Use future event list
                }
            }
        }

        // Set text visibility
        if (list.isNotEmpty()) {
            binding.eventStatusText.visibility = View.INVISIBLE
        } else {
            binding.eventStatusText.visibility = View.VISIBLE
        }
    }


    private fun onEventItemClick(position: Int) {
        val event = eventAdapter.list[position]
        (activity as MainActivity).event = event
        findNavController().navigate(EventListDirections.actionEventListToEventAdminFrag(event.eventId))
    }

    private fun createSampleEvents() {
        // Create sample event 1
        val event1 = Event(
            eventId = "1",
            title = "Sample Event 1",
            startTime = "2024-05-11T10:00:00",
            endTime = "2024-05-11T12:00:00",
            venue = "Stockholm"
        )
        val ticketIdsList = listOf(
            "T1",
            "T2",
            "T3",
            "T4",
            "T5",
            "T6",
            "T7",
            "T0",
            "T11",
            "T12",
            "T13",
            "T14",
            "T15",
            "T16",
            "T17",
            "T18"
        )
        event1.ticketIDs.addAll(ticketIdsList)
        eventDatabase.insert(event1)

        // Create sample event 2
        val event2 = Event(
            eventId = "2",
            title = "Sample Event 2",
            startTime = "2024-05-12T14:00:00",
            endTime = "2024-05-12T16:00:00",
            venue = "Visby"
        )
        event2.ticketIDs.addAll(ticketIdsList)
        eventDatabase.insert(event2)
        // Create more sample events as needed and insert them into localEventDatabase
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_list, container, false)
        v = inflater.inflate(R.layout.fragment_event_list, container, false)
        DBF = DatabaseFunctions(v.context)

        return binding.root
    }

}