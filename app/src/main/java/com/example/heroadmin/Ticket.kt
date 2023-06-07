package com.example.heroadmin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlin.math.floor

@Serializable
data class Ticket(
    val ticketId: String = "",
    var firstName: String = "",
    var lastName: String = "",
    val age: Int = 0,
    val bookerName: String = "",
    var bookerPhone: String = "",
    val bookerAddress: String = "",
    val bookerPostort: String = "",
    val bookerEmail: String = "",
    var note: String = "",
    var teamColor: String = "",
    var checkedIn: Int = 0,
    var recruits: Int = 0,
    var expPersonal: Int = 0,
    var benched: Int = 0,
    var currentRole: Int = 0,
    var lastRole: Int = 0,
    var roundsMage: Int = 0,
    var roundsRogue: Int = 0,
    var roundsWarrior: Int = 0,
    var roundsHealer: Int = 0,
    var roundsKnight: Int = 0,
    var roundsSpecialRole: Int = 0,
    var guaranteedRole: Int = 0,
    @SerialName("playerID") var playerId: String? = null,
    val eventId: String = "",
    var suggestions: List<PlayerListItem>? = null,
    var group : String? = null
) {
    val fullName: String
        get() = "${firstName ?: ""} ${lastName ?: ""}"
    var selected = false
    var roundsSpecial = 0
    val powerLevel: Int
        get() = checkPowerLevel()
    var noteHandled = false
    var groupSize = 1
    var double = false

    private fun checkPowerLevel(): Int {
        var multiplier: Float = 1.0F
        age.let {
            if (it >= 13) {
                multiplier = 1.5F
            } else if (it <= 6) {
                multiplier = 0.75F
            }
        }
        return floor((age.toFloat() ?: 0.0F) * multiplier).toInt()
    }
}