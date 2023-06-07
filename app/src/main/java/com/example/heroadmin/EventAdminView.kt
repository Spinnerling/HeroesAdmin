package com.example.heroadmin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.heroadmin.databinding.FragmentEventAdminBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EventAdminFragment : Fragment() {
    private lateinit var currActivity: MainActivity
    private lateinit var binding: FragmentEventAdminBinding
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
//    private lateinit var args: EventAdminViewArgs
    private lateinit var event: Event

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventAdminBinding.inflate(inflater, container, false)
        v = inflater.inflate(R.layout.fragment_event_admin, container, false)

        currActivity = (activity as MainActivity)
        event = currActivity.event

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        DBF = DatabaseFunctions(v.context)

        loadEventData()
        setupSubmitButton()

        binding.eventAdminEditValues.setOnClickListener {
            if (binding.eventAdminAttendanceValue.visibility == View.VISIBLE) {
                changeExpValues()
            }
            else {
                saveExpValues()
            }
        }
    }

    private fun loadEventData() {
        this.DBF.apiCallGet(
            url = "<API_URL>",
            responseFunction = { eventData ->
                runBlocking {
                    binding.eventAdminEventNameText.text = eventData.getString("name")
                    binding.eventAdminEventVenue.text = eventData.getString("venue")
                    binding.eventAdminEventDate.text = eventData.getString("date")
                    binding.eventAdminEventTime.text = eventData.getString("time")
                    binding.eventAdminAttendanceValue.setText(eventData.getString("attendance_value"))
                    binding.eventAdminRecruitmentValue.setText(eventData.getString("recruitment_value"))
                    binding.eventAdminWin1Value.setText(eventData.getString("winning_value"))
                    binding.eventAdminTeamChangeValue.setText(eventData.getString("changing_team_value"))
                    binding.eventAdminReportText.setText(eventData.getString("report_text"))
                }
            },
            errorFunction = {
                // Handle error case here
            }
        )
    }

    private fun setupSubmitButton() {
        binding.eventAdminToEventListButton.setOnClickListener {
            saveInfo()
        }
        binding.eventAdminToEventViewButton.setOnClickListener {
            saveInfo()
        }
    }

    private fun saveInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            event.apply {
                reportText = binding.eventAdminReportText.text.toString()
                expAttendanceValue = binding.eventAdminAttendanceValue.text.toString().toInt()
                expRecruitValue = binding.eventAdminRecruitmentValue.text.toString().toInt()
                expClickWinValue = binding.eventAdminWin1Value.text.toString().toInt()
                expGameWinValue = binding.eventAdminWin2Value.text.toString().toInt()
                expTeamChangeValue = binding.eventAdminTeamChangeValue.text.toString().toInt()
            }

            val jsonData = Json.encodeToString(event)
//            this@EventAdminFragment.DBF.apiCallPut("<API_URL>", jsonData)
        }
    }

    private fun changeExpValues() {
        binding.eventAdminAttendanceValue.visibility = View.GONE
        binding.eventAdminEditAttendance.visibility = View.VISIBLE

        binding.eventAdminWin1Value.visibility = View.GONE
        binding.eventAdminEditWin1.visibility = View.VISIBLE

        binding.eventAdminWin2Value.visibility = View.GONE
        binding.eventAdminEditWin2.visibility = View.VISIBLE

        binding.eventAdminRecruitmentValue.visibility = View.GONE
        binding.eventAdminEditRecruitment.visibility = View.VISIBLE

        binding.eventAdminTeamChangeValue.visibility = View.GONE
        binding.eventAdminEditTeamChange.visibility = View.VISIBLE

        binding.eventAdminEditValues.text = "Spara"
        binding.eventAdminEditValues.setBackgroundResource(R.color.buttonGreen)
    }

    private fun saveExpValues() {
        binding.eventAdminAttendanceValue.visibility = View.GONE
        binding.eventAdminEditAttendance.visibility = View.VISIBLE

        binding.eventAdminWin1Value.visibility = View.GONE
        binding.eventAdminEditWin1.visibility = View.VISIBLE

        binding.eventAdminWin2Value.visibility = View.GONE
        binding.eventAdminEditWin2.visibility = View.VISIBLE

        binding.eventAdminRecruitmentValue.visibility = View.GONE
        binding.eventAdminEditRecruitment.visibility = View.VISIBLE

        binding.eventAdminTeamChangeValue.visibility = View.GONE
        binding.eventAdminEditTeamChange.visibility = View.VISIBLE

        binding.eventAdminEditValues.text = "Ändra"
        binding.eventAdminEditValues.setBackgroundResource(R.color.white)
    }
}
