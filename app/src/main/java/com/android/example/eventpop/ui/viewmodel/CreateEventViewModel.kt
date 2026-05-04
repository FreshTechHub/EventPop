package com.android.example.eventpop.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.CreateEventSubmission
import com.android.example.eventpop.data.EventLocationData
import com.android.example.eventpop.data.GeoLatLon
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.data.NominatimClient
import com.android.example.eventpop.data.NominatimRateLimitException
import com.android.example.eventpop.data.NominatimResult
import com.android.example.eventpop.ui.mvc.CreateEventFieldErrors
import com.android.example.eventpop.ui.mvc.CreateEventUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateEventViewModel(
    application: Application,
    private val eventRepository: EventRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    private val _selectedLatLng = MutableStateFlow<GeoLatLon?>(null)
    private val _pickerCandidate = MutableStateFlow<EventLocationData?>(null)
    val pickerCandidate: StateFlow<EventLocationData?> = _pickerCandidate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchResults = MutableStateFlow<List<NominatimResult>>(emptyList())
    val searchResults: StateFlow<List<NominatimResult>> = _searchResults.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private var lastPickerPin: GeoLatLon? = null

    init {
        refreshMeta()
        viewModelScope.launch {
            _selectedLatLng
                .filterNotNull()
                .debounce(500)
                .collect { pin ->
                    reverseGeocodeForPicker(pin)
                }
        }
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.length < 2) {
                        _searchResults.value = emptyList()
                        return@collect
                    }
                    NominatimClient.search(q).fold(
                        onSuccess = { list -> _searchResults.value = list },
                        onFailure = { e ->
                            if (e is NominatimRateLimitException) {
                                _snackbarMessages.tryEmit("Too many requests, please wait")
                            }
                            _searchResults.value = emptyList()
                        }
                    )
                }
        }
    }

    fun refreshMeta() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMeta = true,
                    metaError = null,
                    publishError = null
                )
            }
            val (areas, categories) = eventRepository.fetchCreateEventLookups()
            val quota = eventRepository.fetchHostQuota()
            if (quota == null) {
                _uiState.update {
                    it.copy(
                        isLoadingMeta = false,
                        metaError = when {
                            !AuthRepository.isLoggedIn() ->
                                "Sign in to create events."
                            areas.isEmpty() && categories.isEmpty() ->
                                "Could not load areas and categories. Check your connection."
                            else ->
                                "Could not verify your hosting limit. Try again."
                        },
                        areas = areas,
                        categories = categories,
                        subscribeGate = false
                    )
                }
                return@launch
            }
            val gate = !quota.canCreateEvent
            _uiState.update {
                it.copy(
                    isLoadingMeta = false,
                    metaError = when {
                        areas.isEmpty() -> "No areas found in the project. Seed `public.areas` in Supabase."
                        categories.isEmpty() -> "No categories found. Seed `public.categories` in Supabase."
                        else -> null
                    },
                    areas = areas,
                    categories = categories,
                    hostedCount = quota.hostedEventCount,
                    subscriptionActive = quota.subscriptionActive,
                    subscribeGate = gate,
                    selectedAreaId = it.selectedAreaId ?: areas.firstOrNull()?.id,
                    selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id
                )
            }
        }
    }

    fun setTitle(value: String) = _uiState.update { it.copy(title = value, fieldErrors = it.fieldErrors.copy(title = null)) }
    fun setDescription(value: String) = _uiState.update { it.copy(description = value, fieldErrors = it.fieldErrors.copy(description = null)) }
    fun setIsFree(value: Boolean) = _uiState.update { it.copy(isFree = value, fieldErrors = it.fieldErrors.copy(price = null)) }
    fun setPriceText(value: String) = _uiState.update { it.copy(priceText = value, fieldErrors = it.fieldErrors.copy(price = null)) }
    fun setDateText(value: String) = _uiState.update { it.copy(dateText = value, fieldErrors = it.fieldErrors.copy(date = null)) }
    fun setStartTimeText(value: String) = _uiState.update { it.copy(startTimeText = value, fieldErrors = it.fieldErrors.copy(time = null)) }
    fun setEndTimeText(value: String) = _uiState.update { it.copy(endTimeText = value, fieldErrors = it.fieldErrors.copy(time = null)) }
    fun setSelectedAreaId(id: String?) = _uiState.update { it.copy(selectedAreaId = id, fieldErrors = it.fieldErrors.copy(area = null)) }
    fun setSelectedCategoryId(id: String?) = _uiState.update { it.copy(selectedCategoryId = id, fieldErrors = it.fieldErrors.copy(category = null)) }

    fun updateLocationSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onPickerPinMoved(lat: Double, lon: Double) {
        val pin = GeoLatLon(lat, lon)
        lastPickerPin = pin
        _uiState.update { it.copy(locationError = null) }
        _selectedLatLng.value = pin
    }

    fun onLocationPickerDismissed() {
        clearTransientPickerState()
    }

    private fun clearTransientPickerState() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _pickerCandidate.value = null
        _selectedLatLng.value = null
        lastPickerPin = null
        _uiState.update { it.copy(locationLoading = false, locationError = null) }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocationOrNull(): GeoLatLon? {
        val ctx = getApplication<Application>()
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = listOfNotNull(gps, net).maxByOrNull { it.time } ?: return null
            GeoLatLon(best.latitude, best.longitude)
        } catch (_: SecurityException) {
            null
        }
    }

    fun confirmPickerLocation() {
        val pin = lastPickerPin ?: return
        val data = _pickerCandidate.value ?: EventLocationData(
            latitude = pin.latitude,
            longitude = pin.longitude,
            displayAddress = "Lat: ${pin.latitude}, Lon: ${pin.longitude}",
            placeName = "Selected location"
        )
        _uiState.update {
            it.copy(
                locationData = data,
                locationError = null,
                fieldErrors = it.fieldErrors.copy(location = null)
            )
        }
        clearTransientPickerState()
    }

    private suspend fun reverseGeocodeForPicker(pin: GeoLatLon) {
        _uiState.update { it.copy(locationLoading = true, locationError = null) }
        NominatimClient.reverse(pin.latitude, pin.longitude).fold(
            onSuccess = { data ->
                _pickerCandidate.value = data
                _uiState.update { it.copy(locationLoading = false, locationError = null) }
            },
            onFailure = { e ->
                if (e is NominatimRateLimitException) {
                    _snackbarMessages.tryEmit("Too many requests, please wait")
                    _uiState.update { it.copy(locationLoading = false) }
                } else {
                    _pickerCandidate.value = EventLocationData(
                        latitude = pin.latitude,
                        longitude = pin.longitude,
                        displayAddress = "Lat: ${pin.latitude}, Lon: ${pin.longitude}",
                        placeName = "Selected location"
                    )
                    _uiState.update {
                        it.copy(
                            locationLoading = false,
                            locationError = "Could not find address. Try again."
                        )
                    }
                }
            }
        )
    }

    fun onCoverPicked(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isUploadingCover = true, publishError = null)
            }
            val result = eventRepository.uploadEventCover(getApplication(), uri)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { path ->
                        state.copy(
                            isUploadingCover = false,
                            coverStoragePath = path,
                            coverImageLabel = path.substringAfterLast('/')
                        )
                    },
                    onFailure = { e ->
                        state.copy(
                            isUploadingCover = false,
                            publishError = e.message ?: "Upload failed"
                        )
                    }
                )
            }
        }
    }

    fun clearCover() {
        _uiState.update {
            it.copy(coverStoragePath = null, coverImageLabel = null)
        }
    }

    fun publish() {
        val state = _uiState.value
        if (state.subscribeGate || state.isPublishing || state.isLoadingMeta) return

        val title = state.title.trim()
        val description = state.description.trim()
        val areaId = state.selectedAreaId
        val categoryId = state.selectedCategoryId

        val errors = CreateEventFieldErrors(
            title = if (title.length < 3) "Enter a title (at least 3 characters)." else null,
            location = if (state.locationData == null) "Please select a location for this event" else null,
            description = if (description.length < 10) "Add a short description (at least 10 characters)." else null,
            area = if (areaId.isNullOrBlank()) "Choose an area." else null,
            category = if (categoryId.isNullOrBlank()) "Choose a category." else null,
            date = if (state.dateText.isNotBlank() && !isoDateLooksOk(state.dateText)) {
                "Use ISO date YYYY-MM-DD or leave blank."
            } else {
                null
            },
            time = if (state.startTimeText.isNotBlank() && !timeLooksOk(state.startTimeText)) {
                "Start time like 18:00 or 6:00 PM."
            } else {
                null
            },
            price = if (!state.isFree) {
                val p = state.priceText.trim().toDoubleOrNull()
                if (p == null || p <= 0.0) "Enter a valid ticket price." else null
            } else {
                null
            }
        )
        val hasFieldErrors = listOfNotNull(
            errors.title,
            errors.location,
            errors.description,
            errors.area,
            errors.category,
            errors.date,
            errors.time,
            errors.price
        ).isNotEmpty()
        if (hasFieldErrors) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        val loc = state.locationData!!
        val price = if (!state.isFree) state.priceText.trim().toDoubleOrNull() else null

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, publishError = null) }
            val submission = CreateEventSubmission(
                title = title,
                location = loc.displayAddress,
                description = description,
                isFree = state.isFree,
                price = price,
                date = state.dateText.trim().takeIf { it.isNotEmpty() },
                startTime = normalizeTime(state.startTimeText.trim().takeIf { it.isNotEmpty() }),
                endTime = normalizeTime(state.endTimeText.trim().takeIf { it.isNotEmpty() }),
                areaId = areaId!!,
                categoryId = categoryId!!,
                latitude = loc.latitude,
                longitude = loc.longitude,
                imagePathOrUrl = state.coverStoragePath
            )
            val result = eventRepository.createEvent(submission)
            val quotaAfter = eventRepository.fetchHostQuota()
            _uiState.update { s ->
                result.fold(
                    onSuccess = { event ->
                        s.copy(
                            isPublishing = false,
                            navigateToEventId = event.id,
                            hostedCount = quotaAfter?.hostedEventCount ?: (s.hostedCount + 1),
                            subscribeGate = quotaAfter?.canCreateEvent == false
                        )
                    },
                    onFailure = { e ->
                        s.copy(
                            isPublishing = false,
                            publishError = humanizePublishError(e.message)
                        )
                    }
                )
            }
        }
    }

    fun consumeNavigateToEventId() {
        _uiState.update { it.copy(navigateToEventId = null) }
    }

    fun clearPublishError() {
        _uiState.update { it.copy(publishError = null) }
    }

    private fun isoDateLooksOk(s: String): Boolean =
        s.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))

    private fun timeLooksOk(s: String): Boolean {
        if (s.matches(Regex("\\d{1,2}:\\d{2}"))) return true
        if (s.matches(Regex("\\d{1,2}:\\d{2}\\s*(am|pm|AM|PM)"))) return true
        return false
    }

    /** Best-effort normalisation for Postgres time / text columns. */
    private fun normalizeTime(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim()
        val ampm = Regex("(\\d{1,2}):(\\d{2})\\s*(am|pm)", RegexOption.IGNORE_CASE).find(s)
        if (ampm != null) {
            var h = ampm.groupValues[1].toInt()
            val m = ampm.groupValues[2].toInt()
            val pm = ampm.groupValues[3].equals("pm", ignoreCase = true)
            if (pm) {
                if (h != 12) h += 12
            } else if (h == 12) {
                h = 0
            }
            return "%02d:%02d".format(h, m)
        }
        val m24 = Regex("(\\d{1,2}):(\\d{2})").matchEntire(s)
        if (m24 != null) {
            val h = m24.groupValues[1].toInt()
            val m = m24.groupValues[2].toInt()
            return "%02d:%02d".format(h, m)
        }
        return s
    }

    private fun humanizePublishError(raw: String?): String {
        val msg = raw.orEmpty()
        return when {
            msg.contains("42501", ignoreCase = true) ||
                msg.contains("permission denied", ignoreCase = true) ||
                msg.contains("new row violates row-level security", ignoreCase = true) ||
                msg.contains("RLS", ignoreCase = true) ->
                "You cannot publish more events on the free plan. Subscribe to continue hosting."
            msg.contains("23503", ignoreCase = true) ->
                "Invalid area or category. Refresh and try again."
            msg.isNotBlank() -> msg
            else -> "Could not publish. Try again."
        }
    }
}
