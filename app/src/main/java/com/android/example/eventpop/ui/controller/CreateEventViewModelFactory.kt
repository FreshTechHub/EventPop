package com.android.example.eventpop.ui.controller

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.example.eventpop.EventPopApp
import com.android.example.eventpop.ui.viewmodel.CreateEventViewModel

/**
 * Supplies [CreateEventViewModel] with optional [editEventId] (`null` or `"new"` = create flow).
 */
class CreateEventViewModelFactory(
    private val app: EventPopApp,
    private val editEventId: String?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass != CreateEventViewModel::class.java) {
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
        return CreateEventViewModel(application, app.eventRepository, editEventId) as T
    }
}
