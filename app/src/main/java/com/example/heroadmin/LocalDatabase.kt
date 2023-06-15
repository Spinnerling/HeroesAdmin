package com.example.heroadmin

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LocalDatabase<T, ID>(
    private val serializer: KSerializer<T>,
    private val getId: (T) -> ID
) {
    private val database = mutableMapOf<ID, T>()

    fun getById(id: ID): T? {
        val item = database[id]
        if (item == null) {
            Log.i("LocalDatabase", "Item with ID $id not found.")
        }
        return item
    }

    fun getByIds(ids: Iterable<ID>): List<T?> {
        return ids.map { id ->
            val item = database[id]
            if (item == null) {
                Log.i("LocalDatabase", "Item with ID $id not found.")
            }
            item
        }
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

    fun updateList(items: Iterable<T>) {
        for (item in items) {
            update(item)
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

    fun getHighestId(): String? {
        return database.keys.maxOfOrNull { it.toString() }
    }

    fun logAllIds(tag : String) {
        Log.i(tag, "Logging all ids in local database...")
        for (id in database.keys) {
            Log.i(tag, "Item ID: $id")
        }
    }
}