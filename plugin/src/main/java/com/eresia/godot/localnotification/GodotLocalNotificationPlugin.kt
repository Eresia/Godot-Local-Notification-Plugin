package com.eresia.godot.localnotification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class GodotLocalNotificationPlugin: GodotPlugin {

    class ForegroundNotification {
        var channelId : String = "WebSocketServiceChannel"
        var channelName : String = "WebSocket Service Channel"
        var channelDescription : String = "WebSocket Service Channel Description"
        var notificationTitle : String = "Foreground Notification"
        var notificationConnectedText : String = "Foreground Connected Text"
        var notificationIcon : Int = R.drawable.atom
    }

    var listener : WebsocketListenerExecutor? = null
    private var notificationManager : NotificationManager? = null
    private var channelId = "GodotLocalNotificationPlugin"

    public var foregroundNotification : ForegroundNotification = ForegroundNotification()

    private var executorService : ExecutorService? = null

    object Singleton
    {
        var instance: GodotLocalNotificationPlugin? = null
    }

    constructor(godot: Godot) : super(godot)
    {
        Singleton.instance = this
    }

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    @UsedByGodot
    private fun initForegroundNotification(channelId : String, channelName : String, channelDescription: String, title : String, connectedText : String) {
        foregroundNotification.channelId = channelId
        foregroundNotification.channelName = channelName
        foregroundNotification.channelDescription = channelDescription
        foregroundNotification.notificationTitle = title
        foregroundNotification.notificationConnectedText = connectedText
    }

    @UsedByGodot
    private fun initClassicNotification(newChannelId : String, channelName : String, channelDescription : String, notifImportance : Int = NotificationManager.IMPORTANCE_DEFAULT) {
        val activity = activity ?: return

        channelId = newChannelId

        val channel = NotificationChannel(
            channelId,
            channelName,
            notifImportance,
        )

        channel.description = channelDescription

        notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    @UsedByGodot
    private fun connectToServer(websocketUrl : String)
    {
        if(listener != null)
        {
            stopServer()
        }

        val activity = activity ?: return

        val permissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf<String?>(
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf<String?>(
                Manifest.permission.FOREGROUND_SERVICE,
            )
        }

        ActivityCompat.requestPermissions(activity, permissionArray, 0)
        listener = WebsocketListenerExecutor(this, activity, websocketUrl)
        executorService = Executors.newSingleThreadExecutor()
        executorService?.execute(listener)
    }

    @UsedByGodot
    private fun disconnect() {
        listener?.disconnect()
    }

    public fun stopServer() {
        listener?.stop()
        executorService?.shutdown()
        listener = null
    }

    @UsedByGodot
    private fun sendData(data : String) {
        listener?.sendData(data)
    }

    @UsedByGodot
    private fun inServerConnectingState() : Boolean {
        return listener != null && listener!!.inConnectingState
    }

    @UsedByGodot
    private fun isServerConnected() : Boolean {
        return listener != null && listener!!.isSocketOpen()
    }

    @UsedByGodot
    private fun notify(notifId : Int, notifTitle : String, notifText : String, notifPriority : Int = NotificationCompat.PRIORITY_DEFAULT) {
        val activity = activity ?: return

        val notification : Notification = NotificationCompat.Builder(activity, channelId)
            .setSmallIcon(R.drawable.atom)
            .setContentTitle(notifTitle)
            .setContentText(notifText)
            .setPriority(notifPriority)
            .build()

        notificationManager?.notify(notifId, notification)
    }

    fun onWebSocketData(data : String) {
        emitSignal("on_websocket_data", data)
    }

    fun onWebSocketConnected() {
        emitSignal("on_websocket_connected")
    }

    fun onWebSocketDisconnected() {
        emitSignal("on_websocket_disconnected")
    }

    override fun getPluginSignals(): Set<SignalInfo?> {
        return setOf(
            SignalInfo("on_websocket_data", String::class.java),
            SignalInfo("on_websocket_connected"),
            SignalInfo("on_websocket_disconnected"),
        )
    }

    fun toast(text : String)
    {
        runOnHostThread {
            Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
        }
    }

    fun log(text : String)
    {
        Log.i(pluginName, text)
    }

    fun logError(text : String)
    {
        Log.e(pluginName, text)
    }
}
