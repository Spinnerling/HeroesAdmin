package com.example.heroadmin

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    @SerialName("eventID") var eventId: String,
    var title: String,
    var startTime: String,
    var endTime: String,
    var venue: String? = "",
    var reportText: String? = null,
    var description: String = "",
    var clickWinner: String = "",
    var gameWinner: String = "",
    var expAttendanceValue: Int = 25,
    var expClickWinValue: Int = 5,
    var expGameWinValue: Int = 10,
    var expTeamChangeValue: Int = 5,
    var expRecruitValue: Int = 20,
    var blueGameWins: Int = 0,
    var redGameWins: Int = 0,
    var round: Int = 0,
    var status: String? = "Ej påbörjat",
    @SerialName("tickets") var ticketIDs: MutableList<String> = mutableListOf()
) {
    var time = offsetTime(startTime.substring(11, 16))
    var actualStartTime: String? = time

    private val month = startTime.substring(5, 7).toInt().let { setMonth(it) }
    private val date = startTime.substring(8, 10)
    var actualDate: String? = date.let { month.let { m -> "$it $m" } }

    val ticketAmount: Int
        get() = ticketIDs.size

    init {
        if (gameWinner == null) gameWinner = ""
        if (clickWinner == null) clickWinner = ""
        Log.i("initEvent", "Event data: $blueGameWins, ")
    }

    private fun offsetTime(time: String): String {
        val offset = 0
        val hour = time.substring(0, 2).toInt() + offset
        val minute = time.substring(3, 5)

        //Log.d("check", "StartTime: $startTime, actualStartTime: $actualStartTime")
        return "$hour:$minute"
    }

    private fun setMonth(number: Int): String {
        var month = ""
        when (number) {
            1 -> {
                month = "Januari"
            }

            2 -> {
                month = "Februari"
            }

            3 -> {
                month = "Mars"
            }

            4 -> {
                month = "April"
            }

            5 -> {
                month = "Maj"
            }

            6 -> {
                month = "Juni"
            }

            7 -> {
                month = "Juli"
            }

            8 -> {
                month = "Augusti"
            }

            9 -> {
                month = "September"
            }

            10 -> {
                month = "Oktober"
            }

            11 -> {
                month = "November"
            }

            12 -> {
                month = "December"
            }
        }

        return month
    }
}