package com.example.heroadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class eventRecyclerAdapter extends RecyclerView.Adapter<eventRecyclerAdapter.MyViewHolder> {
    private ArrayList<Event> eventArray;
    private eventClickListener listener;

    public eventRecyclerAdapter(ArrayList<Event> eventArray, eventClickListener listener){
        this.eventArray = eventArray;
        this.listener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView dateText;
        private TextView timeText;
        private TextView playerAmountText;
        private TextView statusText;

        public MyViewHolder(final View view) {
             super(view);
             dateText = view.findViewById(R.id.event_text_date);
             timeText = view.findViewById(R.id.event_text_time);
             playerAmountText = view.findViewById(R.id.event_text_playerAmount);
             statusText = view.findViewById(R.id.event_text_status);
             view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }


    @NonNull
    @Override
    public eventRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull eventRecyclerAdapter.MyViewHolder holder, int position) {

        Event event = eventArray.get(position);
        String date = event.getActualDate();
        holder.dateText.setText(date);
        String time = event.getActualStartTime();
        holder.timeText.setText(time);
        int playerAmount = event.getPlayerAmount();
        holder.playerAmountText.setText(Integer.toString(playerAmount));
        String status = event.getStatus();
        holder.statusText.setText(status);
    }

    @Override
    public int getItemCount() {
        return eventArray.size();
    }

    public interface eventClickListener{
    void onClick(View v, int position);
    }
}
