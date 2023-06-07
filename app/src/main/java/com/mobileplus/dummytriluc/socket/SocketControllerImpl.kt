package com.mobileplus.dummytriluc.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter.Listener
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Created by KO Huyn on 16/05/2023.
 */
class SocketControllerImpl : ISocketController {
    interface EventListener {
        fun onEventListener(event: String, data: String?)
    }

    private var socket: Socket? = null
    private val eventCached: MutableSet<String> = mutableSetOf()
    private var eventListener: EventListener? = null

    override fun startup(url: String, query: String) {
        return try {
            val opts = IO.Options()
            opts.transports = arrayOf(WebSocket.NAME)
            opts.query = query
            socket = IO.socket(url, opts)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    override fun connect() {
        socket?.connect()
    }

    override fun disconnect() {
        socket?.disconnect()
    }

    override fun isConnected(): Boolean {
        return socket?.connected() ?: false
    }

    override fun registerListener(event: String) {
        if (eventCached.contains(event)) return
        val listener = Listener { args: Array<out Any>? ->
            val data = args?.firstOrNull()?.toString()
            Log.i(event, "__________SOCKET-RESPONSE__________")
            Log.i(event, "event:$event -> $data")
            eventListener?.onEventListener(event, args?.firstOrNull()?.toString())
        }
        val socketOn = socket?.on(event, listener)
        if (socketOn != null) {
            eventCached.add(event)
        }
    }

    override fun unregisterListener(event: String) {
        if (eventCached.contains(event)) {
            val socketOff = socket?.off(event)
            if (socketOff != null) {
                eventCached.remove(event)
            }
        }
    }

    override fun unregisterAll() {
        socket?.off()
        eventCached.clear()
    }

    override fun onListener(listener: (event: String, data: String?) -> Unit) {
        eventListener = object : EventListener {
            override fun onEventListener(event: String, data: String?) {
                listener(event, data)
            }
        }
    }

    override fun emit(event: String, vararg args: Pair<String, Any>) {
        val data = JSONObject()
        args.forEach { (key, value) ->
            data.put(key, value)
        }
        socket?.emit(event, data)
        Log.i(event,"__________SOCKET-REQUEST__________")
        Log.i(event, "event:$event -> $data")
    }
}