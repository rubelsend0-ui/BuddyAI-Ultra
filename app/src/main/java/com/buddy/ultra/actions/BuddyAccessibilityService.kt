package com.buddy.ultra.actions

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BuddyAccessibilityService : AccessibilityService() {

    companion object {
        var isAutoSendActive = false
        var targetRecipient = ""
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isAutoSendActive) return
        val rootNode = rootInActiveWindow ?: return

        // 1. WhatsApp Send Button by View Resource ID
        val sendNodes = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
        if (sendNodes.isNotEmpty()) {
            for (node in sendNodes) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("BuddyAccessibility", "Dispatched message via resource ID")
                    isAutoSendActive = false
                    return
                }
            }
        }

        // 2. WhatsApp Send Button by Content Description or Text ("Send")
        val fallbackNodes = rootNode.findAccessibilityNodeInfosByText("Send")
        for (node in fallbackNodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("BuddyAccessibility", "Dispatched message via text matching")
                isAutoSendActive = false
                return
            }
        }
    }

    override fun onInterrupt() {
        Log.w("BuddyAccessibility", "Accessibility service interrupted")
    }
}
