package com.example.heroadmin
import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class PlayerListItem (
    val playerID: String,
    val firstName : String,
    val lastName : String,
    val age : Int = 20,
    val bookerNames: List<String>? = listOf(),
    val bookerPhones: List<String>? = listOf(),
    val bookerEmails: List<String>? = listOf(),
    val bookerAddresses: List<String>? = listOf()
) {
    val bookerNamesShort = bookerNames?.firstOrNull() ?: ""
    val bookerNamesLong = bookerNames?.joinToString("\n")
    val bookerPhonesShort = bookerPhones?.firstOrNull() ?: ""
    val bookerPhonesLong = bookerPhones?.joinToString("\n")
    val bookerEmailsShort = bookerEmails?.firstOrNull() ?: ""
    val bookerEmailsLong = bookerEmails?.joinToString("\n")
    val bookerAddressesShort = bookerAddresses?.firstOrNull() ?: ""
    val bookerAddressesLong = bookerAddresses?.joinToString("\n")
    val hasMoreInfo = bookerNames?.size!! > 1 || bookerPhones?.size!! > 1 || bookerEmails?.size!! > 1 || bookerAddresses?.size!! > 1
    var isExpanded: Boolean = false
    init {
        if (hasMoreInfo) {
            Log.i("playerListItem", "Has More Info")
        }
        else {
            Log.i("playerListItem", "Names: ${bookerNames?.size} Phones: ${bookerPhones?.size} Emails: ${bookerEmails?.size} Addresses: ${bookerAddresses?.size} ")
        }
    }
}