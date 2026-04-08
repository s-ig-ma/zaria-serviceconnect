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
        @Part("category_id") categoryId: RequestBody?,
        @Part("service_name") serviceName: RequestBody?,
        @Part("years_of_experience") yearsOfExperience: RequestBody,
        @Part("description") description: RequestBody,
        @Part("location") location: RequestBody,
        @Part("has_shop_in_zaria") hasShopInZaria: RequestBody,
        @Part("shop_address") shopAddress: RequestBody?,
        @Part passportPhoto: MultipartBody.Part,
        @Part idDocument: MultipartBody.Part,
        @Part skillProof: MultipartBody.Part
    ): Response<MessageResponse>

    @GET("auth/me")
    suspend fun getMyProfile(): Response<UserModel>

    @Multipart
    @PATCH("users/me")
    suspend fun updateMyUserProfile(
        @Part("name") name: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("location") location: RequestBody?,
        @Part("home_address") homeAddress: RequestBody?,
        @Part profilePhoto: MultipartBody.Part?
    ): Response<UserModel>

    // ── Categories ────────────────────────────────────────────────────────────

    @GET("categories/")
    suspend fun getCategories(): Response<List<CategoryModel>>

    // ── Providers ─────────────────────────────────────────────────────────────

    @GET("providers/")
    suspend fun getProviders(
        @Query("category_id") categoryId: Int?    = null,
        @Query("user_lat")    userLat   : Double? = null,
        @Query("user_lon")    userLon   : Double? = null
    ): Response<List<ProviderModel>>

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

    @Multipart
    @PATCH("providers/me/profile")
    suspend fun updateMyProviderProfile(
        @Part("name") name: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("location") location: RequestBody?,
        @Part("service_name") serviceName: RequestBody?,
        @Part("years_of_experience") yearsOfExperience: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("has_shop_in_zaria") hasShopInZaria: RequestBody?,
        @Part("shop_address") shopAddress: RequestBody?,
        @Part profilePhoto: MultipartBody.Part?,
        @Part passportPhoto: MultipartBody.Part?,
        @Part idDocument: MultipartBody.Part?,
        @Part skillProof: MultipartBody.Part?
    ): Response<ProviderModel>

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

    // NEW: Provider manually sets their availability
    @PATCH("bookings/provider/availability")
    suspend fun setAvailability(
        @Query("availability_status") status: String
    ): Response<MessageResponse>

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

    @GET("messages/complaint/{complaintId}")
    suspend fun getComplaintMessages(
        @Path("complaintId") complaintId: Int,
        @Query("counterpart_user_id") counterpartUserId: Int? = null
    ): Response<List<MessageModel>>

    @POST("messages/")
    suspend fun sendMessage(
        @Body request: MessageCreateRequest
    ): Response<MessageModel>

    @GET("notifications/my")
    suspend fun getMyNotifications(): Response<List<NotificationModel>>

    @PATCH("notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: Int
    ): Response<NotificationModel>

    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<MessageResponse>
}
