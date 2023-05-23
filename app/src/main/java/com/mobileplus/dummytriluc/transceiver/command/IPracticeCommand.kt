package com.mobileplus.dummytriluc.transceiver.command

/**
 * Created by KO Huyn on 22/05/2023.
 */
interface IPracticeCommand : ICommand{
    override fun getEventName(): String {
        return "PRACTICE"
    }
}