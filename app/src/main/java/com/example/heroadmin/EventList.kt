package com.example.heroadmin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.heroadmin.databinding.FragmentEventListBinding

class EventList : Fragment() {
    // Initialize the binding object
    private lateinit var binding : FragmentEventListBinding
    private lateinit var v : View
    private lateinit var eventAdapter : EventRecyclerAdapterKt
    private lateinit var eventArray : MutableList<Event?>


    override fun onResume() {
        super.onResume()

        val dropdownMenu = binding.dropDownMenu
        val venues = resources.getStringArray(R.array.venues)
        val venuesArrayAdapter = ArrayAdapter(v.context, R.layout.dropdown_item, venues)
        dropdownMenu.setAdapter(venuesArrayAdapter)

        getEvents()

        setEventAdapter()

        if (eventArray.isNotEmpty()) {
            binding.noEventsText.visibility = View.INVISIBLE
        }
        else {
            binding.noEventsText.visibility = View.VISIBLE
        }
    }

    private fun getEvents() {
        eventArray = mutableListOf(
        Event("A123", "Stockholm", "24/3 -22", "2020", "2021", "15:09", "IN PLAY", "", "Torsdagsspel", "Kom och programmera, din blötvattensfisk!", listOf("ticket123"),0, 32,35,0, 0,10,2,5 ),
        Event("A124", "Visby", "25/3 -22", "2020", "2021", "13:00", "DONE", "", "Fredagsspel", "Kom och programmera, din blötvattensfisk!",  listOf("ticket123"),0, 34,35,0, 0,10,2,5 ),
        Event("A125", "Göteborg",  "26/3 -22", "2020", "2021", "15:00", "UNREPORTED", "", "Hunger Games", "Kom och programmera, din blötvattensfisk!", listOf("ticket123"), 0, 35,35,0, 0,10,2,5 )
        )
    }


    private fun setEventAdapter() {
        eventAdapter = EventRecyclerAdapterKt(eventArray){ position -> onEventItemClick(position)}
        val layoutManager = LinearLayoutManager(v.context)
        val eventList = binding.eventList
        eventList.layoutManager = layoutManager
        eventList.itemAnimator = DefaultItemAnimator()
        eventList.adapter = eventAdapter
    }

    private fun onEventItemClick(position : Int) {
        //val args =  EventListArgs.fromBundle(requireArguments())
        val event = eventArray[position]?.eventId.toString()
        findNavController().navigate(EventListDirections.actionEventListToEventView(event))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_list, container, false)
        v = inflater.inflate(R.layout.fragment_event_list, container, false)


        return binding.root
    }

}