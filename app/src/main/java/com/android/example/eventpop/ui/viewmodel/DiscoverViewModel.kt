package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.DiscoverUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * **Controller** for discover/search: coordinates [EventRepository] and search query state.
 */
class DiscoverViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val remoteSearchOverride = MutableStateFlow<List<Event>?>(null)
    private val isBusy = MutableStateFlow(false)

    val uiState: StateFlow<DiscoverUiState> = combine(
        eventRepository.observeEvents(),
        searchQuery,
        remoteSearchOverride,
        isBusy
    ) { cached, query, remote, busy ->
        DiscoverUiState(
            searchQuery = query,
            events = if (query.isBlank()) cached else (remote ?: cached),
            isLoading = busy
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
}
