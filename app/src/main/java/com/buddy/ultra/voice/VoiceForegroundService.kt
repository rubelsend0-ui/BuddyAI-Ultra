package com.buddy.ultra.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.buddy.ultra.actions.ActionExecutionEngine
import com.buddy.ultra.actions.BuddyAccessibilityService
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
    private var wakeLock: PowerManager.WakeLock? = null
    private val NOTIF_CHANNEL_ID = "buddy_ultra_persistent_daemon"

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()

        // Acquire PARTIAL_WAKE_LOCK so CPU stays alive when screen turns off
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BuddyUltra::VoiceWakeLock").apply {
            acquire()
        }

        startForeground(3001, buildNotification("Buddy AI Ultra Active: Listening in Background"))

        speechEngine.onSpeechCaptured = { capturedText ->
            handleCapturedCommand(capturedText)
        }
    }

    private fun handleCapturedCommand(text: String) {
        serviceScope.launch {
            dao.insertMessage(MessageEntity(role = "USER", text = text))
            speechEngine.setOrbState(OrbState.THINKING)

            val result = repository.sendQuery(text)
            result.onSuccess { response ->
                var outText = response.message
                var actType: String? = null

                if (!response.actions.isNullOrEmpty()) {
                    speechEngine.setOrbState(OrbState.EXECUTING)
                    for (action in response.actions) {
                        if (action.type.equals("SEND_WHATSAPP_MESSAGE", ignoreCase = true)) {
                            BuddyAccessibilityService.isAutoSendActive = true
                        }
                        outText = actionEngine.execute(action)
                        actType = action.type
                    }
                }

                dao.insertMessage(MessageEntity(role = "BUDDY", text = outText, actionType = actType))
                speechEngine.speak(outText) {
                    // Loop back to active listening automatically
                    speechEngine.startListening()
                }
            }.onFailure {
                speechEngine.setOrbState(OrbState.OFFLINE)
                val fallback = "Offline: Backend connection unreachable."
                dao.insertMessage(MessageEntity(role = "BUDDY", text = fallback))
                speechEngine.speak(fallback) {
                    speechEngine.startListening()
                }
            }
        }
    }

    private fun buildNotification(message: String): Notification {
        val launchIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("⚡ Buddy AI Ultra Intelligence")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(launchIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Buddy AI Ultra Background Daemon",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Buddy AI active for background voice and automated tasks"
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        speechEngine.startListening()
        return START_STICKY // System will restart service automatically if killed
    }

    override fun onDestroy() {
        super.onDestroy()
        speechEngine.stopListening()
        wakeLock?.let { if (it.isHeld) it.release() }

        // Send broadcast to revive itself immediately
        val restartIntent = Intent("com.buddy.ultra.RESTART_VOICE_DAEMON")
        sendBroadcast(restartIntent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // If user swipes app from recent apps, service revives itself
        val restartIntent = Intent("com.buddy.ultra.RESTART_VOICE_DAEMON")
        sendBroadcast(restartIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
