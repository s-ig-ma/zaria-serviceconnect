package com.example.zariaserviceconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zariaserviceconnect.models.*
import com.example.zariaserviceconnect.repository.AppRepository
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

    // NEW: Search state
    // Holds the current search query text
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Holds search results — separate from regular providers list
    private val _searchResults = MutableStateFlow<UiState<List<ProviderModel>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<ProviderModel>>> = _searchResults

    // Tracks whether we are in search mode or browse mode
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    // Used to debounce search so we don't call API on every keystroke
    private var searchJob: Job? = null

    // ── Bookings ──────────────────────────────────────────────────────────────
    private val _bookings      = MutableStateFlow<UiState<List<BookingModel>>>(UiState.Idle)
    val bookings: StateFlow<UiState<List<BookingModel>>> = _bookings

    private val _bookingAction = MutableStateFlow<UiState<String>>(UiState.Idle)
    val bookingAction: StateFlow<UiState<String>> = _bookingAction

    // ── Reviews ───────────────────────────────────────────────────────────────
    private val _reviews      = MutableStateFlow<UiState<List<ReviewModel>>>(UiState.Idle)
    val reviews: StateFlow<UiState<List<ReviewModel>>> = _reviews

    private val _reviewAction = MutableStateFlow<UiState<String>>(UiState.Idle)
    val reviewAction: StateFlow<UiState<String>> = _reviewAction

    // ── Complaints ────────────────────────────────────────────────────────────
    private val _myComplaints    = MutableStateFlow<UiState<List<ComplaintModel>>>(UiState.Idle)
    val myComplaints: StateFlow<UiState<List<ComplaintModel>>> = _myComplaints

    private val _complaintSubmit = MutableStateFlow<UiState<String>>(UiState.Idle)
    val complaintSubmit: StateFlow<UiState<String>> = _complaintSubmit

    // ─────────────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            _userRole.value = repo.getRole()
            _userName.value = repo.getName()
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

    fun loadProviders(categoryId: Int? = null) {
        viewModelScope.launch {
            _providers.value = UiState.Loading
            val result = repo.getProviders(categoryId)
            _providers.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load providers") }
            )
        }
    }

    // NEW: Update search query with debounce
    // Waits 400ms after user stops typing before calling the API
    // This prevents calling the API on every single keystroke
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            // User cleared the search — go back to normal mode
            _isSearching.value      = false
            _searchResults.value    = UiState.Idle
            searchJob?.cancel()
            return
        }

        _isSearching.value = true

        // Cancel previous search if user is still typing
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Wait 400ms before searching — debounce
            delay(400)
            _searchResults.value = UiState.Loading
            val result = repo.searchProviders(query)
            _searchResults.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Search failed") }
            )
        }
    }

    // NEW: Clear search and go back to categories
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
                onSuccess = {
                    UiState.Success("Complaint submitted. Admin will review it.")
                },
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

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _userRole.value = null
            _userName.value = null
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
