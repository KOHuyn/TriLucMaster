package com.mobileplus.dummytriluc.transceiver.mode

import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.ext.getOrNull
import org.json.JSONObject

/**
 * Created by KO Huyn on 22/06/2023.
 */
sealed class SendCommandFrom(val commandMode: CommandMode) {
    data class FromApp(val mode: CommandMode, val command: ICommand) : SendCommandFrom(mode)
    data class FromMachine(val mode: CommandMode, val data: JSONObject) : SendCommandFrom(mode)
}