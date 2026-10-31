package com.buddy.ultra.actions

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DeviceAdminLockReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Buddy AI Ultra: Device Administrator Enabled", Toast.LENGTH_SHORT).show()
    }
    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Buddy AI Ultra: Device Administrator Disabled", Toast.LENGTH_SHORT).show()
    }
}
