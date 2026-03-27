package com.example.zariaserviceconnect.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zariaserviceconnect.models.*
import com.example.zariaserviceconnect.repository.AppRepository
import com.example.zariaserviceconnect.utils.LocationHelper
import com.example.zariaserviceconnect.utils.UserLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)

    // ── Auth ──────────────────────────────────────────────────────────────────
    private val _loginState    = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState

    private val _registerState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val registerState: StateFlow<UiState<String>> = _registerState

    private val _userRole      = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _userName      = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    // ── Location (NEW) ────────────────────────────────────────────────────────
    // Stores the resident's current GPS location
    // null means location not available or permission denied
    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation

    // Whether we have location permission
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission

    // ── Categories ────────────────────────────────────────────────────────────
    private val _categories = MutableStateFlow<UiState<List<CategoryModel>>>(UiState.Idle)
    val categories: StateFlow<UiState<List<CategoryModel>>> = _categories

    // ── Providers ─────────────────────────────────────────────────────────────
    private val _providers = MutableStateFlow<UiState<List<ProviderModel>>>(UiState.Idle)
    val providers: StateFlow<UiState<List<ProviderModel>>> = _providers

    private val _selectedProvider = MutableStateFlow<UiState<ProviderModel>>(UiState.Idle)
    val selectedProvider: StateFlow<UiState<ProviderModel>> = _selectedProvider

    private val _myProviderProfile = MutableStateFlow<UiState<ProviderModel>>(UiState.Idle)
    val myProviderProfile: StateFlow<UiState<ProviderModel>> = _myProviderProfile

    // ── Search ────────────────────────────────────────────────────────────────
    private val _searchQuery   = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<UiState<List<ProviderModel>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<ProviderModel>>> = _searchResults

    private val _isSearching   = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private var searchJob: Job? = null

    // ── Bookings ──────────────────────────────────────────────────────────────
    private val _bookings      = MutableStateFlow<UiState<List<BookingModel>>>(UiState.Idle)
    val bookings: StateFlow<UiState<List<BookingModel>>> = _bookings

    private val _bookingAction = MutableStateFlow<UiState<String>>(UiState.Idle)
    val bookingAction: StateFlow<UiState<String>> = _bookingAction

    // ── Reviews ───────────────────────────────────────────────────────────────
    private val _reviews       = MutableStateFlow<UiState<List<ReviewModel>>>(UiState.Idle)
    val reviews: StateFlow<UiState<List<ReviewModel>>> = _reviews

    private val _reviewAction  = MutableStateFlow<UiState<String>>(UiState.Idle)
    val reviewAction: StateFlow<UiState<String>> = _reviewAction

    // ── Complaints ────────────────────────────────────────────────────────────
    private val _myComplaints    = MutableStateFlow<UiState<List<ComplaintModel>>>(UiState.Idle)
    val myComplaints: StateFlow<UiState<List<ComplaintModel>>> = _myComplaints

    private val _complaintSubmit = MutableStateFlow<UiState<String>>(UiState.Idle)
    val complaintSubmit: StateFlow<UiState<String>> = _complaintSubmit

    // ── Availability (NEW) ───────────────────────────────────────────────────
    private val _availabilityAction = MutableStateFlow<UiState<String>>(UiState.Idle)
    val availabilityAction: StateFlow<UiState<String>> = _availabilityAction

    // ─────────────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            _userRole.value = repo.getRole()
            _userName.value = repo.getName()
        }
    }

    // ── Location Actions (NEW) ────────────────────────────────────────────────

    /**
     * Called after the user grants location permission.
     * Gets their current GPS position and stores it.
     */
    fun fetchUserLocation(context: Context) {
        _hasLocationPermission.value = LocationHelper.hasPermission(context)
        if (!_hasLocationPermission.value) return

        LocationHelper.getCurrentLocation(context) { location ->
            _userLocation.value = location
            // Reload providers now that we have location
            // so distance sorting kicks in immediately
            if (location != null && _providers.value is UiState.Success<*>) {
                loadProviders()
            }
        }
    }

    /**
     * Called when permission result comes back.
     * Updates state and fetches location if granted.
     */
    fun onLocationPermissionResult(granted: Boolean, context: Context) {
        _hasLocationPermission.value = granted
        if (granted) {
            fetchUserLocation(context)
        }
    }

    /**
     * Provider calls this to save their GPS location to the backend.
     * Called automatically when provider opens the app and has permission.
     */
    fun updateProviderLocation(context: Context) {
        if (!LocationHelper.hasPermission(context)) return

        LocationHelper.getCurrentLocation(context) { location ->
            if (location != null) {
                viewModelScope.launch {
                    repo.updateMyLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    // ── Auth Actions ──────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = repo.login(email, password)
            _loginState.value = result.fold(
                onSuccess = {
                    _userRole.value = it.role
                    _userName.value = it.name
                    UiState.Success(it)
                },
                onFailure = { UiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun registerResident(
        name: String, email: String, phone: String,
        password: String, location: String
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            val result = repo.registerResident(name, email, phone, password, location)
            _registerState.value = result.fold(
                onSuccess = { UiState.Success(it.message) },
                onFailure = { UiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun registerProvider(
        name              : String,
        email             : String,
        phone             : String,
        password          : String,
        location          : String,
        categoryId        : Int,
        yearsOfExperience : Int,
        description       : String
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            // Provider registration requires a file upload for ID document
            // For now we create a dummy empty file since the form doesnt collect it
            val dummyFile = java.io.File.createTempFile("id_doc", ".txt")
            dummyFile.writeText("ID Document")
            val result = repo.registerProvider(
                name                = name,
                email               = email,
                phone               = phone,
                password            = password,
                categoryId          = categoryId,
                yearsOfExperience   = yearsOfExperience,
                description         = description,
                location            = location,
                idDocumentFile      = dummyFile
            )
            _registerState.value = result.fold(
                onSuccess = { UiState.Success(it.message) },
                onFailure = { UiState.Error(it.message ?: "Registration failed") }
            )
            dummyFile.delete()
        }
    }

    // ── Category Actions ──────────────────────────────────────────────────────

    fun loadCategories() {
        viewModelScope.launch {
            _categories.value = UiState.Loading
            val result = repo.getCategories()
            _categories.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load categories") }
            )
        }
    }

    // ── Provider Actions ──────────────────────────────────────────────────────

    // Now passes user GPS automatically if available
    fun loadProviders(categoryId: Int? = null) {
        viewModelScope.launch {
            _providers.value = UiState.Loading
            val loc = _userLocation.value
            val result = repo.getProviders(
                categoryId = categoryId,
                userLat    = loc?.latitude,
                userLon    = loc?.longitude
            )
            _providers.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load providers") }
            )
        }
    }

    // Search also passes user GPS for distance sorting
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _isSearching.value   = false
            _searchResults.value = UiState.Idle
            searchJob?.cancel()
            return
        }
        _isSearching.value = true
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce
            _searchResults.value = UiState.Loading
            val loc = _userLocation.value
            val result = repo.searchProviders(
                query   = query,
                userLat = loc?.latitude,
                userLon = loc?.longitude
            )
            _searchResults.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Search failed") }
            )
        }
    }

    fun clearSearch() {
        _searchQuery.value   = ""
        _isSearching.value   = false
        _searchResults.value = UiState.Idle
        searchJob?.cancel()
    }

    fun loadProviderById(id: Int) {
        viewModelScope.launch {
            _selectedProvider.value = UiState.Loading
            val result = repo.getProviderById(id)
            _selectedProvider.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Provider not found") }
            )
        }
    }

    fun loadMyProviderProfile() {
        viewModelScope.launch {
            _myProviderProfile.value = UiState.Loading
            val result = repo.getMyProviderProfile()
            _myProviderProfile.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Could not load profile") }
            )
        }
    }

    // ── Booking Actions ───────────────────────────────────────────────────────

    fun loadResidentBookings() {
        viewModelScope.launch {
            _bookings.value = UiState.Loading
            val result = repo.getResidentBookings()
            _bookings.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load bookings") }
            )
        }
    }

    fun loadProviderBookings() {
        viewModelScope.launch {
            _bookings.value = UiState.Loading
            val result = repo.getProviderBookings()
            _bookings.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load bookings") }
            )
        }
    }

    fun createBooking(
        providerId: Int, description: String,
        date: String, time: String, notes: String?
    ) {
        viewModelScope.launch {
            _bookingAction.value = UiState.Loading
            val result = repo.createBooking(providerId, description, date, time, notes)
            _bookingAction.value = result.fold(
                onSuccess = { UiState.Success("Booking request sent successfully!") },
                onFailure = { UiState.Error(it.message ?: "Booking failed") }
            )
        }
    }

    fun updateBookingStatus(id: Int, status: String, notes: String? = null) {
        viewModelScope.launch {
            _bookingAction.value = UiState.Loading
            val result = repo.updateBookingStatus(id, status, notes)
            _bookingAction.value = result.fold(
                onSuccess = { UiState.Success("Status updated to $status") },
                onFailure = { UiState.Error(it.message ?: "Update failed") }
            )
        }
    }

    // ── Review Actions ────────────────────────────────────────────────────────

    fun loadProviderReviews(providerId: Int) {
        viewModelScope.launch {
            _reviews.value = UiState.Loading
            val result = repo.getProviderReviews(providerId)
            _reviews.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load reviews") }
            )
        }
    }

    fun submitReview(bookingId: Int, rating: Int, comment: String?) {
        viewModelScope.launch {
            _reviewAction.value = UiState.Loading
            val result = repo.submitReview(bookingId, rating, comment)
            _reviewAction.value = result.fold(
                onSuccess = { UiState.Success("Review submitted!") },
                onFailure = { UiState.Error(it.message ?: "Review failed") }
            )
        }
    }

    // ── Complaint Actions ─────────────────────────────────────────────────────

    fun submitComplaint(bookingId: Int, message: String) {
        viewModelScope.launch {
            _complaintSubmit.value = UiState.Loading
            val result = repo.submitComplaint(bookingId, message)
            _complaintSubmit.value = result.fold(
                onSuccess = { UiState.Success("Complaint submitted. Admin will review it.") },
                onFailure = { UiState.Error(it.message ?: "Failed to submit complaint.") }
            )
        }
    }

    fun loadMyComplaints() {
        viewModelScope.launch {
            _myComplaints.value = UiState.Loading
            val result = repo.getMyComplaints()
            _myComplaints.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load complaints.") }
            )
        }
    }

    // ── Availability Actions (NEW) ───────────────────────────────────────────

    fun setAvailability(status: String) {
        viewModelScope.launch {
            _availabilityAction.value = UiState.Loading
            val result = repo.setAvailability(status)
            _availabilityAction.value = result.fold(
                onSuccess = { UiState.Success(it.message) },
                onFailure = { UiState.Error(it.message ?: "Failed to update availability.") }
            )
            // Reload profile so the badge updates immediately
            loadMyProviderProfile()
        }
    }

    fun resetAvailabilityAction() { _availabilityAction.value = UiState.Idle }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _userRole.value   = null
            _userName.value   = null
            _userLocation.value = null
            _loginState.value = UiState.Idle
            clearSearch()
        }
    }

    // ── Resets ────────────────────────────────────────────────────────────────

    fun resetBookingAction()   { _bookingAction.value   = UiState.Idle }
    fun resetReviewAction()    { _reviewAction.value    = UiState.Idle }
    fun resetComplaintSubmit() { _complaintSubmit.value = UiState.Idle }
    fun resetRegisterState()   { _registerState.value   = UiState.Idle }
}
