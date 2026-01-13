package com.example.summarytube

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FloatingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startService(Intent(context, FloatingService::class.java))
        }
    }
}
