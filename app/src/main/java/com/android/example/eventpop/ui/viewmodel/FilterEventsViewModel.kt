package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.android.example.eventpop.data.EventFilter
import com.android.example.eventpop.data.EventLocation
import com.android.example.eventpop.data.EventType
import com.android.example.eventpop.data.TimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterEventsViewModel : ViewModel() {

    private val _filter = MutableStateFlow(EventFilter())
    val filter: StateFlow<EventFilter> = _filter.asStateFlow()

    fun toggleEventType(type: EventType) {
        _filter.value = _filter.value.copy(
            selectedTypes = if (type in _filter.value.selectedTypes) {
                _filter.value.selectedTypes - type
            } else {
                _filter.value.selectedTypes + type
            }
        )
    }

    fun setLocation(location: EventLocation) {
        _filter.value = _filter.value.copy(selectedLocation = location)
    }

    fun setTimeRange(time: TimeRange) {
        _filter.value = _filter.value.copy(selectedTime = time)
    }

    fun resetFilters() {
        _filter.value = EventFilter()
    }

    fun applyFilters(): EventFilter = _filter.value
}
