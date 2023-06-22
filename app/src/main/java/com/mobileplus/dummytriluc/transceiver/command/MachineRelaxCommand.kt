package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import com.mobileplus.dummytriluc.transceiver.mode.RelaxType
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 19/06/2023.
 */
@Parcelize
data class MachineRelaxCommand(val avgPower: Int, val avgHit: Int,val relaxType: RelaxType) :
    IPracticeCommand {
    override fun getIdPractice(): Long {
        return 2
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["avg-power"] = avgPower
        params["avg-hit"] = avgHit
        params["time"] = System.currentTimeMillis() / 1000
        params["object_type"] = relaxType.type
        return params
    }
    override fun getCommandMode(): CommandMode {
        return CommandMode.RELAX
    }
}