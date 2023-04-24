package com.example.heroadmin

import kotlinx.serialization.Serializable

@Serializable
data class Event (
    val eventId: String,
    var title: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    var venueId: String? = null,
    var venue: String? = null,
    var reportText: String? = "",
    var description: String? = "",
    var winner: String? = "",
    var ExpBlueTeamTotal: Int? = null,
    var ExpRedTeamTotal: Int? = null,
    var ExpAttendanceValue: Int? = null,
    var ExpWinningValue: Int? = null,
    var ExpTeamChangeValue: Int? = null,
    var ExpRecruitValue: Int? = null,
    var round: Int? = 0,
    var redRound: Int? = 0,
    var blueRound: Int? = 0,
    var status: String? = null
) {
    var tickets: MutableList<String> = mutableListOf()
    var time = startTime?.substring(11, 16)?.let { offsetTime(it) }
    var actualStartTime: String? = time

    private val month = startTime?.substring(5, 7)?.toInt()?.let { setMonth(it) }
    private val date = startTime?.substring(8, 10)
    var actualDate: String? = date?.let { month?.let { m -> "$it $m" } }

    var playerAmount = tickets?.size ?: 0
    var playerMax = 99

    private fun offsetTime(time : String) : String{
        val offset = 2
        val hour = time.substring(0, 2).toInt() + offset
        val minute = time.substring(3, 5)

        return "$hour:$minute"
    }

    private fun setMonth(number : Int) : String {
        var month = ""
        when (number){
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