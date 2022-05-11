package com.example.heroadmin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.example.heroadmin.databinding.FragmentEventListBinding
import org.json.JSONObject

class EventList : Fragment() {
    // Initialize the binding object
    private lateinit var binding: FragmentEventListBinding
    private lateinit var v: View
    private lateinit var eventAdapter: EventRecyclerAdapterKt
    private lateinit var eventArray: MutableList<Event>
    private var eventListList: MutableList<MutableList<Event>> = mutableListOf()
    private lateinit var venues: Array<String>
    private lateinit var dropdownMenu: AutoCompleteTextView
    private lateinit var DBF : DatabaseFunctions

    override fun onResume() {
        super.onResume()

        dropdownMenu = binding.dropDownMenu
        venues = resources.getStringArray(R.array.venues)
        val venuesArrayAdapter = ArrayAdapter(v.context, R.layout.dropdown_item, venues)
        dropdownMenu.setAdapter(venuesArrayAdapter)

        dropdownMenu.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            setEventAdapter()
        }

        DBF.apiCallGet(
            "https://talltales.nu/API/api/eventlist.php",
            ::getEvents
        )
        //setEventAdapter()
    }

    private fun getEvents(eventsJson: JSONObject) {
        eventArray = DBF.getEventArray(eventsJson)

        // Create an empty event list for each venue, put into ListList
        for (venue in venues) {
            val list: MutableList<Event> = mutableListOf()
            eventListList.add(list)
        }

        // Divvy up all the events into correct event list
        for (event in eventArray) {

            // Fix venue name
            if (event.venue == "917" || event.venue == "Visby") {
                event.venue = "Visby"
            } else {
                event.venue = "Stockholm"
            }

            for (i in venues.indices) {
                // If event's venue is correct for the list, add event to list
                if (event.venue == venues[i]) {
                    eventListList[i].add(event)
                }
            }
        }
        Log.i("test", "GetEvent run. Venues: " + venues.size.toString() + " " + eventListList.size.toString())
    }

    private fun noEventConnection() {

    }

    private fun setEventAdapter() {
        // Create an empty list to be replaced
        var list = mutableListOf<Event>()

        // Find the list that corresponds to the dropdown menu's option
        for (i in venues.indices) {
            var string = dropdownMenu.text.toString()
            if (string == venues[i]) {
                Log.i("test", string + " is venue")
                list = eventListList[i]
            }
        }

        // Fill list with events
        eventAdapter = EventRecyclerAdapterKt(list) { position -> onEventItemClick(position) }
        val layoutManager = LinearLayoutManager(v.context)
        val eventList = binding.eventList
        eventList.layoutManager = layoutManager
        eventList.itemAnimator = DefaultItemAnimator()
        eventList.adapter = eventAdapter

        // Set text visibility
        if (list.isNotEmpty()) {
            binding.noEventsText.visibility = View.INVISIBLE
        } else {
            binding.noEventsText.visibility = View.VISIBLE
        }
    }

    private fun onEventItemClick(position: Int) {
        val event = eventArray[position].eventId
        (activity as MainActivity).event = eventArray[position]
        findNavController().navigate(EventListDirections.actionEventListToEventView(event))
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