package com.example.zariaserviceconnect.ui.shared

// All navigation routes defined as constants so there are no typos
object Routes {
    const val SPLASH       = "splash"
    const val WELCOME      = "welcome"
    const val LOGIN        = "login"
    const val REGISTER     = "register"

    // Resident
    const val RESIDENT_HOME      = "resident_home"
    const val CATEGORIES         = "categories"
    const val PROVIDERS_LIST     = "providers_list/{categoryId}/{categoryName}"
    const val PROVIDER_PROFILE   = "provider_profile/{providerId}"
    const val BOOK_SERVICE       = "book_service/{providerId}"
    const val RESIDENT_BOOKINGS  = "resident_bookings"
    const val LEAVE_REVIEW       = "leave_review/{bookingId}/{providerName}"
    const val SUBMIT_COMPLAINT   = "submit_complaint/{bookingId}"

    // Provider
    const val PROVIDER_HOME      = "provider_home"
    const val PROVIDER_JOBS      = "provider_jobs"
    const val PROVIDER_PROFILE_VIEW = "provider_profile_view"
}
