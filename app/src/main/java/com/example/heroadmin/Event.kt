package com.example.heroadmin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    @SerialName("ID") var eventId: String,
    @SerialName("Event_Title") var title: String,
    @SerialName("Event_Start_date") var startTime: String,
    @SerialName("Event_End_Date") var endTime: String,
    @SerialName("Venue_ID") var venue: String,
    var reportText: String? = "",
    var description: String? = "",
    var winner: String? = "",
    var ExpAttendanceValue: Int? = null,
    var ExpWinningValue: Int? = null,
    var ExpTeamChangeValue: Int? = null,
    var ExpRecruitValue: Int? = null,
    @SerialName("Round") var round: Int? = 0,
    @SerialName("Status") var status: String? = "Ej påbörjat"
) {
    @SerialName("TicketIDs")
    var ticketIDs: MutableList<String> = mutableListOf()
    var time = offsetTime(startTime.substring(11, 16))
    var actualStartTime: String? = time

    private val month = startTime.substring(5, 7).toInt().let { setMonth(it) }
    private val date = startTime.substring(8, 10)
    var actualDate: String? = date.let { month.let { m -> "$it $m" } }

    val ticketAmount: Int
        get() = ticketIDs.size
    var playerMax = 99

    private fun offsetTime(time: String): String {
        val offset = 2
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