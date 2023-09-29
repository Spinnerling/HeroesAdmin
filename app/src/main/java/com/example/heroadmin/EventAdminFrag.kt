package com.example.heroadmin

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.heroadmin.databinding.FragmentEventAdminBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EventAdminFrag : Fragment() {
    private lateinit var currActivity: MainActivity
    private lateinit var binding: FragmentEventAdminBinding
    private lateinit var v: View
    private lateinit var DBF: DatabaseFunctions
    private lateinit var args: EventAdminFragArgs
    private lateinit var event: Event
    private var currClickWinner: String = ""
    private var currGameWinner: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventAdminBinding.inflate(inflater, container, false)
        v = inflater.inflate(R.layout.fragment_event_admin, container, false)
        args = EventAdminFragArgs.fromBundle(requireArguments())
        currActivity = (activity as MainActivity)
        event = currActivity.event

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        DBF = DatabaseFunctions(v.context)

        loadEventData()
        currClickWinner = event.clickWinner ?: "" // Initialize with the event's click winner
        currGameWinner = event.gameWinner ?: "" // Initialize with the event's game winner

        // Set initial button colors based on the winner states
        updateWinnerButtonColors()

        binding.eventAdminEditValues.setOnClickListener {
            if (binding.eventAdminAttendanceValue.visibility == View.VISIBLE) {
                changeExpValues()
            } else {
                saveExpValues()
            }
        }

        binding.eventAdminToEventListButton.setOnClickListener {
            findNavController().navigate(EventAdminFragDirections.actionEventAdminFragToEventList())
            saveInfo()
        }

        binding.eventAdminToEventViewButton.setOnClickListener {
            findNavController().navigate(
                EventAdminFragDirections.actionEventAdminFragToEventView(
                    event.eventId
                )
            )
            saveInfo()
        }
    }

    private fun loadEventData() {
        updateVenue()
        binding.eventAdminEventNameText.text = event.title
        binding.eventAdminEventVenue.text = event.venue
        binding.eventAdminEventDate.text = event.actualDate
        binding.eventAdminEventTime.text = event.actualStartTime
        binding.eventAdminEventId.text = event.eventId
        binding.eventAdminReportText.setText(event.reportText ?: "")

        binding.eventAdminAttendanceValue.text = event.expAttendanceValue?.toString() ?: "20"
        binding.eventAdminEditAttendance.setText(event.expAttendanceValue?.toString() ?: "20")

        binding.eventAdminRecruitmentValue.text = event.expRecruitValue?.toString() ?: "20"
        binding.eventAdminEditRecruitment.setText(event.expRecruitValue?.toString() ?: "20")

        binding.eventAdminWin1Value.text = event.expClickWinValue?.toString() ?: "10"
        binding.eventAdminEditWin1.setText(event.expClickWinValue?.toString() ?: "10")

        binding.eventAdminWin2Value.text = event.expGameWinValue?.toString() ?: "5"
        binding.eventAdminEditWin2.setText(event.expGameWinValue?.toString() ?: "5")

        binding.eventAdminTeamChangeValue.text = event.expTeamChangeValue?.toString() ?: "5"
        binding.eventAdminEditTeamChange.setText(event.expTeamChangeValue?.toString() ?: "5")

        currClickWinner = event.clickWinner
        currGameWinner = event.gameWinner
    }

    private fun updateVenue() {
        when (event.venue) {
            "917" -> event.venue = "Visby"
            "10691" -> event.venue = "Stockholm"
            "23307" -> event.venue = "Göteborg"
            "24643" -> event.venue = "Göteborg"
            "23314" -> event.venue = "Örebro"
            "23312" -> event.venue = "Malmö"
            "23310" -> event.venue = "Uppsala"
        }
    }

    private fun saveInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            event.apply {
                reportText = binding.eventAdminReportText.text.toString()
                expAttendanceValue =
                    binding.eventAdminAttendanceValue.text.toString().toIntOrNull() ?: 0
                expRecruitValue =
                    binding.eventAdminRecruitmentValue.text.toString().toIntOrNull() ?: 0
                expClickWinValue = binding.eventAdminWin1Value.text.toString().toIntOrNull() ?: 0
                expGameWinValue = binding.eventAdminWin2Value.text.toString().toIntOrNull() ?: 0
                expTeamChangeValue =
                    binding.eventAdminTeamChangeValue.text.toString().toIntOrNull() ?: 0
                clickWinner = currClickWinner
                gameWinner = currGameWinner
            }

            DBF.updateData(event)
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
        binding.eventAdminEditValues.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.buttonGreen
            )
        )
        binding.eventAdminEditValues.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.layout3.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun saveExpValues() {
        binding.eventAdminAttendanceValue.visibility = View.VISIBLE
        binding.eventAdminEditAttendance.visibility = View.GONE

        binding.eventAdminWin1Value.visibility = View.VISIBLE
        binding.eventAdminEditWin1.visibility = View.GONE

        binding.eventAdminWin2Value.visibility = View.VISIBLE
        binding.eventAdminEditWin2.visibility = View.GONE

        binding.eventAdminRecruitmentValue.visibility = View.VISIBLE
        binding.eventAdminEditRecruitment.visibility = View.GONE

        binding.eventAdminTeamChangeValue.visibility = View.VISIBLE
        binding.eventAdminEditTeamChange.visibility = View.GONE

        binding.eventAdminEditValues.text = "Ändra"
        binding.eventAdminEditValues.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.eventAdminEditValues.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary_green
            )
        )
        binding.layout3.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.background_green_medium
            )
        )

        event.apply {
            expAttendanceValue = binding.eventAdminEditAttendance.text.toString().toIntOrNull() ?: 0
            expRecruitValue = binding.eventAdminEditRecruitment.text.toString().toIntOrNull() ?: 0
            expClickWinValue = binding.eventAdminEditWin1.text.toString().toIntOrNull() ?: 0
            expGameWinValue = binding.eventAdminEditWin2.text.toString().toIntOrNull() ?: 0
            expTeamChangeValue = binding.eventAdminEditTeamChange.text.toString().toIntOrNull() ?: 0

        }
        saveInfo()
        dismissKeyboard()
        loadEventData()
    }

    private fun updateWinnerButtonColors() {
        val noWinnerColor = ContextCompat.getColor(requireContext(), R.color.white)
        val redColor = ContextCompat.getColor(requireContext(), R.color.winning_red)
        val blueColor = ContextCompat.getColor(requireContext(), R.color.winning_blue)
        val tieColor = ContextCompat.getColor(requireContext(), R.color.purple_deep)

        fun getColor(winner: String): Int {
            return when (winner) {
                "Red" -> redColor
                "Blue" -> blueColor
                "Tie" -> tieColor
                else -> noWinnerColor
            }
        }

        val clickColor = getColor(event.clickWinner)
        val gameColor = getColor(event.gameWinner)

        binding.eventAdminWin1.apply {
            setBackgroundColor(clickColor)
            elevation = 0f
            alpha = 0.7f
        }

        binding.eventAdminWin2.apply {
            setBackgroundColor(gameColor)
            elevation = 0f
            alpha = 0.7f
        }

    }

    private fun dismissKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}