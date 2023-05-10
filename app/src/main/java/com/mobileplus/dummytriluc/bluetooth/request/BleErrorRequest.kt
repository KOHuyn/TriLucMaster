package com.mobileplus.dummytriluc.bluetooth.request

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.ui.utils.language.SPUtil

/**
 * Created by KO Huyn on 28/10/2021.
 */
@Entity(tableName = "error_ble_table")
data class BleErrorRequest(
    @Expose
    @SerializedName("code")
    val code: String = "json_error",
    @Expose
    @SerializedName("machine_id")
    var machineId: Int? = null,
    @Expose
    @SerializedName("content")
    var content: String? = null,
    @Expose
    @SerializedName("machine_version")
    val machineVersion: String = "N/A",
    @Expose
    @SerializedName("version_app")
    val versionApp: String = BuildConfig.VERSION_NAME,
    @Expose
    @SerializedName("app_type")
    val appType: String = "ANDROID",
    @Expose
    @SerializedName("lang")
    val lang: String = SPUtil.getInstance(DummyTriLucApplication.getInstance())
        .getSelectLanguage(),
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}