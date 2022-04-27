package com.example.heroadmin

class Event (
    val eventId: String,
    var title: String,
    val startTime: String,
    val endTime: String,
    var venue: String,
    var reportText: String,
    var description: String,
    var ExpBlueTeamTotal: Int,
    var ExpRedTeamTotal: Int,
    var ExpAttendanceValue: Int,
    var ExpRecruitValue: Int,
    var round: Int,
    var status: String,
    val tickets: MutableList<String>,
        ) {
    var time = offsetTime(startTime.substring(11, 16))
    var actualStartTime: String = time

    private val month = setMonth(startTime.substring(5, 7).toInt())
    private val date = startTime.substring(8, 10)
    var actualDate: String = "$date $month"

    var playerAmount = tickets.size
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