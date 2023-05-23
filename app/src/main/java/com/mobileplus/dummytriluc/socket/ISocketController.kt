package com.mobileplus.dummytriluc.socket

/**
 * Created by KO Huyn on 16/05/2023.
 */
interface ISocketController {
    fun startup(url: String, query: String)
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun registerListener(event: String)
    fun unregisterListener(event: String)
    fun unregisterAll()
    fun onListener(listener: (event: String, data: String?) -> Unit)
    fun emit(event: String, vararg args: Pair<String, String>)
}