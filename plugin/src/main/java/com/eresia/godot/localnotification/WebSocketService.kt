package com.eresia.godot.localnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat


class WebSocketService(): Service() {

    private val notifId = 1336
    private val channelId = "LesPlusBgDeArras"
    private var notification: Notification? = null
    private var builder: NotificationCompat.Builder? = null

    private var godotPlugin : GodotLocalNotificationPlugin? = null

    /*private fun startForeground() {
        // Before starting the service as foreground check that the app has the
        // appropriate runtime permissions. In this case, verify that the user has
        // granted the CAMERA permission.
       /* val cameraPermission =
            PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (cameraPermission != PermissionChecker.PERMISSION_GRANTED) {
            // Without camera permissions the service cannot run in the foreground
            // Consider informing user or updating your app UI if visible.
            stopSelf()
            return
        }*/

        try {
            val textTitle = "Coucou je suis le background"
            val textContent = "Je suis le contenu"
            builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.atom)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val channel = NotificationChannel(
                channelId,
                "NomadeWebsocket",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.description = "Nomade websocket listener"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            /*ServiceCompat.startForeground(
                /* service = */ this,
                /* id = */ 100, // Cannot be 0
                /* notification = */ notification,
                /* foregroundServiceType = */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )*/

            godotPlugin?.log("Service started")
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                godotPlugin?.logError("App is not in valid state for launching foreground service")
            }
            else
            {
                godotPlugin?.logError(e.message ?: "Unknow error")
            }
        }
    }*/

    override fun onCreate() {
        super.onCreate();
        godotPlugin = GodotLocalNotificationPlugin.Singleton.instance

        godotPlugin?.log("Creating service")

        val textTitle = "Coucou je suis le background"
        val textContent = "Je suis le contenu"

        builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.atom)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val channel = NotificationChannel(
            channelId,
            "NomadeWebsocketChannem",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.description ="Nomade Websocket listener channel for foreground service notification"

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        godotPlugin?.log("Service starting")

        startForeground(notifId, builder?.build())

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        godotPlugin?.log("Service destroyed")
    }
}