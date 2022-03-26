package com.example.heroadmin;

public class Event {
    private String eventId;
    private String actualDate;
    private String startTime;
    private String endTime;
    private String actualStartTime;
    private String status;
    private String reportText;
    private String title;
    private String description;
    private Integer round;
    private Integer playerAmount;
    private Integer playerMax;
    private Integer ExpBlueTeamTotal;
    private Integer ExpRedTeamTotal;
    private Integer ExpAttendanceValue;
    private Integer ExpCostumeValue;
    private Integer ExpRecruitValue;

    public Event(String eventId, String actualDate, String startTime, String endTime, String actualStartTime, String status, String reportText, String title, String description, Integer round, Integer playerAmount, Integer playerMax, Integer expBlueTeamTotal, Integer expRedTeamTotal, Integer expAttendanceValue, Integer expCostumeValue, Integer expRecruitValue) {
        this.eventId = eventId;
        this.actualDate = actualDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.actualStartTime = actualStartTime;
        this.status = status;
        this.reportText = reportText;
        this.title = title;
        this.description = description;
        this.round = round;
        this.playerAmount = playerAmount;
        this.playerMax = playerMax;
        ExpBlueTeamTotal = expBlueTeamTotal;
        ExpRedTeamTotal = expRedTeamTotal;
        ExpAttendanceValue = expAttendanceValue;
        ExpCostumeValue = expCostumeValue;
        ExpRecruitValue = expRecruitValue;
    }

    public String getEventId() {
        return eventId;
    }

    public Integer getPlayerAmount() {
        return playerAmount;
    }

    public String getActualDate() {
        return actualDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getActualStartTime() {
        return actualStartTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Integer getPlayerMax() {
        return playerMax;
    }

    public void setPlayerMax(Integer playerMax) {
        this.playerMax = playerMax;
    }

    public Integer getExpBlueTeamTotal() {
        return ExpBlueTeamTotal;
    }

    public void setExpBlueTeamTotal(Integer expBlueTeamTotal) {
        ExpBlueTeamTotal = expBlueTeamTotal;
    }

    public Integer getExpRedTeamTotal() {
        return ExpRedTeamTotal;
    }

    public void setExpRedTeamTotal(Integer expRedTeamTotal) {
        ExpRedTeamTotal = expRedTeamTotal;
    }

    public Integer getExpAttendanceValue() {
        return ExpAttendanceValue;
    }

    public void setExpAttendanceValue(Integer expAttendanceValue) {
        ExpAttendanceValue = expAttendanceValue;
    }

    public Integer getExpCostumeValue() {
        return ExpCostumeValue;
    }

    public void setExpCostumeValue(Integer expCostumeValue) {
        ExpCostumeValue = expCostumeValue;
    }

    public Integer getExpRecruitValue() {
        return ExpRecruitValue;
    }

    public void setExpRecruitValue(Integer expRecruitValue) {
        ExpRecruitValue = expRecruitValue;
    }
}
