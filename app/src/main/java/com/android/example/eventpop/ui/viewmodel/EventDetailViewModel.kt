package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.data.repository.RatingRepository
import com.android.example.eventpop.ui.mvc.EventDetailUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * **Controller** for a single event: loads from [EventRepository] (cache + remote refresh).
 */
class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var pendingRatingRetryScore: Int? = null

    fun loadEvent(eventId: String) {
        observeJob?.cancel()
        if (eventId.isBlank()) {
            _uiState.value = EventDetailUiState()
            return
        }
        _uiState.update {
            it.copy(
                rsvpSuccess = false,
                event = null,
                myRating = null,
                hasRated = false,
                ratingSubmitError = null,
                isUserSignedIn = AuthRepository.isLoggedIn(),
                ratingCount = 0
            )
        }
        observeJob = viewModelScope.launch {
            eventRepository.observeEvent(eventId).collect { ev ->
                val uid = AuthRepository.currentUserId()
                val owner = uid != null && ev?.createdBy != null && ev.createdBy == uid
                val signedIn = AuthRepository.isLoggedIn()
                _uiState.update {
                    it.copy(
                        event = ev,
                        isOwner = owner,
                        isUserSignedIn = signedIn,
                        ratingCount = ev?.ratingCount ?: 0
                    )
                }
                if (ev != null && signedIn && !owner) {
                    reloadMyRating(ev.id)
                } else if (ev != null) {
                    _uiState.update { s ->
                        s.copy(myRating = null, hasRated = false)
                    }
                }
            }
        }
        viewModelScope.launch {
            eventRepository.refreshEvent(eventId)
        }
        viewModelScope.launch {
            val interested = eventRepository.isEventInterested(eventId)
            _uiState.update { it.copy(isInterested = interested) }
        }
    }

    private fun reloadMyRating(eventId: String) {
        viewModelScope.launch {
            ratingRepository.getMyRating(eventId).onSuccess { score ->
                _uiState.update {
                    it.copy(myRating = score, hasRated = score != null)
                }
            }
        }
    }

    fun toggleInterested() {
        val ev = _uiState.value.event ?: return
        viewModelScope.launch {
            val next = !_uiState.value.isInterested
            val ok = eventRepository.setEventInterested(ev.id, next)
            if (ok) {
                _uiState.update { it.copy(isInterested = next) }
            }
        }
    }

    fun submitRsvp() {
        val currentEvent = _uiState.value.event ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(rsvpLoading = true) }
            val success = eventRepository.rsvpToEvent(currentEvent.id)
            _uiState.update { it.copy(rsvpLoading = false, rsvpSuccess = success) }
            if (success) {
                eventRepository.refreshEvent(currentEvent.id)
            }
        }
    }

    fun consumeRsvpSuccess() {
        _uiState.update { it.copy(rsvpSuccess = false) }
    }

    fun onRatingSelected(score: Int) {
        if (score !in 1..5) return
        if (!AuthRepository.isLoggedIn()) return
        val eventId = _uiState.value.event?.id ?: return
        if (_uiState.value.isOwner) return
        pendingRatingRetryScore = score
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingRating = true,
                    ratingSubmitError = null,
                    myRating = score,
                    hasRated = true
                )
            }
            ratingRepository.upsertRating(eventId, score).fold(
                onSuccess = {
                    pendingRatingRetryScore = null
                    _uiState.update {
                        it.copy(isSubmittingRating = false)
                    }
                    refreshEventRatingAggregates(eventId)
                },
                onFailure = { e ->
                    viewModelScope.launch {
                        val restored = ratingRepository.getMyRating(eventId).getOrNull()
                        _uiState.update {
                            it.copy(
                                isSubmittingRating = false,
                                myRating = restored,
                                hasRated = restored != null,
                                ratingSubmitError = e.message
                                    ?: "Could not submit rating"
                            )
                        }
                    }
                }
            )
        }
    }

    fun onRemoveRating() {
        if (!AuthRepository.isLoggedIn()) return
        val eventId = _uiState.value.event?.id ?: return
        if (_uiState.value.isOwner) return
        pendingRatingRetryScore = null
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmittingRating = true, ratingSubmitError = null)
            }
            ratingRepository.deleteRating(eventId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            myRating = null,
                            hasRated = false,
                            isSubmittingRating = false
                        )
                    }
                    refreshEventRatingAggregates(eventId)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmittingRating = false,
                            ratingSubmitError = e.message
                                ?: "Could not remove rating"
                        )
                    }
                }
            )
        }
    }

    fun onRetryRatingSubmit() {
        pendingRatingRetryScore?.let { onRatingSelected(it) }
    }

    fun dismissRatingError() {
        _uiState.update { it.copy(ratingSubmitError = null) }
        pendingRatingRetryScore = null
    }

    private suspend fun refreshEventRatingAggregates(eventId: String) {
        eventRepository.refreshEvent(eventId)
    }

    suspend fun deleteCurrentEvent(): Boolean {
        val id = _uiState.value.event?.id ?: return false
        return eventRepository.deleteEvent(id).isSuccess
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
