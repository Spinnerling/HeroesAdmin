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
                setEventAdapter(selectedVenue)
            }

        binding.mainActivityBtComingEvents.setOnClickListener {
            setEventAdapter(dropdownMenu.text.toString())
        }

        binding.mainActivityBtPastEvents.setOnClickListener {
            setEventAdapter(dropdownMenu.text.toString())
        }

        binding.refreshButton.setOnClickListener {
            loadEvents()
//            loadEventsLocally()
        }
    }

    private fun loadEvents() {
        binding.eventStatusText.text = "Loading Events..."
        Log.i("checkList", "laddar events")
        DBF.apiCallGet(
            "https://www.talltales.nu/API/api/get-event.php",
            { eventsJson -> getEvents(eventsJson) },
            {
                binding.eventStatusText.text = "Kunde ej koppla upp till databasen"
            }
        )
    }

    private fun saveVenue(venue: String) {
        val editor = sharedPreferences.edit()
        editor.putString(VENUE_KEY, venue)
        editor.apply()
        Log.d("checkList", "Saved venue: $venue")
    }

    private fun loadVenue() {
        val savedVenue = sharedPreferences.getString(VENUE_KEY, null)
        Log.d("checkList", "Loaded venue: $savedVenue")
        savedVenue?.let {
            dropdownMenu.setText(it, false)
        }
    }

    private fun getEvents(eventsJson: JSONObject) {
        eventArray = DBF.getEventArray(eventsJson)
        Log.i("checkList", "eventArray size: ${eventArray.size}")
        eventListList.clear()

        // Create an empty event list for each venue, put into ListList
        for (venue in venues) {
            val eventList: MutableList<Event> = mutableListOf()
            eventListList.add(eventList)
        }

        if (eventListList.size != venues.size) {
            Log.e("checkList", "Did not create all the lists")
        }

        // Divvy up all the events into correct event list
        for (event in eventArray) {
            Log.i("checkList", "Venues: ${event.venue}, Event: ${event.title}")
            // Fix venue name
            when (event.venue) {
                "917" -> event.venue = "Visby"
                "10691" -> event.venue = "Stockholm"
                "23307" -> event.venue = "Göteborg"
                "24643" -> event.venue = "Göteborg"
                "23314" -> event.venue = "Örebro"
                "23312" -> event.venue = "Malmö"
                "23310" -> event.venue = "Uppsala"
            }

            for (i in venues.indices) {
                // If event's venue is correct for the list, add event to the list
                if (event.venue == venues[i]) {
                    eventListList[i].add(event)
                    Log.i("checkList", "Adding event ${event.title} to list for venue: ${venues[i]}")
                }
            }
        }

        // Call setEventAdapter() with the loaded venue
        val loadedVenue = sharedPreferences.getString(VENUE_KEY, null)
        loadedVenue?.let {
            Log.i("checkList", "Loaded venue: $it")
            setEventAdapter(it)
        }
        Log.i(
            "checkList",
            "GetEvent run. Venues: ${venues.size}, eventListList: ${eventListList.size}"
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

    private fun setEventAdapter(venue: String) {
        // Create an empty list to be replaced
        var list = mutableListOf<Event>()

        Log.i("checkList", "Setting adapter. Looking for venue: $venue")

        // Find the list that corresponds to the dropdown menu's option
        if (eventListList.size == venues.size) {
            for (i in venues.indices) {
                if (venue == venues[i]) {
                    list = eventListList[i]
                    Log.i("checkList", "Found matching venue at index: $i, list size: ${list.size}")
                }
            }
        } else {
            Log.e("checkList", "Venues size and eventListList size do not match: ${venues.size} vs ${eventListList.size}")
        }

        // Fill list with events
        eventAdapter = EventRecyclerAdapterKt(list) { position -> onEventItemClick(position) }
        val layoutManager = LinearLayoutManager(v.context)
        val eventList = binding.eventList
        eventList.layoutManager = layoutManager
        eventList.itemAnimator = DefaultItemAnimator()
        eventList.adapter = eventAdapter
    }

    private fun checkList() {
        val selectedVenue = dropdownMenu.text.toString()
        var list = mutableListOf<Event>()

        // Find the list that corresponds to the dropdown menu's option
        for (i in venues.indices) {
            if (selectedVenue == venues[i]) {
                list = eventListList[i]
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