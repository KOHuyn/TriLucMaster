package com.apero.qrart.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.apero.qrart.ui.QRArtAppState

/**
 * Created by KO Huyn on 26/06/2023.
 */
@Composable
fun QRNavHost(
    appState: QRArtAppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = QRRoute.TEMPLATE
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        templateScreen()
        historyScreen()
        qrTemplateScreen()
        qrCreateScreen()
    }
}
