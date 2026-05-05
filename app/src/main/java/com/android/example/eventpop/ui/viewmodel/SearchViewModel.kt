package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.SearchUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * **Controller** for the search screen: local cache when query empty, PostgREST search when typing.
 */
class SearchViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val remoteOverride = MutableStateFlow<List<Event>?>(null)
    private val isBusy = MutableStateFlow(false)

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        eventRepository.observeEvents(),
        remoteOverride,
        isBusy
    ) { q, cached, remote, busy ->
        SearchUiState(
            query = q,
            results = if (q.isBlank()) emptyList() else (remote ?: emptyList()),
            isLoading = busy
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    init {
        viewModelScope.launch {
            eventRepository.refreshEvents()
        }
        viewModelScope.launch {
            query.collectLatest { q ->
                isBusy.value = true
                try {
                    delay(220)
                    if (q.isBlank()) {
                        remoteOverride.value = null
                    } else {
                        remoteOverride.value = eventRepository.searchEventsRemote(q)
                    }
                } finally {
                    isBusy.value = false
                }
            }
        }
    }

    fun setQuery(value: String) {
        query.value = value
    }
}
