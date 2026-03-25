package com.example.zariaserviceconnect.repository

import android.content.Context
import com.example.zariaserviceconnect.models.*
import com.example.zariaserviceconnect.network.RetrofitClient
import com.example.zariaserviceconnect.utils.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AppRepository(private val context: Context) {

    private val api = RetrofitClient.create(context)

    private fun errorMessage(code: Int, body: String?): String {
        return try {
            if (body == null) return "Error $code. Please try again."
            if (body.contains("\"detail\":\""))
                return body.substringAfter("\"detail\":\"").substringBefore("\"")
            if (body.contains("\"msg\":\""))
                return body.substringAfter("\"msg\":\"").substringBefore("\"")
            "Error $code. Please try again."
        } catch (e: Exception) {
            "Error $code. Please try again."
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): Result<LoginResponse> = try {
        val response = api.login(LoginRequest(email, password))
        if (response.isSuccessful && response.body() != null) {
            val data = response.body()!!
            TokenManager.saveLoginData(
                context, data.accessToken, data.role, data.userId, data.name)
            Result.success(data)
        } else {
            Result.failure(Exception(
                errorMessage(response.code(), response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        Result.failure(Exception(
            "Cannot connect to server. Check your IP address."))
    }

    suspend fun registerResident(
        name: String, email: String, phone: String,
        password: String, location: String
    ): Result<MessageResponse> = try {
        val response = api.registerResident(
            RegisterResidentRequest(name, email, phone, password, location))
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception(
            errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun registerProvider(
        name: String, email: String, phone: String, password: String,
        categoryId: Int, yearsOfExperience: Int, description: String,
        location: String, idDocumentFile: File
    ): Result<MessageResponse> = try {
        fun String.toRB() = toRequestBody("text/plain".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            "id_document", idDocumentFile.name,
            idDocumentFile.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
        val response = api.registerProvider(
            name.toRB(), email.toRB(), phone.toRB(), password.toRB(),
            categoryId.toString().toRB(), yearsOfExperience.toString().toRB(),
            description.toRB(), location.toRB(), filePart)
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception(
            errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // ── Categories ────────────────────────────────────────────────────────────

    suspend fun getCategories(): Result<List<CategoryModel>> = try {
        val response = api.getCategories()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load categories."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // ── Providers ─────────────────────────────────────────────────────────────

    // Updated: now passes user GPS to backend for location-based sorting
    suspend fun getProviders(
        categoryId : Int?    = null,
        userLat    : Double? = null,
        userLon    : Double? = null
    ): Result<List<ProviderModel>> = try {
        val response = api.getProviders(categoryId, userLat, userLon)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load providers."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // Updated: search also passes user GPS
    suspend fun searchProviders(
        query   : String,
        userLat : Double? = null,
        userLon : Double? = null
    ): Result<List<ProviderModel>> = try {
        val response = api.searchProviders(query.trim(), userLat, userLon)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception(
            errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getProviderById(id: Int): Result<ProviderModel> = try {
        val response = api.getProviderById(id)
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception("Provider not found."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getMyProviderProfile(): Result<ProviderModel> = try {
        val response = api.getMyProviderProfile()
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception("Could not load profile."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // NEW: Provider updates their GPS location on the backend
    suspend fun updateMyLocation(
        latitude     : Double,
        longitude    : Double,
        locationText : String? = null
    ): Result<MessageResponse> = try {
        val response = api.updateMyLocation(latitude, longitude, locationText)
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception("Failed to update location."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    suspend fun createBooking(
        providerId: Int, description: String,
        date: String, time: String, notes: String?
    ): Result<BookingModel> = try {
        val response = api.createBooking(
            BookingCreateRequest(providerId, description, date, time, notes))
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception(
            errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getResidentBookings(): Result<List<BookingModel>> = try {
        val response = api.getResidentBookings()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load bookings."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getProviderBookings(): Result<List<BookingModel>> = try {
        val response = api.getProviderBookings()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load bookings."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun updateBookingStatus(
        id: Int, status: String, notes: String? = null
    ): Result<BookingModel> = try {
        val response = api.updateBookingStatus(id, BookingStatusUpdate(status, notes))
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception(
            errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    suspend fun submitReview(
        bookingId: Int, rating: Int, comment: String?
    ): Result<ReviewModel> = try {
        val response = api.submitReview(ReviewCreateRequest(bookingId, rating, comment))
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception(
            errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getProviderReviews(providerId: Int): Result<List<ReviewModel>> = try {
        val response = api.getProviderReviews(providerId)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load reviews."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // ── Complaints ────────────────────────────────────────────────────────────

    suspend fun submitComplaint(
        bookingId: Int, message: String
    ): Result<ComplaintModel> = try {
        val response = api.submitComplaint(ComplaintCreateRequest(bookingId, message))
        if (response.isSuccessful && response.body() != null)
            Result.success(response.body()!!)
        else Result.failure(Exception(
            errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getMyComplaints(): Result<List<ComplaintModel>> = try {
        val response = api.getMyComplaints()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load complaints."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    // ── Local ─────────────────────────────────────────────────────────────────

    suspend fun logout()     = TokenManager.clearAll(context)
    suspend fun getRole()    = TokenManager.getRole(context)
    suspend fun isLoggedIn() = TokenManager.isLoggedIn(context)
    suspend fun getName()    = TokenManager.getName(context)
}
