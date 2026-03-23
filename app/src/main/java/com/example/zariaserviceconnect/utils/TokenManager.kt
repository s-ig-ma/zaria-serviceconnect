package com.example.zariaserviceconnect.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Creates a DataStore instance attached to the app context
// DataStore is Android's modern way of storing small amounts of data on device
private val Context.dataStore by preferencesDataStore(name = "zaria_prefs")

object TokenManager {

    // Keys used to store/retrieve each piece of data
    private val KEY_TOKEN  = stringPreferencesKey("auth_token")
    private val KEY_ROLE   = stringPreferencesKey("user_role")
    private val KEY_USER_ID = intPreferencesKey("user_id")
    private val KEY_NAME   = stringPreferencesKey("user_name")

    // Save all login info after successful login
    suspend fun saveLoginData(context: Context, token: String, role: String, userId: Int, name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]   = token
            prefs[KEY_ROLE]    = role
            prefs[KEY_USER_ID] = userId
            prefs[KEY_NAME]    = name
        }
    }

    suspend fun getToken(context: Context): String? =
        context.dataStore.data.map { it[KEY_TOKEN] }.first()

    suspend fun getRole(context: Context): String? =
        context.dataStore.data.map { it[KEY_ROLE] }.first()

    suspend fun getUserId(context: Context): Int? =
        context.dataStore.data.map { it[KEY_USER_ID] }.first()

    suspend fun getName(context: Context): String? =
        context.dataStore.data.map { it[KEY_NAME] }.first()

    suspend fun isLoggedIn(context: Context): Boolean =
        getToken(context) != null

    // Clear everything on logout
    suspend fun clearAll(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}
