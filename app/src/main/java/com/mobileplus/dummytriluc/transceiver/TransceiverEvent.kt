package com.mobileplus.dummytriluc.transceiver

import io.socket.client.Socket

/**
 * Created by KO Huyn on 17/05/2023.
 */
enum class TransceiverEvent(val eventName: String) {
    CONNECT(Socket.EVENT_CONNECT),
    DISCONNECT(Socket.EVENT_DISCONNECT),
    CONNECT_ERROR(Socket.EVENT_CONNECT_ERROR),
    NEW_MESSAGE("new_message"),
    PRACTICE("PRACTICE"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
    CONNECT_MACHINE("CONNECT_MACHINE");

    companion object {
        fun getEvent(eventName: String): TransceiverEvent? {
            return values().find { it.eventName == eventName || it.name == eventName }
        }
    }
}