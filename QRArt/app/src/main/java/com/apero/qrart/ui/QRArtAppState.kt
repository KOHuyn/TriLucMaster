package com.apero.qrart.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.apero.qrart.navigation.TopLevelDestination
import com.apero.qrart.navigation.navigateToHistory
import com.apero.qrart.navigation.navigateToQrCreate
import com.apero.qrart.navigation.navigateToTemplate
import kotlinx.coroutines.CoroutineScope

/**
 * Created by KO Huyn on 26/06/2023.
 */

@Composable
fun rememberQRArtAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): QRArtAppState {
    return remember(navController, coroutineScope) {
        QRArtAppState(navController, coroutineScope)
    }
}

data class QRArtAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.values().asList()

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }

        when (topLevelDestination) {
            TopLevelDestination.TEMPLATE -> navController.navigateToTemplate(topLevelNavOptions)
            TopLevelDestination.HISTORY -> navController.navigateToHistory(topLevelNavOptions)
        }
    }
}