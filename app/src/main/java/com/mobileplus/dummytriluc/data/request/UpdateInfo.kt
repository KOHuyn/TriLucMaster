package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr

data class UpdateInfo(
    @Expose
    var gender: String? = null,
    @Expose
    var height: Double? = null,
    @SerializedName("height_unit")
    @Expose
    var heightUnit: String? = null,
    @Expose
    var weight: Double? = null,
    @SerializedName("weight_unit")
    @Expose
    var weightUnit: String? = null,
    @Expose
    var birthday: String? = null,
    @SerializedName("subject_id")
    @Expose
    var subjectId: Int? = null,
    @SerializedName("full_name")
    @Expose
    var fullName: String? = null
) {
    companion object {
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

    fun getGenderInfo(): String {
        return when (gender) {
            UserInfo.MALE -> loadStringRes(R.string.gender_male)
            UserInfo.FEMALE -> loadStringRes(R.string.gender_female)
            UserInfo.OTHER -> loadStringRes(R.string.gender_other)
            else -> loadStringRes(R.string.unknown)
        }
    }

    fun saveHeightFeet(input: Double) {
        heightUnit = UNIT_FEET
        height = input
    }

    fun saveHeightCentimeter(input: Int) {
        heightUnit = UNIT_CENTIMETER
        height = input.toDouble()
    }

    fun saveWeightKg(input: Double) {
        weight = input
        weightUnit = UNIT_KILOGRAM
    }

    fun saveWeightLbs(input: Double) {
        weight = input
        weightUnit = UNIT_POUND
    }

    fun getFullHeight(): String {
        return if (height != null) {
            "$height $heightUnit"
        } else loadStringRes(R.string.unknown)
    }

    fun getFullWeight(): String {
        return if (weight != null) {
            "$weight $weightUnit"
        } else loadStringRes(R.string.unknown)
    }

}