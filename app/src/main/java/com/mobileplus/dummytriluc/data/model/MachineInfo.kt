package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KO Huyn on 18/05/2023.
 */
data class MachineInfo(
    @Expose
    @SerializedName("status")
    val status: Int? = null,
    @Expose
    @SerializedName("link_firmware_esp")
    val linkFirmwareEsp: String? = null,
    @Expose
    @SerializedName("link_firmware")
    val linkFirmware: String? = null,
    @Expose
    @SerializedName("status_firmware")
    val statusFirmware: Boolean? = null,
    @Expose
    @SerializedName("machine_room")
    val machineRoom: String? = null,
    @Expose
    @SerializedName("update_sound")
    val updateSound: Boolean? = null,
    @Expose
    @SerializedName("link_firmware_stm")
    val linkFirmwareStm: String? = null,
)