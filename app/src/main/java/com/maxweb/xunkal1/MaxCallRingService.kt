package com.maxweb.xunkal1

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log

class MaxCallRingService : Service() {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var callId: String = ""
    private var callerName: String = ""

    companion object {
        private const val TAG = "MaxCallRingService"
        private const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): MaxCallRingService = this@MaxCallRingService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        if (intent == null) {
            Log.e(TAG, "intent is null")
            return START_NOT_STICKY
        }

        callId = intent.getStringExtra("CALL_ID") ?: callId
        callerName = intent.getStringExtra("CALLER_NAME") ?: callerName

        val action = intent.action
        Log.d(TAG, "callId=$callId, callerName=$callerName, action=$action")

        when (action) {
            "START_RINGING" -> startRinging()
            "STOP_RINGING" -> stopRinging()
            "CALL_ACCEPTED" -> {
                stopRinging()
                stopSelf()
            }
            "CALL_DECLINED" -> {
                stopRinging()
                stopSelf()
            }
            "CALL_ENDED" -> {
                stopRinging()
                stopSelf()
            }
            else -> {
                // По умолчанию — начинаем звонить при старте foreground
                startRinging()
            }
        }

        return START_STICKY
    }

    private fun startRinging() {
        Log.d(TAG, "Starting ringtone for call from $callerName")
        
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MaxCallRingService, ringtoneUri)
                setLooping(true)
                setAudioStreamType(android.media.AudioManager.STREAM_RING)
                prepare()
                start()
            }
            Log.d(TAG, "Ringtone started")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing ringtone", e)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "max_calls"
            val notification = Notification.Builder(this, channelId)
                .setContentTitle("Входящий звонок")
                .setContentText("$callerName вызывает вас...")
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopRinging() {
        Log.d(TAG, "Stopping ringtone")
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone", e)
        }
        
        stopForeground(true)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopRinging()
        super.onDestroy()
    }
}
