package com.apero.qrart.ui.screen.qr.create

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
fun QRCreateRoute() {
    QRCreateScreen()
}

@Composable
fun QRCreateScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Cyan))
}