package com.example.zariaserviceconnect.models

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType  : String,
    val role    : String,
    @SerializedName("user_id") val userId : Int,
    val name    : String
)

data class RegisterResidentRequest(
    val name     : String,
    val email    : String,
    val phone    : String,
    val password : String,
    val location : String?,
    @SerializedName("home_address") val homeAddress: String?
)

data class MessageResponse(val message: String, val success: Boolean)

// ── User ──────────────────────────────────────────────────────────────────────

data class UserModel(
    val id       : Int,
    val name     : String,
    val email    : String,
    val phone    : String,
    val location : String?,
    @SerializedName("home_address") val homeAddress: String?,
    val role     : String,
    @SerializedName("is_active")     val isActive    : Boolean,
    @SerializedName("profile_photo") val profilePhoto: String?,
    @SerializedName("created_at")    val createdAt   : String
)

// ── Category ──────────────────────────────────────────────────────────────────

data class CategoryModel(
    val id          : Int,
    val name        : String,
    val description : String?,
    val icon        : String?
)

// ── Provider ──────────────────────────────────────────────────────────────────

data class ProviderModel(
    val id          : Int,
    @SerializedName("user_id")      val userId     : Int,
    @SerializedName("category_id")  val categoryId : Int?,
    @SerializedName("service_name") val serviceName: String?,
    val description : String?,
    @SerializedName("years_of_experience") val yearsOfExperience: Int,
    val status         : String,
    @SerializedName("average_rating") val averageRating : Double,
    @SerializedName("total_reviews")  val totalReviews  : Int,
    val location   : String?,
    val latitude   : Double?,
    val longitude  : Double?,
    @SerializedName("distance_km") val distanceKm : Double?,
    @SerializedName("passport_photo_path") val passportPhotoPath: String?,
    @SerializedName("id_document_path") val idDocumentPath: String?,
    @SerializedName("skill_proof_path") val skillProofPath: String?,
    @SerializedName("has_shop_in_zaria") val hasShopInZaria: Boolean = false,
    @SerializedName("shop_address") val shopAddress: String?,

    // NEW: availability status
    // "available" = ready for bookings (green)
    // "busy"      = currently working on a booking (orange)
    // "offline"   = not available today (gray)
    @SerializedName("availability_status") val availabilityStatus: String = "available",

    @SerializedName("created_at") val createdAt : String,
    val user     : UserModel,
    val category : CategoryModel?
) {
    val displayServiceName: String
        get() = serviceName?.takeIf { it.isNotBlank() } ?: category?.name ?: "General Service"
}

// ── Booking ───────────────────────────────────────────────────────────────────

data class BookingCreateRequest(
    @SerializedName("provider_id")         val providerId        : Int,
    @SerializedName("service_description") val serviceDescription: String,
    @SerializedName("scheduled_date")      val scheduledDate     : String,
    @SerializedName("scheduled_time")      val scheduledTime     : String,
    @SerializedName("service_address")     val serviceAddress    : String?,
    val notes : String?
)

data class BookingStatusUpdate(
    val status         : String,
    @SerializedName("provider_notes") val providerNotes: String? = null
)

data class BookingModel(
    val id          : Int,
    @SerializedName("resident_id")         val residentId        : Int,
    @SerializedName("provider_id")         val providerId        : Int,
    @SerializedName("service_description") val serviceDescription: String,
    @SerializedName("scheduled_date")      val scheduledDate     : String,
    @SerializedName("scheduled_time")      val scheduledTime     : String,
    @SerializedName("service_address")     val serviceAddress    : String?,
    val status         : String,
    val notes          : String?,
    @SerializedName("provider_notes") val providerNotes : String?,
    @SerializedName("created_at")     val createdAt     : String,
    val resident : UserModel?,
    val provider : ProviderModel?
)

// ── Review ────────────────────────────────────────────────────────────────────

data class ReviewCreateRequest(
    @SerializedName("booking_id") val bookingId : Int,
    val rating  : Int,
    val comment : String?
)

data class ReviewModel(
    val id          : Int,
    @SerializedName("booking_id")  val bookingId  : Int,
    @SerializedName("resident_id") val residentId : Int,
    @SerializedName("provider_id") val providerId : Int,
    val rating     : Int,
    val comment    : String?,
    @SerializedName("created_at") val createdAt : String,
    val resident : UserModel
)

// ── Complaint ─────────────────────────────────────────────────────────────────

data class ComplaintCreateRequest(
    @SerializedName("booking_id") val bookingId : Int,
    val message : String
)

data class ComplaintModel(
    val id          : Int,
    @SerializedName("booking_id")  val bookingId  : Int,
    @SerializedName("user_id")     val userId     : Int,
    @SerializedName("provider_id") val providerId : Int,
    val message         : String,
    val status          : String,
    @SerializedName("resolution_note") val resolutionNote : String?,
    @SerializedName("created_at")      val createdAt      : String,
    val user     : UserBasicModel,
    val provider : ProviderBasicModel
)

data class UserBasicModel(
    val id    : Int,
    val name  : String,
    val email : String,
    val phone : String
)

data class ProviderBasicModel(
    val id   : Int,
    val user : UserBasicModel
)

data class MessageCreateRequest(
    @SerializedName("recipient_user_id") val recipientUserId: Int?,
    val content: String,
    @SerializedName("complaint_id") val complaintId: Int?
)

data class MessageModel(
    val id: Int,
    @SerializedName("complaint_id") val complaintId: Int?,
    @SerializedName("sender_user_id") val senderUserId: Int,
    @SerializedName("recipient_user_id") val recipientUserId: Int,
    val content: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
    val sender: UserModel,
    val recipient: UserModel
)

data class NotificationModel(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val title: String,
    val message: String,
    val type: String,
    @SerializedName("related_id") val relatedId: Int?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)
