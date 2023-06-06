package com.mobileplus.dummytriluc.data.model

import androidx.annotation.StringRes
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R

/**
 * Created by KO Huyn on 05/06/2023.
 */

enum class TargetUnit(val value: String, @StringRes val stringRes: Int) {
    TOTAL_POWER("total_power", R.string.total_power_punch),
    TIME_PRACTICE("time_practice", R.string.time_practice),
    TOTAL_PUNCH("total_punch", R.string.punch),
    TOTAL_CALORIES("total_calories", R.string.calories);

    override fun toString(): String {
        return DummyTriLucApplication.getInstance().getString(stringRes)
    }

    companion object {
        fun getType(type: String?): TargetUnit? {
            return values().find { it.value == type }
        }

        fun getDefault() = TOTAL_POWER
    }
}

enum class TargetType(val value: String, @StringRes val stringRes: Int) {
    BY_DAY("by_day", R.string.label_day),
    BY_WEEK("by_week", R.string.label_week),
    BY_MONTH("by_month", R.string.label_month);

    override fun toString(): String {
        return DummyTriLucApplication.getInstance().getString(stringRes)
    }

    companion object {
        fun getType(type: String?): TargetType? {
            return TargetType.values().find { it.value == type }
        }

        fun getDefault() = BY_DAY
    }
}