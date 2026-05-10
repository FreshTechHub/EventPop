package com.android.example.eventpop.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.R
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.CreateEventSubmission
import com.android.example.eventpop.data.EventLocationData
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.data.GeoLatLon
import com.android.example.eventpop.data.NamedLookupRow
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
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

private const val LOG_TAG = "EventPopCreateEvent"

class CreateEventViewModel(
    application: Application,
    private val eventRepository: EventRepository,
    private val initialEditEventId: String? = null
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
            val categories = eventRepository.createEventCategoryOptions()
            val quota = eventRepository.fetchHostQuota()
            if (quota == null) {
                _uiState.update {
                    it.copy(
                        isLoadingMeta = false,
                        metaError = when {
                            !AuthRepository.isLoggedIn() ->
                                "Sign in to create events."
                            else ->
                                "Could not verify your hosting limit. Try again."
                        },
                        categories = categories,
                        subscribeGate = false,
                        hostRole = AuthRepository.cachedRole()
                    )
                }
                return@launch
            }

            val editId = initialEditEventId?.takeIf { it.isNotBlank() && it != "new" }
            if (editId != null) {
                val ev = eventRepository.fetchEventSnapshotRemote(editId)
                if (ev == null) {
                    _uiState.update {
                        it.copy(
                            isLoadingMeta = false,
                            metaError = "Could not load that event.",
                            categories = categories,
                            subscribeGate = false,
                            hostRole = quota.role
                        )
                    }
                    return@launch
                }
                val me = AuthRepository.currentUserId()
                if (me == null || ev.createdBy == null || ev.createdBy != me) {
                    _uiState.update {
                        it.copy(
                            isLoadingMeta = false,
                            metaError = "You can only edit your own events.",
                            categories = categories,
                            subscribeGate = false,
                            hostRole = quota.role
                        )
                    }
                    return@launch
                }
                val rawPath = eventRepository.fetchEventImagePathRemote(editId)
                val lat = ev.latitude ?: 0.3476
                val lon = ev.longitude ?: 32.5825
                val loc = EventLocationData(
                    latitude = lat,
                    longitude = lon,
                    displayAddress = ev.location,
                    placeName = ev.location.substringBefore(",").trim().ifEmpty { ev.title }
                )
                val priceTxt = when {
                    ev.isFree -> ""
                    ev.price != null -> {
                        val p = ev.price!!
                        if (p % 1.0 == 0.0) p.toLong().toString() else p.toString()
                    }
                    else -> ""
                }
                val matchedCategoryId = matchCategoryId(categories, ev.category.displayName)
                _uiState.update { prev ->
                    prev.copy(
                        isLoadingMeta = false,
                        metaError = null,
                        categories = categories,
                        hostedCount = quota.hostedEventCount,
                        subscriptionActive = quota.subscriptionActive,
                        hostRole = quota.role,
                        subscribeGate = false,
                        editingEventId = editId,
                        rsvpCountForEdit = ev.rsvpCount ?: 0,
                        title = ev.title,
                        description = ev.description.orEmpty(),
                        isFree = ev.isFree,
                        priceText = priceTxt,
                        dateText = ev.date.orEmpty(),
                        startTimeText = ev.startTime.orEmpty(),
                        endTimeText = ev.endTime.orEmpty(),
                        areaText = ev.area.orEmpty(),
                        selectedCategoryId = matchedCategoryId ?: categories.firstOrNull()?.id,
                        locationData = loc,
                        coverStoragePath = rawPath?.takeIf { !it.startsWith("http", ignoreCase = true) },
                        coverImageLabel = rawPath?.substringAfterLast('/'),
                        navigateToEventId = null
                    )
                }
                return@launch
            }

            val gate = !quota.canCreateEvent
            _uiState.update {
                it.copy(
                    isLoadingMeta = false,
                    metaError = if (categories.isEmpty()) {
                        "No categories available. Please try again later."
                    } else {
                        null
                    },
                    categories = categories,
                    hostedCount = quota.hostedEventCount,
                    subscriptionActive = quota.subscriptionActive,
                    hostRole = quota.role,
                    subscribeGate = gate,
                    selectedCategoryId = it.selectedCategoryId
                        ?.takeIf { id -> categories.any { c -> c.id == id } }
                        ?: categories.firstOrNull()?.id
                )
            }
        }
    }

    private fun matchCategoryId(options: List<NamedLookupRow>, displayName: String): String? {
        val target = displayName.trim()
        if (target.isEmpty()) return null
        return options.firstOrNull { it.name.equals(target, ignoreCase = true) }?.id
    }

    fun setTitle(value: String) = _uiState.update { it.copy(title = value, fieldErrors = it.fieldErrors.copy(title = null)) }
    fun setDescription(value: String) = _uiState.update { it.copy(description = value, fieldErrors = it.fieldErrors.copy(description = null)) }
    fun setIsFree(value: Boolean) = _uiState.update { it.copy(isFree = value, fieldErrors = it.fieldErrors.copy(price = null)) }
    fun setPriceText(value: String) = _uiState.update { it.copy(priceText = value, fieldErrors = it.fieldErrors.copy(price = null)) }
    fun setDateText(value: String) = _uiState.update { it.copy(dateText = value, fieldErrors = it.fieldErrors.copy(date = null)) }
    fun setStartTimeText(value: String) = _uiState.update { it.copy(startTimeText = value, fieldErrors = it.fieldErrors.copy(time = null)) }
    fun setEndTimeText(value: String) = _uiState.update { it.copy(endTimeText = value, fieldErrors = it.fieldErrors.copy(time = null)) }
    fun setAreaText(value: String) = _uiState.update { it.copy(areaText = value, fieldErrors = it.fieldErrors.copy(area = null)) }
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
        val categoryKey = state.selectedCategoryId

        val errors = CreateEventFieldErrors(
            title = if (title.length < 3) "Enter a title (at least 3 characters)." else null,
            location = if (state.locationData == null) "Please select a location for this event" else null,
            description = if (description.length < 10) "Add a short description (at least 10 characters)." else null,
            area = null,
            category = if (state.categories.isNotEmpty() && categoryKey.isNullOrBlank()) {
                "Choose a category."
            } else {
                null
            },
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

            val latestRole = AuthRepository.refreshRole()
            _uiState.update { it.copy(hostRole = latestRole) }
            Log.i(
                LOG_TAG,
                "publish start uid=${AuthRepository.currentUserId()} role=$latestRole " +
                    "subscribed=${state.subscriptionActive} hostedCount=${state.hostedCount} " +
                    "editing=${state.editingEventId != null} hasCover=${state.coverStoragePath != null}"
            )

            val areaResolved = eventRepository.resolveAreaIdForSubmission(state.areaText).getOrElse { e ->
                Log.e(LOG_TAG, "area resolve failed for '${state.areaText}'", e)
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        publishError = humanizePublishError(e, failingTable = "areas")
                    )
                }
                return@launch
            }

            val categoryResolved = state.categories.firstOrNull { it.id == categoryKey }?.id
            if (categoryResolved.isNullOrBlank() || !looksLikeUuid(categoryResolved)) {
                Log.e(
                    LOG_TAG,
                    "category guard rejected '$categoryResolved' (categories=${state.categories.size})"
                )
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        publishError = if (state.categories.isEmpty()) {
                            "No categories available yet. Try again later."
                        } else {
                            "Choose a category."
                        }
                    )
                }
                return@launch
            }
            Log.i(
                LOG_TAG,
                "publish resolved area=$areaResolved category=$categoryResolved title='${title.take(40)}'"
            )

            val imagePathOrUrl = state.coverStoragePath
                ?: state.editingEventId?.let { eventRepository.fetchEventImagePathRemote(it) }
                    ?.takeIf { it.isNotBlank() }

            val submission = CreateEventSubmission(
                title = title,
                location = loc.displayAddress,
                description = description,
                isFree = state.isFree,
                price = price,
                date = state.dateText.trim().takeIf { it.isNotEmpty() },
                startTime = normalizeTime(state.startTimeText.trim().takeIf { it.isNotEmpty() }),
                endTime = normalizeTime(state.endTimeText.trim().takeIf { it.isNotEmpty() }),
                areaId = areaResolved,
                categoryId = categoryResolved,
                latitude = loc.latitude,
                longitude = loc.longitude,
                imagePathOrUrl = imagePathOrUrl,
                rsvpCount = state.editingEventId?.let { state.rsvpCountForEdit }
            )

            val editId = state.editingEventId
            val result = if (editId != null) {
                eventRepository.updateEvent(editId, submission)
            } else {
                eventRepository.createEvent(submission)
            }
            result.onSuccess { Log.i(LOG_TAG, "publish ok id=${it.id}") }
            result.onFailure { Log.e(LOG_TAG, "publish remote call failed", it) }
            val quotaAfter = if (editId == null) eventRepository.fetchHostQuota() else null
            _uiState.update { s ->
                result.fold(
                    onSuccess = { event ->
                        s.copy(
                            isPublishing = false,
                            navigateToEventId = event.id,
                            hostedCount = quotaAfter?.hostedEventCount ?: s.hostedCount,
                            hostRole = quotaAfter?.role ?: s.hostRole,
                            subscribeGate = quotaAfter?.let { q -> !q.canCreateEvent } ?: s.subscribeGate
                        )
                    },
                    onFailure = { e ->
                        Log.e(LOG_TAG, "publish failed", e)
                        s.copy(
                            isPublishing = false,
                            publishError = humanizePublishError(e)
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

    private val uuidRegex =
        Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", RegexOption.IGNORE_CASE)

    private fun looksLikeUuid(value: String): Boolean = uuidRegex.matches(value)

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

    private fun humanizePublishError(
        error: Throwable?,
        failingTable: String? = null
    ): String {
        val app = getApplication<Application>()
        val combined = error.collectDiagnosticText()

        // Server / DB / auth must win over heuristics — PostgREST and Ktor often put the word
        // "timeout" or "I/O" in messages that are not device connectivity failures.
        val rlsOrPg =
            combined.contains("42501", ignoreCase = true) ||
                combined.contains("permission denied", ignoreCase = true) ||
                combined.contains("new row violates row-level security", ignoreCase = true) ||
                combined.contains("violates row-level security policy", ignoreCase = true) ||
                combined.contains("row-level security", ignoreCase = true) ||
                combined.contains("RLS", ignoreCase = true) ||
                combined.contains("PGRST", ignoreCase = true) ||
                // Supabase Kotlin SDK class names for 403 (RLS) and 4xx server replies.
                combined.contains("ForbiddenRestException", ignoreCase = true) ||
                combined.contains("403 ", ignoreCase = true)
        if (rlsOrPg) {
            // Categories come from the DB (public.categories). Surface a specific message
            // since users can only choose from the loaded list — they can't free-form type one.
            val tableHint = failingTable
                ?: when {
                    combined.contains("for table \"categories\"", ignoreCase = true) ||
                        combined.contains("on table \"categories\"", ignoreCase = true) ||
                        combined.contains("/rest/v1/categories", ignoreCase = true) -> "categories"
                    else -> null
                }
            if (tableHint == "categories") {
                return app.getString(R.string.create_event_error_new_category_blocked)
            }
            // Areas are free-form for organizers; an RLS denial here means either the caller
            // is not an organizer yet, or the lookup-insert migration is not applied. Route
            // through the standard role-aware copy in both cases.
            val st = _uiState.value
            return if (!st.hostRole.canCreateEvents) {
                app.getString(R.string.create_event_error_not_organizer_rls)
            } else {
                app.getString(R.string.create_event_error_permission_generic)
            }
        }

        if (combined.looksLikeUnauthorized()) {
            return app.getString(R.string.create_event_error_permission_generic)
        }

        if (combined.contains("23503", ignoreCase = true)) {
            return "Invalid area or category. Refresh and try again."
        }

        // Only classify as transport when we can prove it from a typed cause in the chain.
        // Substring sniffing on text like "504" or "Read timed out" produced false positives
        // for legitimate server replies, so we now rely on real exception types only.
        if (error.hasTransportRootCause()) {
            return app.getString(R.string.create_event_error_network)
        }

        // Fall back to the actual error text so we never lie about what went wrong.
        val firstLine = combined.lineSequence().firstOrNull { it.isNotBlank() }?.take(500)
        val single = error?.message?.trim()?.takeIf { it.isNotBlank() }?.take(500)
        return single ?: firstLine ?: "Could not publish. Try again."
    }

    /**
     * Full message text from this throwable and its causes. Includes [Throwable.toString] so
     * PostgREST/Ktor `RestException` payloads (status + description) are not lost.
     */
    private fun Throwable?.collectDiagnosticText(): String {
        if (this == null) return ""
        val out = StringBuilder()
        var t: Throwable? = this
        var depth = 0
        while (t != null && depth++ < 10) {
            val name = t.javaClass.simpleName ?: ""
            out.append(name).append(": ").append(t.message).append('\n')
            // toString() often carries extra fields (e.g. error JSON description) the bare
            // message does not.
            val str = runCatching { t.toString() }.getOrNull()
            if (!str.isNullOrBlank() && (t.message == null || !str.contains(t.message ?: ""))) {
                out.append(str).append('\n')
            }
            t = t.cause
        }
        return out.toString()
    }

    private fun Throwable?.hasTransportRootCause(): Boolean {
        var t: Throwable? = this
        var depth = 0
        while (t != null && depth++ < 12) {
            when (t) {
                is UnknownHostException,
                is SocketTimeoutException,
                is ConnectException,
                is SSLHandshakeException -> return true
                is IOException -> {
                    val cn = t.javaClass.name
                    if (cn.contains("Timeout", ignoreCase = true)) return true
                }
            }
            val simple = t.javaClass.simpleName ?: ""
            if (simple.contains("HttpRequestTimeout", ignoreCase = true)) return true
            if (simple.contains("ConnectTimeout", ignoreCase = true)) return true
            t = t.cause
        }
        return false
    }

    private fun String.looksLikeUnauthorized(): Boolean {
        if (isBlank()) return false
        return contains("UnauthorizedRestException", ignoreCase = true) ||
            contains("401 ", ignoreCase = true) ||
            contains(" 401", ignoreCase = true) ||
            contains("JWT", ignoreCase = true) ||
            contains("not authenticated", ignoreCase = true) ||
            contains("invalid_grant", ignoreCase = true) ||
            (contains("session", ignoreCase = true) && contains("expired", ignoreCase = true))
    }
}
