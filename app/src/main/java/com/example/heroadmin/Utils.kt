package com.example.heroadmin

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.heroadmin.LocalDatabaseSingleton.playerDatabase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// THIS IS A CLASS WITH UTILITY FUNCTIONS, USEFUL IN ALL FRAGMENTS

fun logLargeString(tag: String, content: String) {
    val maxLogSize = 1000
    for (i in 0..content.length / maxLogSize) {
        val start = i * maxLogSize
        var end = (i + 1) * maxLogSize
        end = if (end > content.length) content.length else end
        Log.i(tag, content.substring(start, end))
    }
}


private fun ResetTicket(ticket: Ticket) {
    val player = ticket.playerId?.let { playerDatabase.getById(it) }
    if (player != null) {
        player.healerLevel = 0
        player.healerUltimateA = false
        player.healerUltimateB = false
        player.rogueLevel = 0
        player.rogueUltimateA = false
        player.rogueUltimateB = false
        player.mageLevel = 0
        player.mageUltimateA = false
        player.mageUltimateB = false
        player.knightLevel = 0
        player.knightUltimateA = false
        player.knightUltimateB = false
        player.warriorHealer = false
        player.warriorRogue = false
        player.warriorMage = false
        player.warriorKnight = false
    }
    ticket.checkedIn = 0
    ticket.teamColor = null
}



fun callNotification(message: String, context : Context) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.note_popup, null)

    val builder = AlertDialog.Builder(context).setView(dialogView)

    val notification = builder.show()

    val textHolder: TextView = dialogView.findViewById(R.id.notePopupText)
    textHolder.text = message
    val noteAButton = dialogView.findViewById<Button>(R.id.noteAButton)
    val noteBButton = dialogView.findViewById<Button>(R.id.noteBButton)
    noteBButton.visibility = View.GONE

    noteAButton.setOnClickListener {
        notification.dismiss()
    }
}


fun callChoice(
    message: String,
    buttonAText: String,
    buttonBText: String,
    aFunction: () -> Unit,
    bFunction: () -> Unit,
    context : Context
) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.note_popup, null)

    val builder = AlertDialog.Builder(context).setView(dialogView)

    val notification = builder.show()

    val textHolder: TextView = dialogView.findViewById(R.id.notePopupText)
    val noteAButton = dialogView.findViewById<Button>(R.id.noteAButton)
    val noteBButton = dialogView.findViewById<Button>(R.id.noteBButton)

    textHolder.text = message
    noteAButton.text = buttonAText
    noteBButton.text = buttonBText
    noteBButton.visibility = View.VISIBLE

    noteAButton.setOnClickListener {
        aFunction()
        notification.dismiss()
    }

    noteBButton.setOnClickListener {
        bFunction()
        notification.dismiss()
    }
}


fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        //for other devices who are able to connect with Ethernet
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        //for check internet over Bluetooth
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}

fun getRoleByNumber(number: Int): String {
    var role = ""

    when (number) {
        0 -> {
            role = "Undecided"
        }
        1 -> {
            role = "Helare"
        }
        2 -> {
            role = "Odåga"

        }
        3 -> {
            role = "Magiker"
        }
        4 -> {
            role = "Riddare"
        }
        5 -> {
            role = "Special A"
        }
        6 -> {
            role = "Special B"
        }
        7 -> {
            role = "Krigare"
        }
    }

    return role
}

inline fun <reified T> createJsonString(data: T): String {
    val json = Json { encodeDefaults = true }
    return json.encodeToString(data)
}
