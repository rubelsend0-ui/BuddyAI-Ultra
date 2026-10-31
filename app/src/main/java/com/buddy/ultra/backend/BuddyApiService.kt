package com.buddy.ultra.backend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BuddyApiService {
    @GET("api/v1/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/v1/status")
    suspend fun getStatus(): Response<StatusResponse>

    @POST("api/v1/chat")
    suspend fun sendChat(
        @Header("X-Buddy-Token") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
