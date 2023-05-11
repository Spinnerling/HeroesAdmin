package com.example.heroadmin

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LocalDatabase<T, ID>(
    private val serializer: KSerializer<T>,
    private val getId: (T) -> ID
) {
    private val database = mutableMapOf<ID, T>()

    fun getById(id: ID): T? {
        return database[id]
    }

    fun getAll(): MutableList<T> {
        return database.values.toMutableList()
    }

    fun insert(item: T) {
        val id = getId(item)
        if (id !in database) {
            database[id] = item
        } else {
            //update(item)
            Log.i("check", "Item with ID $id already in database.")
        }
    }

    fun update(item: T) {
        val id = getId(item)
        if (id in database) {
            database[id] = item
        } else {
            insert(item)
            Log.i("check", "Item with ID $id does not exist in database. Inserting instead.")
        }
    }

    fun deleteById(id: ID): Boolean {
        return database.remove(id) != null
    }

    // Serialize and deserialize functions for JSON
    fun toJson(obj: T): String {
        return try {
            Json.encodeToString(serializer, obj)
        } catch (e: Exception) {
            throw RuntimeException("Error while encoding object to JSON", e)
        }
    }

    fun fromJson(json: String): T {
        return try {
            Json.decodeFromString(serializer, json)
        } catch (e: Exception) {
            throw RuntimeException("Error while decoding JSON to object", e)
        }
    }

    internal inline fun <reified P> getByPropertyValue(propertySelector: (T) -> P, value: P): T? {
        return database.values.firstOrNull { item ->
            propertySelector(item) == value
        }
    }
}