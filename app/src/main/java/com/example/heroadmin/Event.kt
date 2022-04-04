package com.example.heroadmin

public class Event (

    public val eventId: String,
    public val venue: String,
    public val actualDate: String,
    private val startTime: String,
    private val endTime: String,
    public val actualStartTime: String,
    public val status: String,
    public val reportText: String,
    public var title: String,
    public val description: String,
    public val tickets: List<String>,
    public val round: Int,
    public val playerAmount: Int,
    public val playerMax: Int,
    public val ExpBlueTeamTotal: Int,
    public val ExpRedTeamTotal: Int,
    public val ExpAttendanceValue: Int,
    public val ExpCostumeValue: Int,
    public val ExpRecruitValue: Int,
        ) {
}