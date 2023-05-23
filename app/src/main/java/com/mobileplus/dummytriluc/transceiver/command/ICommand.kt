package com.mobileplus.dummytriluc.transceiver.command

/**
 * Created by KO Huyn on 16/05/2023.
 */
interface ICommand {
    fun getEventName(): String
    fun params(): HashMap<String, Any?>
    fun log():String {
        return "${getEventName()}: => ${params().toList().joinToString(",") { (key, value) -> "$key($value)" }}"
    }
}