package com.buddy.ultra.backend

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("message") val message: String,
    @SerializedName("language") val language: String = "auto"
)

data class ActionItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("parameters") val parameters: Map<String, Any>,
    @SerializedName("requiresConfirmation") val requiresConfirmation: Boolean = false
)

data class ChatResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("message") val message: String,
    @SerializedName("mode") val mode: String,
    @SerializedName("intent") val intent: String,
    @SerializedName("actions") val actions: List<ActionItem>? = emptyList(),
    @SerializedName("requiresConfirmation") val requiresConfirmation: Boolean = false
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("platform") val platform: String
)

data class StatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("activeModel") val activeModel: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("inferenceOnline") val inferenceOnline: Boolean
)
