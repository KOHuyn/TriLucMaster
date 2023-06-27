package com.apero.qrart.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.apero.qrart.R

/**
 * Created by KO Huyn on 26/06/2023.
 */
enum class TopLevelDestination(
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
) {

    TEMPLATE(
        selectedIcon = R.drawable.ic_home_selected,
        unselectedIcon = R.drawable.ic_home,
        iconTextId = R.string.title_template,
        titleTextId = R.string.title_template,
    ),
    HISTORY(
        selectedIcon = R.drawable.ic_history_selected,
        unselectedIcon = R.drawable.ic_history,
        iconTextId = R.string.title_history,
        titleTextId = R.string.title_history,
    ),
}