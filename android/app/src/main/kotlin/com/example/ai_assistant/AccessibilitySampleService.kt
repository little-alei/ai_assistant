package com.example.ai_assistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

interface AccessibilityServiceCallback {
    fun onServiceConnected()
    fun onServiceDisconnected()
}

class AccessibilitySampleService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        service = this
        callback?.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        event.source?.apply {
            // recycle the nodeInfo object
            @Suppress("DEPRECATION")
            recycle()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        service = null
        callback?.onServiceDisconnected()
    }

    override fun onInterrupt() {}

    companion object {
        private var service: AccessibilitySampleService? = null
        private var callback: AccessibilityServiceCallback? = null

        @Synchronized
        fun getInstance(): AccessibilitySampleService? {
            return service
        }

        fun setCallback(callbackFun: AccessibilityServiceCallback) {
            callback = callbackFun
        }
    }
}
