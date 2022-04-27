package com.example.heroadmin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class EventRecyclerAdapterKt(private val eventArray: MutableList<Event>, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<EventListViewHolder>()  {

    override fun getItemCount(): Int {
        return if (eventArray.isEmpty()) 0 else eventArray!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListViewHolder{
    val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.event_item, parent, false)
        return EventListViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: EventListViewHolder, position: Int) {
        // is called to attach data to a ViewHolder. Here you change the text, color, whatever needs to be done to the list item views according to the data being displayed. I created a bind function in the ViewHolder earlier for convenience which I use here, if you want you could do everything in the bind function here instead.
        val event = eventArray[position]
        val title = event.title
        holder.titleText.text = title
        val date = event.actualDate
        holder.dateText.text = date
        val time = event.actualStartTime
        holder.timeText.text = time
        val playerAmount = event.playerAmount
        holder.playerAmountText.text = "$playerAmount players"
        val status = event.status
        holder.statusText.text = status
    }

}