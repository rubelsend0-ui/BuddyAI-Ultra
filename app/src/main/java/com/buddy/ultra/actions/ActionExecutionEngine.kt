package com.buddy.ultra.actions

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.SmsManager
import com.buddy.ultra.backend.ActionItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun execute(action: ActionItem): String {
        return when (action.type.uppercase()) {
            "OPEN_APP" -> handleOpenApp(action.parameters["appName"]?.toString() ?: "")
            "SEND_WHATSAPP_MESSAGE" -> handleWhatsApp(
                contact = action.parameters["contact"]?.toString() ?: "",
                message = action.parameters["message"]?.toString() ?: ""
            )
            "SEND_SMS", "PREPARE_SMS", "CREATE_REMINDER" -> handleSms(
                target = (action.parameters["receiver"] ?: action.parameters["contact"])?.toString() ?: "",
                body = (action.parameters["body"] ?: action.parameters["message"])?.toString() ?: ""
            )
            "CALL_CONTACT", "MAKE_CALL" -> handleCall(action.parameters["contact"]?.toString() ?: "")
            "TURN_FLASHLIGHT_ON" -> setFlashlight(true)
            "TURN_FLASHLIGHT_OFF" -> setFlashlight(false)
            "INCREASE_VOLUME" -> alterVolume(AudioManager.ADJUST_RAISE)
            "DECREASE_VOLUME" -> alterVolume(AudioManager.ADJUST_LOWER)
            "GET_BATTERY_STATUS" -> fetchBattery()
            "LOCK_DEVICE" -> lockPhone()
            "OPEN_SETTINGS" -> launchIntent(Settings.ACTION_SETTINGS, "Settings")
            "OPEN_WIFI_SETTINGS" -> launchIntent(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings")
            "OPEN_BLUETOOTH_SETTINGS" -> launchIntent(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth")
            else -> "Action ${action.type} is registered but not supported locally."
        }
    }

    private fun handleOpenApp(target: String): String {
        val query = target.lowercase().trim()
        if (query.contains("youtube")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                setPackage("com.google.android.youtube")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return if (canResolve(intent)) {
                context.startActivity(intent)
                "Opening YouTube."
            } else {
                launchBrowserSearch("YouTube")
            }
        }
        if (query.contains("whatsapp")) {
            val launch = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            return if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launch)
                "Opening WhatsApp."
            } else {
                "WhatsApp is not installed."
            }
        }
        return launchBrowserSearch(target)
    }

    private fun handleWhatsApp(contact: String, message: String): String {
        return try {
            val encoded = URLEncoder.encode(message, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=$encoded")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "WhatsApp opened with message for $contact."
        } catch (e: Exception) {
            "Unable to dispatch WhatsApp: ${e.localizedMessage}"
        }
    }

    private fun handleSms(target: String, body: String): String {
        return try {
            val smsMgr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsMgr.sendTextMessage(target, null, body, null, null)
            "SMS sent successfully to $target."
        } catch (e: Exception) {
            "SMS dispatch failed: ${e.message}"
        }
    }

    private fun handleCall(contactNameOrPhone: String): String {
        val resolvedPhone = resolveContactNumber(contactNameOrPhone) ?: contactNameOrPhone
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$resolvedPhone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            "Calling $contactNameOrPhone."
        } catch (e: Exception) {
            "Call failed. Make sure CALL_PHONE permission is granted."
        }
    }

    private fun resolveContactNumber(name: String): String? {
        return try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$name%"),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(0)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun setFlashlight(on: Boolean): String {
        return try {
            val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = camMgr.cameraIdList[0]
            camMgr.setTorchMode(id, on)
            if (on) "Flashlight turned on." else "Flashlight turned off."
        } catch (e: Exception) {
            "Flashlight control failed: ${e.message}"
        }
    }

    private fun alterVolume(direction: Int): String {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        return "Audio volume adjusted."
    }

    private fun fetchBattery(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return "Current battery level is $level%."
    }

    private fun lockPhone(): String {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, DeviceAdminLockReceiver::class.java)
        return if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
            "Phone locked."
        } else {
            "Device Administrator enrollment required to lock device."
        }
    }

    private fun launchIntent(action: String, title: String): String {
        val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
        return "Opening $title."
    }

    private fun launchBrowserSearch(query: String): String {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return "Searched for $query."
    }

    private fun canResolve(intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }
}
