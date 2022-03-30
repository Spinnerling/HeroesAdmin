package com.example.heroadmin

class Event(
    val eventId : String,
    var startTime : String,
    var endTime : String,
    var description : String,
    var title : String,
    var reportText : String,
    var playerMax : Int,
    var tickets : List<String>, // list of ticketIds
    var EXPBlueTeamTotal : Int,
    var EXPRedTeamTotal : Int,
    var EXPAttendanceValue : Int,
    var EXPCostumeValue : Int,
    var EXPRecruitValue : Int,
) {
}