package com.eresia.godot.localnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.neovisionaries.ws.client.HostnameUnverifiedException
import com.neovisionaries.ws.client.OpeningHandshakeException
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class WebSocketService(): Service() {

    private val notifId = 1336
    private val channelId = "LesPlusBgDeArras"
    private var builder: NotificationCompat.Builder? = null
    private var godotPlugin : GodotLocalNotificationPlugin? = null
    private var notificationManager : NotificationManager? = null
    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var webSocket : WebSocket? = null

    inner class WebSocketServiceAdapter : WebSocketAdapter() {
        override fun onTextMessage(websocket : WebSocket, message : String) {
            godotPlugin?.onWebSocketData(message)
        }
    }

    inner class WebsocketListenerExecutor : Runnable {
        override fun run() {
            try {
                godotPlugin?.log("Connect to server...")
                webSocket?.connect()
                godotPlugin?.onWebSocketConnected()
                builder?.setContentText("Connected")
                godotPlugin?.log("Connected to server")

                notificationManager?.notify(notifId, builder?.build())

                while(true) {
                    if(webSocket == null || !webSocket!!.isOpen) {
                        stop()
                        break;
                    }
                }
            } catch (e: OpeningHandshakeException) {
                godotPlugin?.logError("OpeningHandshakeException " + e.message)
                stopSelf()
            } catch (e: HostnameUnverifiedException) {
                godotPlugin?.logError("HostnameUnverifiedException " + e.message)
                stopSelf()
            } catch (e: WebSocketException) {
                godotPlugin?.logError("WebSocketException " + e.message)
                stopSelf()
            }
        }
    }

    fun sendData(data : String) {
        if(webSocket != null && webSocket!!.isOpen) {
            webSocket!!.sendText(data)
            godotPlugin?.toast("Send data")
        }
        else
        {
            godotPlugin?.toast("Cant send data ")
        }
    }

    fun stop() {
        godotPlugin?.log("Stop server")
        webSocket?.disconnect()
        godotPlugin?.onWebSocketDisconnected()
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate();
        godotPlugin = GodotLocalNotificationPlugin.Singleton.instance
        godotPlugin?.service = this

        godotPlugin?.log("Creating service")

        val textTitle = "Nomade Admin"
        val textContent = "Connexion au serveur..."

        builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.atom)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val channel = NotificationChannel(
            channelId,
            "NomadeWebsocketChannel",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.description ="Nomade Websocket listener channel for foreground service notification"

        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        godotPlugin?.log("Service starting")

        webSocket = WebSocketFactory().createSocket(intent.getStringExtra("websocket_url"))
        webSocket?.addListener(WebSocketServiceAdapter());

        executorService.execute(WebsocketListenerExecutor())

        startForeground(notifId, builder?.build())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        executorService.shutdown()
        godotPlugin?.log("Service destroyed")
    }
}