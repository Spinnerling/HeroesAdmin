package com.example.heroadmin

import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat.getExtras
import androidx.databinding.DataBindingUtil
import com.example.heroadmin.databinding.FragmentEventViewBinding

class EventView : Fragment() {
    private lateinit var binding : FragmentEventViewBinding
    private lateinit var v : View
    private lateinit var eventId : String

    override fun onResume() {
        super.onResume()

        val assignTeamPanelButton : Button = binding.assignTeamPanelButton
        val assignTeamList : LinearLayout = binding.assignTeamList

        val checkInPanelButton : Button = binding.checkInPanelButton
        val checkInList : LinearLayout = binding.checkInList

        assignTeamPanelButton.setOnClickListener {
            if (assignTeamList.layoutParams.height == 0) {
                assignTeamList.setLayoutParams(
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_open, 0, 0, 0)
            }
            else {
                assignTeamList.layoutParams.height = 0
                assignTeamPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_closed, 0, 0, 0)
            }
        }

        checkInPanelButton.setOnClickListener {
            if (checkInList.layoutParams.height == 0) {
                checkInList.setLayoutParams(
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_open, 0, 0, 0)
            }
            else {
                checkInList.layoutParams.height = 0
                checkInPanelButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_list_closed, 0, 0, 0)
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_view, container, false)
        v = inflater.inflate(R.layout.fragment_event_view, container, false)
        eventId = "not found"

        val extras : Bundle? = getIntent().getExtras("Sdasd")

        return binding.root
    }

}