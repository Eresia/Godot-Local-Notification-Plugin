package com.eresia.godot.localnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat


class WebSocketService: Service() {

    private val notifId = 2465
    private var builder: NotificationCompat.Builder? = null
    private var notificationManager : NotificationManager? = null

    private var godotPlugin : GodotLocalNotificationPlugin? = null
    override fun onCreate() {
        super.onCreate()
        godotPlugin = GodotLocalNotificationPlugin.Singleton.instance

        if(godotPlugin == null) {
            return
        }

        godotPlugin!!.listener?.service = this
        godotPlugin!!.log("Creating service")

        val notification : GodotLocalNotificationPlugin.ForegroundNotification = godotPlugin!!.foregroundNotification

        builder = NotificationCompat.Builder(this, notification.channelId)
            .setSmallIcon(notification.notificationIcon)
            .setContentTitle(notification.notificationTitle)
            .setContentText(notification.notificationConnectedText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        godotPlugin?.log("Service starting")

        val notification : GodotLocalNotificationPlugin.ForegroundNotification = godotPlugin!!.foregroundNotification

        val channel = NotificationChannel(
            notification.channelId,
            notification.channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.description = notification.channelDescription

        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
        notificationManager?.notify(notifId, builder?.build())

        startForeground(notifId, builder?.build())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        godotPlugin?.log("Service destroyed")
    }
}