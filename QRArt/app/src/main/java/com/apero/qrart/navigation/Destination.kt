package com.apero.qrart.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.apero.qrart.ui.screen.qr.template.QRTemplateRoute
import com.apero.qrart.ui.screen.history.HistoryRoute
import com.apero.qrart.ui.screen.qr.create.QRCreateRoute
import com.apero.qrart.ui.screen.template.TemplateRoute

/**
 * Created by KO Huyn on 26/06/2023.
 */

fun NavController.navigateToTemplate(navOptions: NavOptions? = null) {
    this.navigate(QRRoute.TEMPLATE, navOptions)
}

fun NavGraphBuilder.templateScreen() {
    composable(route = QRRoute.TEMPLATE) {
        TemplateRoute()
    }
}

fun NavController.navigateToHistory(navOptions: NavOptions? = null) {
    this.navigate(QRRoute.HISTORY, navOptions)
}

fun NavGraphBuilder.historyScreen() {
    composable(route = QRRoute.HISTORY) {
        HistoryRoute()
    }
}

fun NavController.navigateToQrTemplate(navOptions: NavOptions? = null) {
    this.navigate(QRRoute.QR_TEMPLATE, navOptions)
}

fun NavGraphBuilder.qrTemplateScreen() {
    composable(route = QRRoute.QR_TEMPLATE) {
        QRTemplateRoute()
    }
}

fun NavController.navigateToQrCreate(navOptions: NavOptions? = null) {
    this.navigate(QRRoute.QR_CREATE, navOptions)
}

fun NavGraphBuilder.qrCreateScreen() {
    composable(route = QRRoute.QR_CREATE) {
        QRCreateRoute()
    }
}