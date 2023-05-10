package com.mobileplus.dummytriluc.socket

import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

/**
 * Created by KOHuyn on 3/1/2021
 */
class SocketHelper(private val socket: Socket) {

    companion object {
        const val TAG = "socket"
    }

    private var roomKeyID: String = ""
    private var onSocketCallback: OnSocketCallback? = null

    fun setSocketID(id: String): SocketHelper {
        roomKeyID = id
        return this
    }

    fun setOnCallbackSocket(data: (Array<out Any>) -> Unit): SocketHelper {
        onSocketCallback = OnSocketCallback {
            data.invoke(it)
        }
        return this
    }

    fun build(): SocketHelper {
        logErr(TAG, "initSocket")
        try {
            if (!socket.connected()) {
                socket.on(Socket.EVENT_CONNECT, onConnect)
                socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
                socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                socket.on(AppConstants.EVENT_NEW_MESSAGE, onEventNewMessageListener)
                socket.on("debug", onDebug)
                socket.connect()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return this
    }

    fun destroy() {
        logErr(TAG, "destroySocket")
        socket.disconnect()
        socket.off(Socket.EVENT_CONNECT, onConnect)
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socket.off(AppConstants.EVENT_NEW_MESSAGE, onEventNewMessageListener)
        socket.off("debug", onDebug)
    }

    private fun emitNewMessage(value: String) {
        val channel = JSONObject()
        channel.put("channelID", value)
        socket.emit("subscribe", channel)
    }

    //    -----------------SOCKET IO----------------
    private var onConnect: Emitter.Listener = Emitter.Listener {
        logErr(TAG, "onConnect")
        emitNewMessage(roomKeyID)
    }

    private var onDisconnect: Emitter.Listener = Emitter.Listener {
        logErr(TAG, "onDisconnect")
        logErr(TAG, it[0].toString())
    }

    private var onConnectError: Emitter.Listener = Emitter.Listener {
        logErr(TAG, "onConnectError")
        try {
            logErr(TAG, it[0].toString())
        } catch (e: Exception) {
            e.logErr()
        }
    }

    private var onDebug: Emitter.Listener = Emitter.Listener {
        logErr(TAG, "onDebug ${it[0]}")
    }

    private var onEventNewMessageListener: Emitter.Listener = Emitter.Listener {
        logErr(TAG, "onEventNewMessageListener ${it[0]}")
        onSocketCallback?.setOnSocketCallback(it)
    }

    fun interface OnSocketCallback {
        fun setOnSocketCallback(arr: Array<out Any>)
    }
}