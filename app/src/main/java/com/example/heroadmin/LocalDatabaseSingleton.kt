package com.example.heroadmin

import android.content.Context
import android.content.SharedPreferences

object LocalDatabaseSingleton {
    lateinit var ticketDatabase: LocalDatabase<Ticket, String>
    lateinit var playerDatabase: LocalDatabase<Player, String>
    lateinit var eventDatabase: LocalDatabase<Event, String>

    fun initialize(preferences: SharedPreferences) {
        ticketDatabase = LocalDatabase<Ticket, String>(Ticket.serializer(), { it.ticketId ?: "" }, preferences, "ticketDatabase")
        playerDatabase = LocalDatabase<Player, String>(Player.serializer(), { it.playerId }, preferences, "playerDatabase")
        eventDatabase = LocalDatabase<Event, String>(Event.serializer(), { it.eventId }, preferences, "eventDatabase")

        // After initializing, load data from SharedPreferences
        ticketDatabase.initialize()
        playerDatabase.initialize()
        eventDatabase.initialize()
    }
}
