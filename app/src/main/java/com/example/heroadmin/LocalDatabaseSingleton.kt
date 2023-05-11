package com.example.heroadmin

object LocalDatabaseSingleton {
    val ticketDatabase = LocalDatabase<Ticket, String>(Ticket.serializer()) { it.ticketId ?: "" }
    val playerDatabase = LocalDatabase<Player, String>(Player.serializer()) { it.playerId }
    val eventDatabase = LocalDatabase<Event, String>(Event.serializer()) { it.eventId }
}