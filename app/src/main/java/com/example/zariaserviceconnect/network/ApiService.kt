package com.example.zariaserviceconnect.network

import com.example.zariaserviceconnect.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Authentication ────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register/resident")
    suspend fun registerResident(
        @Body request: RegisterResidentRequest
    ): Response<MessageResponse>

    @Multipart
    @POST("auth/register/provider")
    suspend fun registerProvider(
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("password") password: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("years_of_experience") yearsOfExperience: RequestBody,
        @Part("description") description: RequestBody,
        @Part("location") location: RequestBody,
        @Part idDocument: MultipartBody.Part
    ): Response<MessageResponse>

    @GET("auth/me")
    suspend fun getMyProfile(): Response<UserModel>

    // ── Categories ────────────────────────────────────────────────────────────

    @GET("categories/")
    suspend fun getCategories(): Response<List<CategoryModel>>

    // ── Providers ─────────────────────────────────────────────────────────────

    // Updated: now accepts user GPS for location-based sorting
    @GET("providers/")
    suspend fun getProviders(
        @Query("category_id") categoryId: Int?    = null,
        @Query("user_lat")    userLat   : Double? = null,
        @Query("user_lon")    userLon   : Double? = null
    ): Response<List<ProviderModel>>

    // Updated: search also accepts user GPS for distance sorting
    @GET("providers/search")
    suspend fun searchProviders(
        @Query("q")        query  : String,
        @Query("user_lat") userLat: Double? = null,
        @Query("user_lon") userLon: Double? = null
    ): Response<List<ProviderModel>>

    @GET("providers/{id}")
    suspend fun getProviderById(@Path("id") id: Int): Response<ProviderModel>

    @GET("providers/me/profile")
    suspend fun getMyProviderProfile(): Response<ProviderModel>

    // NEW: Provider updates their GPS location
    @PATCH("providers/me/location")
    suspend fun updateMyLocation(
        @Query("latitude")      latitude    : Double,
        @Query("longitude")     longitude   : Double,
        @Query("location_text") locationText: String? = null
    ): Response<MessageResponse>

    // ── Bookings ──────────────────────────────────────────────────────────────

    @POST("bookings/")
    suspend fun createBooking(@Body request: BookingCreateRequest): Response<BookingModel>

    @GET("bookings/my/resident")
    suspend fun getResidentBookings(): Response<List<BookingModel>>

    @GET("bookings/my/provider")
    suspend fun getProviderBookings(): Response<List<BookingModel>>

    @PATCH("bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: Int,
        @Body update: BookingStatusUpdate
    ): Response<BookingModel>

    // ── Reviews ───────────────────────────────────────────────────────────────

    @POST("reviews/")
    suspend fun submitReview(@Body request: ReviewCreateRequest): Response<ReviewModel>

    @GET("reviews/provider/{providerId}")
    suspend fun getProviderReviews(
        @Path("providerId") providerId: Int
    ): Response<List<ReviewModel>>

    // ── Complaints ────────────────────────────────────────────────────────────

    @POST("complaints/")
    suspend fun submitComplaint(
        @Body request: ComplaintCreateRequest
    ): Response<ComplaintModel>

    @GET("complaints/my")
    suspend fun getMyComplaints(): Response<List<ComplaintModel>>

    @GET("complaints/")
    suspend fun getAllComplaints(
        @Query("status") status: String? = null
    ): Response<List<ComplaintModel>>

    @PUT("complaints/{id}/resolve")
    suspend fun resolveComplaint(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<ComplaintModel>
}
