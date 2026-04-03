package com.example.zariaserviceconnect.network

import android.content.Context
import com.example.zariaserviceconnect.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitClient creates the single Retrofit instance used across the whole app.
 *
 * IMPORTANT: Change BASE_URL to your PC's local IP address.
 * - Find your IP: open Command Prompt → type "ipconfig" → look for IPv4 Address
 * - Example: "http://192.168.1.105:8000/"
 */
object RetrofitClient {

    // ⚠️ CHANGE THIS to your PC's IP address
    private const val BASE_URL = "http://10.58.38.215:8000/"

    /**
     * AuthInterceptor automatically adds the JWT token to every request.
     * This means you don't have to manually add "Authorization: Bearer ..."
     * to every single API call — it's done here automatically.
     */
    class AuthInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val token = runBlocking { TokenManager.getToken(context) }
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            return chain.proceed(request)
        }
    }

    // Shows HTTP request/response logs in Logcat — very helpful for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun create(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
