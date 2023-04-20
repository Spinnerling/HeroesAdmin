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
import org.json.JSONObject
import kotlinx.coroutines.runBlocking

class EventAdminFragment : Fragment() {
    private lateinit var DBF: DatabaseFunctions
    private var _binding: FragmentEventAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventAdminBinding.inflate(inflater, container, false)
        v = inflater.inflate(R.layout.fragment_event_admin, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        DBF = DatabaseFunctions(v.context)

        loadEventData()
        setupSubmitButton()
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
                    binding.eventAdminWinningValue.setText(eventData.getString("winning_value"))
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
            val data = JSONObject().apply {
                put("name", binding.eventAdminEventNameText.text.toString())
                put("venue", binding.eventAdminEventVenue.text.toString())
                put("date", binding.eventAdminEventDate.text.toString())
                put("time", binding.eventAdminEventTime.text.toString())
                put("attendance_value", binding.eventAdminAttendanceValue.text.toString())
                put("recruitment_value", binding.eventAdminRecruitmentValue.text.toString())
                put("winning_value", binding.eventAdminWinningValue.text.toString())
                put("changing_team_value", binding.eventAdminTeamChangeValue.text.toString())
                put("report_text", binding.eventAdminReportText.text.toString())
            }

            this@EventAdminFragment.DBF.apiCallPost("<API_URL>", data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
