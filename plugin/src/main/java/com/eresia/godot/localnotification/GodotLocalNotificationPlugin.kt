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

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    /**
     * Example showing how to declare a method that's used by Godot.
     *
     * Shows a 'Hello World' toast.
     */
    @UsedByGodot
    private fun beginBackgroundService()
    {
        val activity = godot.getActivity() ?: return

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
        ContextCompat.startForegroundService(activity, intent)
    }

    public fun log(text : String)
    {
        runOnHostThread {
            Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
        }

        Log.i(pluginName, text)
    }

    public fun logError(text : String)
    {
        runOnHostThread {
            Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
        }

        Log.e(pluginName, text)
    }
}
