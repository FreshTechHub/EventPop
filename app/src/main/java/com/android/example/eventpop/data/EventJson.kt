package com.android.example.eventpop.data

import kotlinx.serialization.json.Json

internal object EventJson {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun encode(event: Event): String = json.encodeToString(Event.serializer(), event)

    fun decode(payload: String): Event = json.decodeFromString(Event.serializer(), payload)
}
