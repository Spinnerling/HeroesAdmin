package com.example.heroadmin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LocalDatabase<T : Any>(private val serializer: KSerializer<T>) {
    private val storage = ConcurrentHashMap<Int, T>()
    private val nextId = AtomicInteger(1)

    fun getById(id: Int): T? {
        return storage[id]
    }

    fun getAll(): MutableList<T> {
        return storage.values.toMutableList()
    }

    fun insert(obj: T, id: Int? = null): Int {
        val newId = id ?: nextId.getAndIncrement()
        if (storage.putIfAbsent(newId, obj) != null) {
            throw IllegalStateException("Object with id $newId already exists.")
        }
        return newId
    }

    fun update(id: Int, obj: T): Boolean {
        return storage.replace(id, obj) != null
    }

    fun deleteById(id: Int): Boolean {
        return storage.remove(id) != null
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
}