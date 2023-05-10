package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes

data class UserData(
    @SerializedName("userinfo")
    @Expose
    var userInfo: UserInfo,
    @Expose
    var token: String
)

data class LevelUserInfo(
    @SerializedName("title")
    @Expose
    var title: String? = null,
    @SerializedName("next_point")
    @Expose
    var nextPoint: Int? = null,
)

data class UserInfo(
    @Expose
    val birthday: String? = null,
    @Expose
    val address: String? = null,
    @Expose
    val gender: String? = null,
    @Expose
    val phone: String? = null,
    @Expose
    @SerializedName("avatar_path")
    var avatarPath: String? = null,
    @Expose
    @SerializedName("level")
    var level: LevelUserInfo? = null,
    @Expose
    @SerializedName("height_unit")
    val heightUnit: String? = null,
    @Expose
    @SerializedName("weight_unit")
    val weightUnit: String? = null,
    @Expose
    val weight: Double? = null,
    @Expose
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null,
    @Expose
    val email: String? = null,
    @Expose
    val height: Double? = null,
    @Expose
    @SerializedName("subject_id")
    val subjectId: Int? = null,
    @Expose
    val status: Int? = null,
    @SerializedName("is_master")
    @Expose
    val isMaster: String? = null,
    @Expose
    val subject: SubjectItem? = null,
    @Expose
    val practice: MutableList<ItemCoachPractice>? = mutableListOf(),
    @SerializedName("reward")
    @Expose
    val reward: MutableList<ItemAppellation>? = mutableListOf(),
    @SerializedName("master_info")
    @Expose
    val masterInfo: MasterInfo?
) {

    companion object {
        const val heightConvertValue = 30.48
        const val weightConvertValue = 0.45359237
        const val UNIT_FEET = "feet"
        const val UNIT_FEET_SORT = "ft"
        const val UNIT_CENTIMETER = "cm"
        const val UNIT_KILOGRAM = "kg"
        const val UNIT_POUND = "pound"
        const val UNIT_POUND_SORT = "Lbs"
        const val MALE = "male"
        const val FEMALE = "female"
        const val OTHER = "other"
    }

    fun getFullWeight(): String? {
        return if (weight != null) "$weight ${weightUnit ?: ""}" else null
    }

    fun getGenderInfo(): String {
        return when (gender) {
            MALE -> loadStringRes(R.string.gender_male)
            FEMALE -> loadStringRes(R.string.gender_female)
            OTHER -> loadStringRes(R.string.gender_other)
            else -> loadStringRes(R.string.gender_other)
        }
    }

    fun getFullHeight(): String {
        return "$height $heightUnit"
    }
}

data class MasterInfo(
    @SerializedName("so_de_tu")
    @Expose
    val discipleCount: String? = null,
    @SerializedName("so_bai_hoc")
    @Expose
    val lessonCount: String? = null,
    @SerializedName("so_nguoi_luyen_tap")
    @Expose
    val participants: String? = null,
    @SerializedName("trang_thai_bai_su")
    @Expose
    val statusReceiveMaster: Int? = null,

    )
