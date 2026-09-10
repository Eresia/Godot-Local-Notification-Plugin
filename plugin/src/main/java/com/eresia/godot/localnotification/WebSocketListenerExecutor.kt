package com.eresia.godot.localnotification

import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat
import com.neovisionaries.ws.client.HostnameUnverifiedException
import com.neovisionaries.ws.client.OpeningHandshakeException
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFactory

class WebsocketListenerExecutor(val godotPlugin : GodotLocalNotificationPlugin, val activity : Activity, val socketUrl : String) : Runnable {
    private var webSocket : WebSocket? = null
    public var service : WebSocketService? = null
    public var inConnectingState : Boolean = true

    inner class WebSocketServiceAdapter : WebSocketAdapter() {
        override fun onTextMessage(websocket : WebSocket, message : String) {
            godotPlugin.onWebSocketData(message)
        }
    }

    fun sendData(data : String) {
        if(isSocketOpen()) {
            webSocket!!.sendText(data)
        }
        else
        {
            godotPlugin.logError("Cant send data ")
        }
    }

    fun isSocketOpen() : Boolean {
        return webSocket != null && webSocket!!.isOpen
    }

    fun disconnect() {
        webSocket?.disconnect()
    }

    fun stop() {
        godotPlugin.log("Stop server")
        godotPlugin.onWebSocketDisconnected()
        service?.stopSelf()
    }

    override fun run() {
        godotPlugin.log("Connect to server...")
        webSocket = WebSocketFactory().createSocket(socketUrl)
        webSocket?.addListener(WebSocketServiceAdapter());

        try {
            webSocket?.connect()
            inConnectingState = false
            godotPlugin.onWebSocketConnected()
            godotPlugin.log("Connected to server")

            val intent = Intent(activity, WebSocketService::class.java)
            ContextCompat.startForegroundService(activity, intent)

            while(true) {
                if(!isSocketOpen()) {
                    godotPlugin.stopServer()
                    break;
                }
            }
        } catch (e: OpeningHandshakeException) {
            godotPlugin.logError("OpeningHandshakeException " + e.message)
            godotPlugin.stopServer()
        } catch (e: HostnameUnverifiedException) {
            godotPlugin.logError("HostnameUnverifiedException " + e.message)
            godotPlugin.stopServer()
        } catch (e: WebSocketException) {
            godotPlugin.logError("WebSocketException " + e.message)
            godotPlugin.stopServer()
        } finally {
            inConnectingState = false
        }
    }
}