package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LandingUiState(
    val isLoading: Boolean = true,
    val allEvents: List<Event> = emptyList()
) {
    private val zone: ZoneId get() = ZoneId.of("Africa/Kampala")

    val liveEvents: List<Event>
        get() {
            val now = ZonedDateTime.now(zone)
            return allEvents.filter { it.isLiveNow(now, zone) }
        }

    val featuredEvents: List<Event>
        get() = allEvents
            .sortedByDescending { it.rsvpCount ?: 0 }
            .take(4)
}

class LandingViewModel(
    private val repository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandingUiState())
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val remote = repository.fetchPublicEventsSnapshot()
            _uiState.update {
                LandingUiState(
                    isLoading = false,
                    allEvents = remote.orEmpty()
                )
            }
        }
    }
}

private fun Event.parsedStart(zone: ZoneId): ZonedDateTime? {
    val st = startTime?.trim().orEmpty()
    if (st.isBlank()) return null
    return try {
        when {
            'T' in st -> {
                runCatching {
                    Instant.parse(st).atZone(zone)
                }.recoverCatching {
                    ZonedDateTime.parse(st, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                }.recoverCatching {
                    LocalDateTime.parse(st, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(zone)
                }.getOrNull()
            }
            !date.isNullOrBlank() -> {
                val ld = LocalDate.parse(date.trim())
                val lt = parseLocalTimeFlexible(st)
                ld.atTime(lt).atZone(zone)
            }
            else -> null
        }
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun parseLocalTimeFlexible(raw: String): LocalTime {
    val s = raw.trim()
    return try {
        LocalTime.parse(s)
    } catch (_: DateTimeParseException) {
        LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"))
    }
}

private fun Event.isLiveNow(now: ZonedDateTime, zone: ZoneId): Boolean {
    val start = parsedStart(zone) ?: return false
    if (start.toLocalDate() != now.toLocalDate()) return false
    val windowEnd = now.plusHours(3)
    return !start.isBefore(now) && !start.isAfter(windowEnd)
}
