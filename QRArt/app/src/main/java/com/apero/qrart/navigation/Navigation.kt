package com.apero.qrart.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.apero.qrart.R
import com.apero.qrart.ui.component.BottomBarShape
import com.apero.qrart.ui.theme.ColorDisable
import com.apero.qrart.ui.theme.ColorPrimary

/**
 * Created by KO Huyn on 26/06/2023.
 */

@Preview(name = "QRBottomBar")
@Composable
fun PreviewNavBar() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .background(Color.Black)
                .weight(1f)
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
        QRBottomBar(
            onNavigateToDestination = {},
            onNavigateToCreateQR = {},
            currentDestination = null,
        )
    }
}

@Composable
fun QRBottomBar(
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    onNavigateToCreateQR: () -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        BottomBarShape(modifier = modifier, diameter = 60.dp, backgroundColor = Color.Black)
        ConstraintLayout(
            modifier = modifier.fillMaxWidth()
        ) {
            val (template, qrArtIcon,qrArtLabel, history) = createRefs()
            QRNavigationBarItem(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .constrainAs(template) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(qrArtIcon.start)
                    },
                item = TopLevelDestination.TEMPLATE,
                selected = currentDestination.isTopLevelDestinationInHierarchy(TopLevelDestination.TEMPLATE),
                onClick = { onNavigateToDestination(TopLevelDestination.TEMPLATE) },
            )
            Image(
                painter = painterResource(id = R.drawable.img_home_qr_art),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        onNavigateToCreateQR()
                    }
                    .size(56.dp)
                    .constrainAs(qrArtIcon) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
            Text(
                modifier = Modifier.constrainAs(qrArtLabel) {
                    start.linkTo(qrArtIcon.start)
                    end.linkTo(qrArtIcon.end)
                    top.linkTo(qrArtIcon.bottom)
                    bottom.linkTo(parent.bottom)
                },
                text = stringResource(id = R.string.title_ai_qr).uppercase(),
                style = TextStyle(color = Color.White, fontSize = 16.sp)
            )
            QRNavigationBarItem(
                modifier = Modifier.constrainAs(history) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(qrArtIcon.end)
                },
                item = TopLevelDestination.HISTORY,
                selected = currentDestination.isTopLevelDestinationInHierarchy(TopLevelDestination.HISTORY),
                onClick = { onNavigateToDestination(TopLevelDestination.HISTORY) },
            )
        }
    }
}

@Composable
fun QRNavigationBarItem(
    modifier: Modifier = Modifier,
    item: TopLevelDestination,
    selected: Boolean,
    onClick: (item: TopLevelDestination) -> Unit
) {
    Column(
        modifier = modifier
            .height(IntrinsicSize.Max)
            .padding(vertical = 8.dp)
            .clickable { onClick(item) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier,
            painter = painterResource(id = if (selected) item.selectedIcon else item.unselectedIcon),
            contentDescription = stringResource(item.iconTextId)
        )
        Text(
            text = stringResource(id = item.titleTextId),
            color = if (selected) ColorPrimary else ColorDisable,
            fontSize = 13.sp
        )
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
