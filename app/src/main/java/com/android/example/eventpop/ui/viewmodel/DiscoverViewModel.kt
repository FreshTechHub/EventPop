package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.DiscoverUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * **Controller** for Discover: cached feed, remote search, category/date filters.
 */
class DiscoverViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val remoteSearchOverride = MutableStateFlow<List<Event>?>(null)
    private val isBusy = MutableStateFlow(false)
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val selectedDateMillis = MutableStateFlow<Long?>(null)

    private data class MergeSnapshot(
        val merged: List<Event>,
        val query: String,
        val busy: Boolean
    )

    private val mergeSnapshot = combine(
        eventRepository.observeEvents(),
        searchQuery,
        remoteSearchOverride,
        isBusy
    ) { cached, query, remote, busy ->
        val merged = if (query.isBlank()) cached else (remote ?: cached)
        MergeSnapshot(merged = merged, query = query, busy = busy)
    }

    val uiState: StateFlow<DiscoverUiState> = combine(
        mergeSnapshot,
        selectedCategory,
        selectedDateMillis
    ) { snap, category, dateMillis ->
        val filtered = applyLocalFilters(snap.merged, category, dateMillis)
        DiscoverUiState(
            searchQuery = snap.query,
            events = filtered,
            isLoading = snap.busy,
            selectedCategory = category,
            selectedDateMillis = dateMillis
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscoverUiState()
    )

    init {
        viewModelScope.launch {
            isBusy.value = true
            try {
                eventRepository.refreshEvents()
            } finally {
                isBusy.value = false
            }
        }
        viewModelScope.launch {
            searchQuery.collectLatest { q ->
                isBusy.value = true
                try {
                    delay(240)
                    if (q.isBlank()) {
                        remoteSearchOverride.value = null
                    } else {
                        remoteSearchOverride.value = eventRepository.searchEventsRemote(q)
                    }
                } finally {
                    isBusy.value = false
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSelectedCategory(label: String?) {
        selectedCategory.value = label
    }

    fun setSelectedDateMillis(millis: Long?) {
        selectedDateMillis.value = millis
    }

    fun clearSearchAndFilters() {
        searchQuery.value = ""
        remoteSearchOverride.value = null
        selectedCategory.value = null
        selectedDateMillis.value = null
    }

    fun clearSearchAndRefresh() {
        clearSearchAndFilters()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isBusy.value = true
            try {
                eventRepository.refreshEvents()
            } finally {
                isBusy.value = false
            }
        }
    }

    private fun applyLocalFilters(
        events: List<Event>,
        categoryLabel: String?,
        dateMillis: Long?
    ): List<Event> {
        var list = events
        if (!categoryLabel.isNullOrBlank()) {
            list = list.filter { e ->
                e.category.displayName.equals(categoryLabel, ignoreCase = true) ||
                    e.category.name.equals(categoryLabel, ignoreCase = true)
            }
        }
        if (dateMillis != null) {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val key = fmt.format(Date(dateMillis))
            list = list.filter { e ->
                val d = e.date?.trim().orEmpty()
                d.startsWith(key, ignoreCase = true) ||
                    e.timeInfo.contains(key, ignoreCase = true)
            }
        }
        return list
    }
}
