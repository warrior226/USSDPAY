package com.example.mobilemhealthpay.services

import com.example.mobilemhealthpay.utils.Global
import io.socket.client.IO
import io.socket.client.Socket
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor() {
    private var socket: Socket? = null
    private val baseUrl = "https://dev.lagfo.com"
    private val socketPath = "/wallet/ws/socket.io/"

    data class DeviceStatus(
        val device_id: String,
        val device_name: String,
        val solde: String?,
        val date: String,
        val battery: String,
        val status: Int // 1: actif, 0: au repos
    )

    fun connect(deviceId: String, onDeviceInfoRequested: () -> Unit) {
        Timber.d("Socket.IO: Connecting with deviceId: $deviceId and secret_key: ${Global.secret_key}")
        try {
            val options = IO.Options().apply {
                path = socketPath
                transports = arrayOf("websocket")

                // Reconnection Configuration
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 2000
                reconnectionDelayMax = 10000
                timeout = 20000

                // Handshake Authentication per documentation 13.3
                val authData = mutableMapOf<String, String>()
                authData["secret_key"] = Global.secret_key
                authData["device_id"] = deviceId
                this.auth = authData

                Timber.d("Socket.IO: Handshake Auth Data: $authData")
                Timber.d("Socket.IO: Connection Path: $path")
            }

            socket = IO.socket(baseUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Timber.d("Socket.IO: Connected")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Timber.e("Socket.IO Connection Error: ${args.getOrNull(0)}")
            }

            // Using string literals for reconnection events as some constants are in Manager class
            socket?.on("reconnect_attempt") { args ->
                Timber.d("Socket.IO: Reconnection attempt #${args.getOrNull(0)}")
            }

            socket?.on("reconnect") { args ->
                Timber.i("Socket.IO: Reconnected after ${args.getOrNull(0)} attempts")
            }

            socket?.on("reconnect_error") { args ->
                Timber.e("Socket.IO: Reconnection Error: ${args.getOrNull(0)}")
            }

            // Handler for "request_device_info" per documentation 13.4
            socket?.on("request_device_info") {
                Timber.d("Socket.IO: request_device_info received")
                onDeviceInfoRequested()
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.getOrNull(0) as? String
                Timber.w("Socket.IO: Disconnected. Reason: $reason")
                // If server explicitly kicked us, we may need to reconnect manually
                if (reason == "io server disconnect") {
                    socket?.connect()
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Timber.e(e, "Socket.IO Connection Failed")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}
