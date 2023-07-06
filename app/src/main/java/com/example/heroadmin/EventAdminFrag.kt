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
            }
            else {
                saveExpValues()
            }
        }

        binding.eventAdminWin1NoWinner.setOnClickListener {
            currClickWinner = ""
            updateWinnerButtonColors()
        }

        binding.eventAdminWin1RedButton.setOnClickListener {
            currClickWinner = "Red"
            updateWinnerButtonColors()
        }

        binding.eventAdminWin1BlueButton.setOnClickListener {
            currClickWinner = "Blue"
            updateWinnerButtonColors()
        }

        binding.eventAdminWin1TieButton.setOnClickListener {
            currClickWinner = "Tie"
            updateWinnerButtonColors()
        }

        binding.eventAdminWin2NoWinner.setOnClickListener {
            currGameWinner = ""
            updateWinnerButtonColors()
        }

        binding.eventAdminWin2RedButton.setOnClickListener {
            currGameWinner = "Red"
            updateWinnerButtonColors()
        }

        binding.eventAdminWin2BlueButton.setOnClickListener {
            currGameWinner = "Blue"
            updateWinnerButtonColors()
        }

        binding.eventAdminWin2TieButton.setOnClickListener {
            currGameWinner = "Tie"
            updateWinnerButtonColors()
        }

        binding.eventAdminToEventListButton.setOnClickListener {
            findNavController().navigate(EventAdminFragDirections.actionEventAdminFragToEventList())
            saveInfo()
        }

        binding.eventAdminToEventViewButton.setOnClickListener {
            findNavController().navigate(EventAdminFragDirections.actionEventAdminFragToEventView(event.eventId))
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

        currClickWinner = event.clickWinner.toString()
        currGameWinner = event.gameWinner.toString()
        updateWinnerButtonColors()
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
                expAttendanceValue = binding.eventAdminAttendanceValue.text.toString().toIntOrNull() ?: 0
                expRecruitValue = binding.eventAdminRecruitmentValue.text.toString().toIntOrNull() ?: 0
                expClickWinValue = binding.eventAdminWin1Value.text.toString().toIntOrNull() ?: 0
                expGameWinValue = binding.eventAdminWin2Value.text.toString().toIntOrNull() ?: 0
                expTeamChangeValue = binding.eventAdminTeamChangeValue.text.toString().toIntOrNull() ?: 0
                clickWinner = currClickWinner
                gameWinner = currGameWinner
            }

            DBF.updateData(event)
//            DBF.updateEventStatus(event)
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
        binding.eventAdminEditValues.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.buttonGreen))
        binding.eventAdminEditValues.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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
        binding.eventAdminEditValues.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.eventAdminEditValues.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green))
        binding.layout3.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_green_medium))

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
        val noWinnerUnselectedColor = ContextCompat.getColor(requireContext(), R.color.grey)
        val noWinnerColor = ContextCompat.getColor(requireContext(), R.color.white)
        val redUnselectedColor = ContextCompat.getColor(requireContext(), R.color.light_pink)
        val redWinnerColor = ContextCompat.getColor(requireContext(), R.color.winning_red)
        val blueUnselectedColor = ContextCompat.getColor(requireContext(), R.color.light_blue)
        val blueWinnerColor = ContextCompat.getColor(requireContext(), R.color.winning_blue)
        val tieActiveColor = ContextCompat.getColor(requireContext(), R.color.purple_deep)
        val tieUnselectedColor = ContextCompat.getColor(requireContext(), R.color.purple_200)

        binding.eventAdminWin1NoWinner.apply {
            setBackgroundColor(if (currClickWinner == "") noWinnerColor else noWinnerUnselectedColor)
            elevation = if (currClickWinner == "") 10f else 0f
            alpha = if (currClickWinner == "") 1f else 0.7f
        }

        binding.eventAdminWin1RedButton.apply {
            setBackgroundColor(if (currClickWinner == "Red") redWinnerColor else redUnselectedColor)
            elevation = if (currClickWinner == "Red") 10f else 0f
            alpha = if (currClickWinner == "Red") 1f else 0.7f
        }

        binding.eventAdminWin1BlueButton.apply {
            setBackgroundColor(if (currClickWinner == "Blue") blueWinnerColor else blueUnselectedColor)
            elevation = if (currClickWinner == "Blue") 10f else 0f
            alpha = if (currClickWinner == "Blue") 1f else 0.7f
        }

        binding.eventAdminWin1TieButton.apply {
            setBackgroundColor(if (currClickWinner == "Tie") tieActiveColor else tieUnselectedColor)
            elevation = if (currClickWinner == "Tie") 10f else 0f
            alpha = if (currClickWinner == "Tie") 1f else 0.7f
        }

        binding.eventAdminWin2NoWinner.apply {
            setBackgroundColor(if (currGameWinner == "") noWinnerColor else noWinnerUnselectedColor)
            elevation = if (currGameWinner == "") 10f else 0f
            alpha = if (currGameWinner == "") 1f else 0.7f
        }

        binding.eventAdminWin2RedButton.apply {
            setBackgroundColor(if (currGameWinner == "Red") redWinnerColor else redUnselectedColor)
            elevation = if (currGameWinner == "Red") 10f else 0f
            alpha = if (currGameWinner == "Red") 1f else 0.7f
        }

        binding.eventAdminWin2BlueButton.apply {
            setBackgroundColor(if (currGameWinner == "Blue") blueWinnerColor else blueUnselectedColor)
            elevation = if (currGameWinner == "Blue") 10f else 0f
            alpha = if (currGameWinner == "Blue") 1f else 0.7f
        }

        binding.eventAdminWin2TieButton.apply {
            setBackgroundColor(if (currGameWinner == "Tie") tieActiveColor else tieUnselectedColor)
            elevation = if (currGameWinner == "Tie") 10f else 0f
            alpha = if (currGameWinner == "Tie") 1f else 0.7f
        }

//        DBF.updateEventStatus(event)
    }

    private fun dismissKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}