package com.buddy.ultra.backend

import com.buddy.ultra.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuddyRepository @Inject constructor(
    private val securityManager: SecurityManager
) {
    private fun getClient(): BuddyApiService {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()

        var base = securityManager.getApiUrl()
        if (!base.endsWith("/")) base += "/"

        return Retrofit.Builder()
            .baseUrl(base)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BuddyApiService::class.java)
    }

    suspend fun sendQuery(message: String): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getClient()
            val res = api.sendChat(
                token = securityManager.getApiToken(),
                request = ChatRequest(sessionId = "buddy-ultra-session", message = message)
            )
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("HTTP Error ${res.code()}: ${res.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pingHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = getClient().getHealth()
            res.isSuccessful && res.body()?.status == "UP"
        } catch (_: Exception) {
            false
        }
    }
}
