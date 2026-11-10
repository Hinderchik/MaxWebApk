package com.maxweb.xunkal1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

class MaxIncomingCallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MaxIncomingCallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val callId = intent.getStringExtra("CALL_ID") ?: return
        val sender = intent.getStringExtra("SENDER") ?: "Unknown"
        
        Log.d(TAG, "Received action: $action for call $callId from $sender")
        
        when (action) {
            "ACCEPT_CALL" -> {
                acceptCall(context, callId, sender)
            }
            "DECLINE_CALL" -> {
                declineCall(context, callId, sender)
            }
            "MAX_INCOMING_CALL" -> {
                handleIncomingCall(context, intent)
            }
        }
    }

    private fun acceptCall(context: Context, callId: String, sender: String) {
        Log.d(TAG, "Call accepted: $callId from $sender")
        
        // Снимаем уведомление
        NotificationManagerCompat.from(context).cancel(callId.hashCode())
        
        // Получаем WebView и отправляем сообщение в веб-интерфейс для принятия звонка
        // Здесь можно использовать JavaScript interface
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("ACCEPT_CALL_ID", callId)
            putExtra("CALL_SENDER", sender)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
        
        // Виброотклик
        // vibrate...
        
        // Звук вызова останавливаем
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audioManager.mode = android.media.AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone", e)
        }
    }

    private fun declineCall(context: Context, callId: String, sender: String) {
        Log.d(TAG, "Call declined: $callId from $sender")
        
        // Снимаем уведомление
        NotificationManagerCompat.from(context).cancel(callId.hashCode())
        
        // Можно отправить сигнал на сервер об отклонении звонка
        // Здесь можно добавить отправку через WebView JavaScript interface
    }

    private fun handleIncomingCall(context: Context, intent: Intent) {
        val callId = intent.getStringExtra("CALL_ID") ?: return
        val sender = intent.getStringExtra("SENDER") ?: "Unknown"
        val callerName = intent.getStringExtra("CALLER_NAME") ?: sender
        
        Log.d(TAG, "Incoming call from $callerName ($sender)")
        
        // Проверяем разрешения
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "No notification permission")
            return
        }
        
        // Запускаем службу для звукового сигнала звонка
        startCallRingingService(context, callId, sender)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
               NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun startCallRingingService(context: Context, callId: String, sender: String) {
        val serviceIntent = Intent(context, MaxCallRingService::class.java).apply {
            putExtra("CALL_ID", callId)
            putExtra("CALLER_NAME", sender)
        }
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ring service", e)
        }
    }

    /**
     * Проверка наличия телефонного разрешения
     */
    fun isPhonePermissionGranted(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
