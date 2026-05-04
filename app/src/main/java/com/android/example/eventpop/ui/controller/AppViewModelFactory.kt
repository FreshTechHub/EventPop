package com.android.example.eventpop.ui.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.example.eventpop.EventPopApp
import com.android.example.eventpop.ui.viewmodel.CreateEventViewModel
import com.android.example.eventpop.ui.viewmodel.DiscoverViewModel
import com.android.example.eventpop.ui.viewmodel.EventDetailViewModel
import com.android.example.eventpop.ui.viewmodel.FavoritesViewModel
import com.android.example.eventpop.ui.viewmodel.HomeViewModel
import com.android.example.eventpop.ui.viewmodel.MapViewModel
import com.android.example.eventpop.ui.viewmodel.ProfileViewModel
import com.android.example.eventpop.ui.viewmodel.SearchViewModel
/**
 * Supplies ViewModels (**Controllers**) with app-scoped **Model** dependencies ([EventPopApp.eventRepository]).
 */
class AppViewModelFactory(
    private val app: EventPopApp
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val repo = app.eventRepository
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repo) as T
            modelClass.isAssignableFrom(DiscoverViewModel::class.java) ->
                DiscoverViewModel(repo) as T
            modelClass.isAssignableFrom(EventDetailViewModel::class.java) ->
                EventDetailViewModel(repo) as T
            modelClass.isAssignableFrom(MapViewModel::class.java) ->
                MapViewModel(repo) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(repo) as T
            modelClass.isAssignableFrom(FavoritesViewModel::class.java) ->
                FavoritesViewModel(repo) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel() as T
            modelClass.isAssignableFrom(CreateEventViewModel::class.java) ->
                CreateEventViewModel(app, repo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
