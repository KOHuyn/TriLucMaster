package com.apero.qrart.ui.screen.template

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
internal fun TemplateRoute() {
    TemplateScreen()
}

@Composable
internal fun TemplateScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Green)) {

    }
}