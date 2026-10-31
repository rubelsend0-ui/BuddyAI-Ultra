package com.buddy.ultra.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.buddy.ultra.actions.ActionExecutionEngine
import com.buddy.ultra.backend.BuddyRepository
import com.buddy.ultra.database.BuddyDao
import com.buddy.ultra.database.MessageEntity
import com.buddy.ultra.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VoiceForegroundService : Service() {

    @Inject lateinit var speechEngine: SpeechEngine
    @Inject lateinit var repository: BuddyRepository
    @Inject lateinit var actionEngine: ActionExecutionEngine
    @Inject lateinit var dao: BuddyDao

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val CHANNEL_ID = "buddy_ultra_voice_daemon"

    override fun onCreate() {
        super.onCreate()
        makeNotificationChannel()
        startForeground(2001, createNotification("Buddy AI Ultra: Voice Daemon Active"))

        speechEngine.onSpeechCaptured = { spokenText ->
            processSpokenText(spokenText)
        }
    }

    private fun processSpokenText(text: String) {
        serviceScope.launch {
            dao.insertMessage(MessageEntity(role = "USER", text = text))
            speechEngine.setOrbState(OrbState.THINKING)

            val result = repository.sendQuery(text)
            result.onSuccess { res ->
                var finalSpeech = res.message
                var actionType: String? = null

                if (!res.actions.isNullOrEmpty()) {
                    speechEngine.setOrbState(OrbState.EXECUTING)
                    for (action in res.actions) {
                        finalSpeech = actionEngine.execute(action)
                        actionType = action.type
                    }
                }

                dao.insertMessage(MessageEntity(role = "BUDDY", text = finalSpeech, actionType = actionType))
                speechEngine.speak(finalSpeech) {
                    speechEngine.startListening()
                }
            }.onFailure {
                speechEngine.setOrbState(OrbState.OFFLINE)
                val fallback = "Offline: Buddy Core connection unavailable."
                dao.insertMessage(MessageEntity(role = "BUDDY", text = fallback))
                speechEngine.speak(fallback) {
                    speechEngine.startListening()
                }
            }
        }
    }

    private fun createNotification(statusText: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Buddy AI Ultra Intelligence")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    private fun makeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Buddy AI Continuous Listening",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        speechEngine.startListening()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        speechEngine.stopListening()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
