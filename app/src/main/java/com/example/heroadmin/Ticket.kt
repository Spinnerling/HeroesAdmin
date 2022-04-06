package com.example.heroadmin

class Ticket(
    val ticketId: String,
    var playerId: String,
    var firstName: String,
    var lastName: String,
    val age: Int,
    var checkedIn: Boolean,
    var teamColor: String,
    var costume: Boolean,
    var recruits: Int,
    var benched: Boolean,
    var currentRole: Int,   // 0 = Undecided, 1 = Healer, 2 = Mage, 3 = Rogue, 4 = Knight, 5 = SpecialA, 6 = SpecialB, 7 = Warrior
    var lastRole: Int,      // 0 = Undecided, 1 = Healer, 2 = Mage, 3 = Rogue, 4 = Knight, 5 = SpecialA, 6 = SpecialB, 7 = Warrior
    val guardianFirstName: String,
    val guardianLastName: String,
    val guardianPhoneNr: String,
    val guardianEmail: String,
    val bookerFirstName: String,
    val bookerLastName: String,
    val bookerPhoneNr: String,
    val bookerEmail: String,
    var roundsHealer: Int,
    var roundsMage: Int,
    var roundsRogue: Int,
    var roundsKnight: Int,
    var roundsWarrior: Int,
    var roundsSpecial: Int,
    var respawns: Int,
    var guaranteedRole: Int,
    var expPersonal: Int,
    var tabardNr : Int,
    var note : String
) {
    var fullName = "$firstName $lastName"
    var guardianFullName = "$guardianFirstName $guardianLastName"
    var bookerFullName = "$bookerFirstName $bookerLastName"
    var selected = false
}