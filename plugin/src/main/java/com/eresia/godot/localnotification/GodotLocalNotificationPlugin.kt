package com.eresia.godot.localnotification

import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot


class GodotLocalNotificationPlugin: GodotPlugin {

    object Singleton
    {
        var instance: GodotLocalNotificationPlugin? = null
    }

    constructor(godot: Godot) : super(godot)
    {
        Singleton.instance = this
    }

    public var service : WebSocketService? = null

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    @UsedByGodot
    private fun connectToServer(websocketUrl : String)
    {
        if(service != null)
        {
            service?.stop()
            service = null
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
        val intent = Intent(activity, WebSocketService::class.java)
        intent.putExtra("websocket_url", websocketUrl)
        ContextCompat.startForegroundService(activity, intent)
    }

    @UsedByGodot
    private fun stopServer() {
        service?.stop()
    }

    @UsedByGodot
    private fun sendData(data : String) {
        service?.sendData(data)
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
