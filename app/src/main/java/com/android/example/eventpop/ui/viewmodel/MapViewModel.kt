package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.MapUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * **Controller** for the map tab: same event source as home (Room + refresh).
 */
class MapViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<MapUiState> = combine(
        eventRepository.observeEvents(),
        isRefreshing
    ) { pins, loading ->
        MapUiState(eventPins = pins, isLoading = loading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                eventRepository.refreshEvents()
            } finally {
                isRefreshing.value = false
            }
        }
    }
}
