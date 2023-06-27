package com.apero.qrart.ui.screen.qr.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Created by KO Huyn on 26/06/2023.
 */
@Composable
internal fun QRTemplateRoute() {
    QRTemplateScreen()
}

@Composable
internal fun QRTemplateScreen() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Yellow))
}
