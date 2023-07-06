package com.example.heroadmin

import android.content.SharedPreferences
import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalDatabase<T, ID>(
    private val serializer: KSerializer<T>,
    private val getId: (T) -> ID,
    private val preferences: SharedPreferences,
    private val preferencesKey: String  // Add a preferencesKey parameter
) {
    private val database = mutableMapOf<ID, T>()

    fun initialize() {
        loadFromPreferences()
    }

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
            saveToPreferences()
        } else {
            //update(item)
            Log.i("check", "Item with ID $id already in database.")
        }
    }

    fun update(item: T) {
        val id = getId(item)
        if (id in database) {
            database[id] = item
            saveToPreferences()
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

    // Lasting data, across processes
    private fun loadFromPreferences() {
        val json = preferences.getString(preferencesKey, null) ?: return
        val listSerializer = ListSerializer(serializer)
        val items = Json.decodeFromString(listSerializer, json)
        database.clear()
        items.forEach { item ->
            val id = getId(item)
            database[id] = item
        }
    }

    private fun saveToPreferences() {
        val items = database.values.toList()
        val listSerializer = ListSerializer(serializer)
        val json = Json.encodeToString(listSerializer, items)
        preferences.edit().putString(preferencesKey, json).apply()  // Use preferencesKey here
    }

    fun clearDatabase() {
        database.clear()
        saveToPreferences()
    }

    fun clearCache() {
        clearDatabase()
        preferences.edit().clear().apply()
    }
}