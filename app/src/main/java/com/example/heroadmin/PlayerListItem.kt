package com.example.heroadmin

data class PlayerListItem (
    val firstName : String,
    val lastName : String,
    val age : Int,
    val bookerNames: List<String> = listOf(),
    val bookerPhones: List<String> = listOf(),
    val bookerEmails: List<String> = listOf(),
    val bookerAddresses: List<String> = listOf()
) {
    val bookerNamesShort = bookerNames[0]
    val bookerNamesLong = bookerNames.joinToString("\n")
    val bookerPhonesShort = bookerPhones[0]
    val bookerPhonesLong = bookerPhones.joinToString("\n")
    val bookerEmailsShort = bookerEmails[0]
    val bookerEmailsLong = bookerEmails.joinToString("\n")
    val bookerAddressesShort = bookerAddresses[0]
    val bookerAddressesLong = bookerAddresses.joinToString("\n")
    val hasMoreInfo = bookerNames.size > 1 || bookerPhones.size > 1 || bookerEmails.size > 1 || bookerAddresses.size > 1
}