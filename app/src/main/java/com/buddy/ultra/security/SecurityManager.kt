package com.buddy.ultra.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "buddy_ultra_vault",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiUrl(): String = prefs.getString("api_url", "https://berry-theatre-works-transcription.trycloudflare.com") ?: ""
    fun setApiUrl(url: String) = prefs.edit().putString("api_url", url.trimEnd('/')).apply()

    fun getApiToken(): String = prefs.getString("api_token", "buddy-secret-key-prod-007") ?: ""
    fun setApiToken(token: String) = prefs.edit().putString("api_token", token).apply()

    fun getWakeWord(): String = prefs.getString("wake_phrase", "hey buddy") ?: "hey buddy"
    fun setWakeWord(phrase: String) = prefs.edit().putString("wake_phrase", phrase.lowercase().trim()).apply()

    fun isContinuousListening(): Boolean = prefs.getBoolean("continuous_listen", true)
    fun setContinuousListening(enabled: Boolean) = prefs.edit().putBoolean("continuous_listen", enabled).apply()
}
