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

    /** Published events from the feed scheduled for today (Kampala) that have not ended yet. */
    val liveEvents: List<Event>
        get() {
            val now = ZonedDateTime.now(zone)
            return allEvents
                .filter { it.isHappeningToday(now, zone) }
                .sortedBy { e ->
                    e.parsedStart(zone)?.toInstant()?.epochSecond ?: Long.MAX_VALUE
                }
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
    val d = date?.trim()?.takeIf { it.isNotBlank() }
    return try {
        when {
            st.isNotBlank() && 'T' in st -> {
                runCatching {
                    Instant.parse(st).atZone(zone)
                }.recoverCatching {
                    ZonedDateTime.parse(st, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                }.recoverCatching {
                    LocalDateTime.parse(st, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(zone)
                }.getOrNull()
            }
            st.isNotBlank() && d != null -> {
                val ld = LocalDate.parse(d)
                val lt = parseLocalTimeFlexible(st)
                ld.atTime(lt).atZone(zone)
            }
            st.isBlank() && d != null -> LocalDate.parse(d).atStartOfDay(zone)
            else -> null
        }
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun Event.parsedEnd(zone: ZoneId): ZonedDateTime? {
    val et = endTime?.trim().orEmpty()
    if (et.isBlank()) return null
    val d = date?.trim()?.takeIf { it.isNotBlank() }
    val baseDate = runCatching {
        d?.let { LocalDate.parse(it) }
    }.getOrNull() ?: parsedStart(zone)?.toLocalDate() ?: return null
    return try {
        when {
            'T' in et -> {
                runCatching { Instant.parse(et).atZone(zone) }
                    .recoverCatching { ZonedDateTime.parse(et, DateTimeFormatter.ISO_ZONED_DATE_TIME) }
                    .recoverCatching {
                        LocalDateTime.parse(et, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(zone)
                    }.getOrNull()
            }
            else -> {
                val lt = parseLocalTimeFlexible(et)
                baseDate.atTime(lt).atZone(zone)
            }
        }
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun Event.isAllDayDateOnly(): Boolean {
    val hasDate = !date.isNullOrBlank()
    val noTimes = startTime.isNullOrBlank() && endTime.isNullOrBlank()
    return hasDate && noTimes
}

private fun parseLocalTimeFlexible(raw: String): LocalTime {
    val s = raw.trim()
    return try {
        LocalTime.parse(s)
    } catch (_: DateTimeParseException) {
        LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"))
    }
}

private fun Event.isHappeningToday(now: ZonedDateTime, zone: ZoneId): Boolean {
    val today = now.toLocalDate()

    if (isAllDayDateOnly()) {
        val ld = runCatching { LocalDate.parse(date!!.trim()) }.getOrNull() ?: return false
        return ld == today
    }

    val start = parsedStart(zone) ?: return false
    if (start.toLocalDate() != today) return false

    val end = parsedEnd(zone)
    if (end != null && now.isAfter(end)) return false

    return true
}
