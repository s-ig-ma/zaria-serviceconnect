package com.example.zariaserviceconnect.repository

import android.content.Context
import android.net.Uri
import com.example.zariaserviceconnect.models.*
import com.example.zariaserviceconnect.network.RetrofitClient
import com.example.zariaserviceconnect.utils.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    private fun String.toRB(): RequestBody = toRequestBody("text/plain".toMediaTypeOrNull())

    private fun String?.toOptionalRB(): RequestBody? =
        this?.takeIf { it.isNotBlank() }?.toRB()

    private fun Int?.toOptionalRB(): RequestBody? =
        this?.toString()?.toRB()

    private fun Boolean?.toOptionalRB(): RequestBody? =
        this?.toString()?.toRB()

    private fun uriToFile(uri: Uri, fallbackName: String): File {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open selected file.")
        val file = File(context.cacheDir, fallbackName)
        input.use { source -> file.outputStream().use { target -> source.copyTo(target) } }
        return file
    }

    private fun createFilePart(fieldName: String, file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            fieldName,
            file.name,
            file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        )
    }

    private fun createOptionalFilePart(fieldName: String, uri: Uri?, fallbackName: String): Pair<MultipartBody.Part?, File?> {
        if (uri == null) return null to null
        val file = uriToFile(uri, fallbackName)
        return createFilePart(fieldName, file) to file
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> = try {
        val response = api.login(LoginRequest(email, password))
        if (response.isSuccessful && response.body() != null) {
            val data = response.body()!!
            TokenManager.saveLoginData(context, data.accessToken, data.role, data.userId, data.name)
            Result.success(data)
        } else {
            Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server. Check your IP address."))
    }

    suspend fun registerResident(
        name: String,
        email: String,
        phone: String,
        password: String,
        location: String,
        homeAddress: String,
    ): Result<MessageResponse> = try {
        val response = api.registerResident(
            RegisterResidentRequest(name, email, phone, password, location, homeAddress)
        )
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun registerProvider(
        name: String,
        email: String,
        phone: String,
        password: String,
        categoryId: Int?,
        serviceName: String,
        yearsOfExperience: Int,
        description: String,
        location: String,
        hasShopInZaria: Boolean,
        shopAddress: String,
        passportPhotoUri: Uri,
        idDocumentUri: Uri,
        skillProofUri: Uri,
    ): Result<MessageResponse> = try {
        val passportFile = uriToFile(passportPhotoUri, "passport_upload")
        val idFile = uriToFile(idDocumentUri, "id_upload")
        val skillFile = uriToFile(skillProofUri, "skill_upload")

        val response = api.registerProvider(
            name.toRB(),
            email.toRB(),
            phone.toRB(),
            password.toRB(),
            categoryId.toOptionalRB(),
            serviceName.toOptionalRB(),
            yearsOfExperience.toString().toRB(),
            description.toRB(),
            location.toRB(),
            hasShopInZaria.toString().toRB(),
            shopAddress.toOptionalRB(),
            createFilePart("passport_photo", passportFile),
            createFilePart("id_document", idFile),
            createFilePart("skill_proof", skillFile),
        )

        passportFile.delete()
        idFile.delete()
        skillFile.delete()

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getMyProfile(): Result<UserModel> = try {
        val response = api.getMyProfile()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun updateMyUserProfile(
        name: String,
        phone: String,
        location: String,
        homeAddress: String,
        profilePhotoUri: Uri?,
    ): Result<UserModel> = try {
        val (photoPart, photoFile) = createOptionalFilePart("profile_photo", profilePhotoUri, "profile_photo")
        val response = api.updateMyUserProfile(
            name.toOptionalRB(),
            phone.toOptionalRB(),
            location.toOptionalRB(),
            homeAddress.toOptionalRB(),
            photoPart,
        )
        photoFile?.delete()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getCategories(): Result<List<CategoryModel>> = try {
        val response = api.getCategories()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load categories."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getProviders(
        categoryId: Int? = null,
        userLat: Double? = null,
        userLon: Double? = null,
    ): Result<List<ProviderModel>> = try {
        val response = api.getProviders(categoryId, userLat, userLon)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load providers."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun searchProviders(
        query: String,
        userLat: Double? = null,
        userLon: Double? = null,
    ): Result<List<ProviderModel>> = try {
        val response = api.searchProviders(query.trim(), userLat, userLon)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getProviderById(id: Int): Result<ProviderModel> = try {
        val response = api.getProviderById(id)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("Provider not found."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getMyProviderProfile(): Result<ProviderModel> = try {
        val response = api.getMyProviderProfile()
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("Could not load profile."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun updateMyProviderProfile(
        name: String,
        phone: String,
        location: String,
        serviceName: String,
        yearsOfExperience: Int,
        description: String,
        hasShopInZaria: Boolean,
        shopAddress: String,
        profilePhotoUri: Uri?,
        passportPhotoUri: Uri?,
        idDocumentUri: Uri?,
        skillProofUri: Uri?,
    ): Result<ProviderModel> = try {
        val (profilePart, profileFile) = createOptionalFilePart("profile_photo", profilePhotoUri, "provider_profile")
        val (passportPart, passportFile) = createOptionalFilePart("passport_photo", passportPhotoUri, "provider_passport")
        val (idPart, idFile) = createOptionalFilePart("id_document", idDocumentUri, "provider_id")
        val (skillPart, skillFile) = createOptionalFilePart("skill_proof", skillProofUri, "provider_skill")

        val response = api.updateMyProviderProfile(
            name.toOptionalRB(),
            phone.toOptionalRB(),
            location.toOptionalRB(),
            serviceName.toOptionalRB(),
            yearsOfExperience.toString().toOptionalRB(),
            description.toOptionalRB(),
            hasShopInZaria.toString().toOptionalRB(),
            shopAddress.toOptionalRB(),
            profilePart,
            passportPart,
            idPart,
            skillPart,
        )

        profileFile?.delete()
        passportFile?.delete()
        idFile?.delete()
        skillFile?.delete()

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun updateMyLocation(
        latitude: Double,
        longitude: Double,
        locationText: String? = null,
    ): Result<MessageResponse> = try {
        val response = api.updateMyLocation(latitude, longitude, locationText)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("Failed to update location."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun setAvailability(status: String): Result<MessageResponse> = try {
        val response = api.setAvailability(status)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun createBooking(
        providerId: Int,
        description: String,
        date: String,
        time: String,
        serviceAddress: String,
        notes: String?,
    ): Result<BookingModel> = try {
        val response = api.createBooking(
            BookingCreateRequest(providerId, description, date, time, serviceAddress, notes)
        )
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
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

    suspend fun updateBookingStatus(id: Int, status: String, notes: String? = null): Result<BookingModel> = try {
        val response = api.updateBookingStatus(id, BookingStatusUpdate(status, notes))
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun submitReview(bookingId: Int, rating: Int, comment: String?): Result<ReviewModel> = try {
        val response = api.submitReview(ReviewCreateRequest(bookingId, rating, comment))
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
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

    suspend fun submitComplaint(bookingId: Int, message: String): Result<ComplaintModel> = try {
        val response = api.submitComplaint(ComplaintCreateRequest(bookingId, message))
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
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

    suspend fun getComplaintMessages(
        complaintId: Int,
        counterpartUserId: Int? = null,
    ): Result<List<MessageModel>> = try {
        val response = api.getComplaintMessages(complaintId, counterpartUserId)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun sendMessage(
        recipientUserId: Int?,
        content: String,
        complaintId: Int?,
    ): Result<MessageModel> = try {
        val response = api.sendMessage(MessageCreateRequest(recipientUserId, content, complaintId))
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun getMyNotifications(): Result<List<NotificationModel>> = try {
        val response = api.getMyNotifications()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to load notifications."))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun markNotificationRead(notificationId: Int): Result<NotificationModel> = try {
        val response = api.markNotificationRead(notificationId)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun markAllNotificationsRead(): Result<MessageResponse> = try {
        val response = api.markAllNotificationsRead()
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception(errorMessage(response.code(), response.errorBody()?.string())))
    } catch (e: Exception) {
        Result.failure(Exception("Cannot connect to server."))
    }

    suspend fun logout() = TokenManager.clearAll(context)
    suspend fun getRole() = TokenManager.getRole(context)
    suspend fun isLoggedIn() = TokenManager.isLoggedIn(context)
    suspend fun getName() = TokenManager.getName(context)
}
