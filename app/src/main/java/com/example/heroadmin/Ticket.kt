package com.example.heroadmin

import kotlin.math.floor

class Ticket(
    val ticketId: String,
    var firstName: String,
    var lastName: String,
    val age: Int,
    val guardianName: String,
    val guardianPhoneNr: String,
    val bookerEmail: String,
    val bookerFullName: String,
    var teamColor: String,
    var tabardNr : Int,
    var note : String,
    var checkedIn: Int,
    var recruits: Int,
    var expPersonal: Int,
    var benched: Int,
    var currentRole: Int,   // 0 = Undecided, 1 = Healer, 2 = Rogue, 3 = Mage, 4 = Knight, 5 = SpecialA, 6 = SpecialB, 7 = Warrior
    var roundsMage: Int,
    var roundsRogue: Int,
    var roundsWarrior: Int,
    var roundsHealer: Int,
    var roundsKnight: Int,
    var hasRespawn: Int,
    var guaranteedRole: Int, // 0 = Undecided, 1 = Healer, 2 = Rogue, 3 = Mage, 4 = Knight, 5 = SpecialA, 6 = SpecialB, 7 = Warrior
    var playerId: String,
) {
    var fullName = "$firstName $lastName"
    var selected = false
    var roundsSpecialRole = 0
    var allowedTimesPerRole = 1 // How many times they're allowed to be Healer, Knight, etc as well as Special
    var roundsSpecial = 0
    var powerLevel : Int = checkPowerLevel()
    var noteHandled = false
    var group = ""
    var groupSize = 1

    private fun checkPowerLevel(): Int {
        var multiplier : Float = 1.0F
        if (age >= 13) {
            multiplier = 1.5F
        }
        else if (age <= 6){
            multiplier = 0.75F
        }
        return floor((age * multiplier).toDouble()).toInt()
    }
}