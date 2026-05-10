package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.HostedEventsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Lists events hosted by the signed-in user ([public.events.created_by]).
 */
class HostedEventsViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val hosted = MutableStateFlow<List<Event>>(emptyList())
    private val isBusy = MutableStateFlow(false)
    private val needsSignIn = MutableStateFlow(false)
    private val needsOrganizerRole = MutableStateFlow(false)

    val uiState: StateFlow<HostedEventsUiState> = combine(
        hosted,
        isBusy,
        needsSignIn,
        needsOrganizerRole
    ) { list, busy, signIn, needOrg ->
        HostedEventsUiState(
            events = list,
            isLoading = busy,
            needsSignIn = signIn,
            needsOrganizerRole = needOrg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HostedEventsUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (!AuthRepository.isLoggedIn()) {
                needsSignIn.value = true
                needsOrganizerRole.value = false
                hosted.value = emptyList()
                return@launch
            }
            needsSignIn.value = false
            if (!AuthRepository.isOrganizer()) {
                AuthRepository.refreshRole()
            }
            if (!AuthRepository.isOrganizer()) {
                needsOrganizerRole.value = true
                hosted.value = emptyList()
                return@launch
            }
            needsOrganizerRole.value = false
            isBusy.value = true
            try {
                val remote = eventRepository.fetchHostedEventsForCurrentUser()
                hosted.value = remote ?: emptyList()
            } finally {
                isBusy.value = false
            }
        }
    }

    fun deleteHostedEvent(eventId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = eventRepository.deleteEvent(eventId)
            result.fold(
                onSuccess = {
                    hosted.value = hosted.value.filter { it.id != eventId }
                    onResult(true)
                },
                onFailure = { onResult(false) }
            )
        }
    }
}
