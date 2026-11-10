package com.maxweb.xunkal1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MaxFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MaxFcmService"
        private const val CHANNEL_ID = "max_messages"
        private const val CHANNEL_CALLS = "max_calls"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        Log.d(TAG, "FCM Service started")
    }

    /**
     * Создает каналы уведомлений для Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Канал для сообщений
            val messageChannel = NotificationChannel(
                CHANNEL_ID,
                "Сообщения MAX",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о новых сообщениях"
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#471AFF")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            // Канал для звонков
            val callChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Звонки MAX",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о входящих звонках"
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
                setSound(
                    android.provider.Settings.System.DEFAULT_RINGTONE_URI,
                    null
                )
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(messageChannel)
            notificationManager.createNotificationChannel(callChannel)
            
            Log.d(TAG, "Notification channels created")
        }
    }

    /**
     * Получение токена FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Сохраняем токен в SharedPreferences или отправляем на сервер
        com.maxweb.xunkal1.prefs.AppPrefs(this).fcmToken = token
    }

    /**
     * Обработка входящего push-уведомления
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Данные из payload
        remoteMessage.data.isNotEmpty().let {
            val sender = remoteMessage.data["sender"] ?: "MAX"
            val message = remoteMessage.data["message"] ?: ""
            val type = remoteMessage.data["type"] ?: "message"
            val messageId = remoteMessage.data["messageId"] ?: ""
            val timestamp = remoteMessage.data["timestamp"] ?: ""
            
            Log.d(TAG, "Data: sender=$sender, type=$type, message=$message")
            
            when (type) {
                "call_request" -> {
                    // Входящий звонок
                    sendCallNotification(sender, messageId)
                }
                "call_end" -> {
                    // Звонок завершён
                    sendCallEndedNotification(sender, messageId)
                }
                else -> {
                    // Обычное сообщение
                    sendMessageNotification(sender, message, messageId)
                }
            }
        }
        
        // Уведомление из notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification title: ${notification.title}, body: ${notification.body}")
        }
    }

    /**
     * Отправка уведомления о сообщении
     */
    private fun sendMessageNotification(sender: String, message: String, messageId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_TYPE", "message")
            putExtra("MESSAGE_ID", messageId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, messageId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(sender)
            .setContentText(message.takeIf { it.isNotBlank() } ?: "Новое сообщение")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(messageId.hashCode(), notification)
        
        Log.d(TAG, "Notification sent: $sender - $message")
    }

    /**
     * Отправка уведомления о входящем звонке
     */
    private fun sendCallNotification(sender: String, messageId: String) {
        val acceptIntent = Intent(this, MaxIncomingCallReceiver::class.java).apply {
            action = "ACCEPT_CALL"
            putExtra("CALL_ID", messageId)
            putExtra("SENDER", sender)
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            this, 0, acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(this, MaxIncomingCallReceiver::class.java).apply {
            action = "DECLINE_CALL"
            putExtra("CALL_ID", messageId)
            putExtra("SENDER", sender)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            this, 1, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Входящий звонок")
            .setContentText("$sender вызывает вас...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setFullScreenIntent(acceptPendingIntent, true)
            .addAction(R.drawable.ic_accept, "Принять", acceptPendingIntent)
            .addAction(R.drawable.ic_decline, "Отклонить", declinePendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(messageId.hashCode(), notification)
        
        Log.d(TAG, "Call notification sent: $sender")
    }

    /**
     * Отправка уведомления об окончании звонка
     */
    private fun sendCallEndedNotification(sender: String, messageId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, messageId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Звонок завершён")
            .setContentText("Разговор с $sender окончен")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(messageId.hashCode(), notification)
    }
}
