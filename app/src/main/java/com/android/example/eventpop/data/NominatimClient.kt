package com.android.example.eventpop.data

import android.os.SystemClock
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val USER_AGENT = "EventPop/1.0"
private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class ReverseDto(
    @SerialName("display_name") val displayName: String? = null,
    val name: String? = null,
    val address: AddressDto? = null
)

@Serializable
private data class AddressDto(
    val road: String? = null,
    val suburb: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null
)

@Serializable
private data class SearchItemDto(
    val lat: String,
    val lon: String,
    @SerialName("display_name") val displayName: String,
    val name: String? = null
)

class NominatimRateLimitException : Exception("Rate limited")

object NominatimClient {

    @Volatile
    private var lastRequestElapsed: Long = 0L

    private fun throttleInterRequest() {
        synchronized(this) {
            val now = SystemClock.elapsedRealtime()
            val wait = 1000L - (now - lastRequestElapsed)
            if (wait > 0) {
                Thread.sleep(wait)
            }
            lastRequestElapsed = SystemClock.elapsedRealtime()
        }
    }

    suspend fun reverse(lat: Double, lon: Double): Result<EventLocationData> =
        withContext(Dispatchers.IO) {
            runCatching {
                throttleInterRequest()
                val url =
                    "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"
                val body = httpGet(url) ?: throw IllegalStateException("Empty body")
                val dto = json.decodeFromString(ReverseDto.serializer(), body)
                val display = dto.displayName?.takeIf { it.isNotBlank() }
                    ?: "Lat: $lat, Lon: $lon"
                val place = buildPlaceName(dto)
                EventLocationData(
                    latitude = lat,
                    longitude = lon,
                    displayAddress = display,
                    placeName = place
                )
            }
        }

    suspend fun search(query: String): Result<List<NominatimResult>> =
        withContext(Dispatchers.IO) {
            val q = query.trim()
            if (q.length < 2) return@withContext Result.success(emptyList())
            runCatching {
                throttleInterRequest()
                val enc = URLEncoder.encode(q, StandardCharsets.UTF_8.name())
                val url =
                    "https://nominatim.openstreetmap.org/search?q=$enc&format=json&limit=5&countrycodes=ug"
                val body = httpGet(url).orEmpty()
                if (body.isEmpty()) return@runCatching emptyList()
                val list = json.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(SearchItemDto.serializer()),
                    body
                )
                list.mapNotNull { item ->
                    val la = item.lat.toDoubleOrNull() ?: return@mapNotNull null
                    val lo = item.lon.toDoubleOrNull() ?: return@mapNotNull null
                    NominatimResult(
                        lat = la,
                        lon = lo,
                        name = item.name?.takeIf { it.isNotBlank() }
                            ?: item.displayName.substringBefore(',').trim(),
                        displayName = item.displayName
                    )
                }
            }
        }

    private fun buildPlaceName(dto: ReverseDto): String {
        dto.name?.takeIf { it.isNotBlank() }?.let { return it.trim() }
        val a = dto.address ?: return "Selected location"
        listOf(a.road, a.suburb, a.city, a.town, a.village)
            .firstOrNull { !it.isNullOrBlank() }
            ?.let { return it.trim() }
        return "Selected location"
    }

    private fun httpGet(urlString: String): String? {
        val conn = (java.net.URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", USER_AGENT)
            connectTimeout = 20_000
            readTimeout = 20_000
        }
        return try {
            when (conn.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    conn.inputStream.use { ins ->
                        ins.readBytes().toString(StandardCharsets.UTF_8)
                    }
                }
                429 -> throw NominatimRateLimitException()
                else -> throw IllegalStateException("HTTP ${conn.responseCode}")
            }
        } finally {
            conn.disconnect()
        }
    }
}
