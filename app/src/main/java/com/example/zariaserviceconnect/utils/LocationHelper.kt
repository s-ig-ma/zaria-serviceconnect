package com.example.zariaserviceconnect.utils

// ─────────────────────────────────────────────────────────────────────────────
// LocationHelper.kt
// Create this file at: utils/LocationHelper.kt
//
// Handles requesting the device's GPS location.
// Uses FusedLocationProviderClient — the standard Android location API.
// No Google Maps API key needed.
// ─────────────────────────────────────────────────────────────────────────────

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// Simple data class to hold a GPS position
data class UserLocation(
    val latitude  : Double,
    val longitude : Double
)

object LocationHelper {

    /**
     * Check if the app has location permission granted.
     */
    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get the device's current GPS location.
     *
     * Returns a UserLocation if successful, or null if:
     * - Permission is not granted
     * - GPS is off
     * - Location could not be obtained
     *
     * Uses a callback pattern because location is async.
     *
     * Usage:
     *   LocationHelper.getCurrentLocation(context) { location ->
     *       if (location != null) {
     *           // use location.latitude and location.longitude
     *       }
     *   }
     */
    fun getCurrentLocation(
        context  : Context,
        onResult : (UserLocation?) -> Unit
    ) {
        // Check permission first
        if (!hasPermission(context)) {
            onResult(null)
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationToken = CancellationTokenSource()

            // Request a fresh location with high accuracy
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    onResult(UserLocation(location.latitude, location.longitude))
                } else {
                    // Location returned null — try last known location instead
                    fusedClient.lastLocation.addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            onResult(UserLocation(lastLocation.latitude, lastLocation.longitude))
                        } else {
                            onResult(null)
                        }
                    }.addOnFailureListener {
                        onResult(null)
                    }
                }
            }.addOnFailureListener {
                onResult(null)
            }
        } catch (e: SecurityException) {
            // Permission was revoked after we checked
            onResult(null)
        }
    }
}
