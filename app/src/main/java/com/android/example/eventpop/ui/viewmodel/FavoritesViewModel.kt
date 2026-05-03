package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.FavoritesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * **Controller** for favorites: loads [public.event_interests] for the signed-in user via [EventRepository].
 */
class FavoritesViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val favorites = MutableStateFlow<List<Event>>(emptyList())
    private val isBusy = MutableStateFlow(false)
    private val needsSignIn = MutableStateFlow(false)

    val uiState: StateFlow<FavoritesUiState> = combine(
        favorites,
        isBusy,
        needsSignIn
    ) { fav, busy, signIn ->
        FavoritesUiState(
            favorites = fav,
            isLoading = busy,
            needsSignIn = signIn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (!AuthRepository.isLoggedIn()) {
                needsSignIn.value = true
                favorites.value = emptyList()
                return@launch
            }
            needsSignIn.value = false
            isBusy.value = true
            try {
                favorites.value = eventRepository.loadFavoriteEvents()
            } finally {
                isBusy.value = false
            }
        }
    }
}
