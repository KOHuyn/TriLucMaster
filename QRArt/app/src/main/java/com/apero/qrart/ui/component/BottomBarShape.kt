package com.apero.qrart.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Created by KO Huyn on 27/06/2023.
 */

@Preview
@Composable
private fun BottomBarShapePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomBarShape(
            modifier = Modifier
                .defaultMinSize(minHeight = 56.dp)
                .height(56.dp)
        )
    }
}


@Composable
fun BottomBarShape(
    modifier: Modifier = Modifier,
    diameter: Dp = 100.dp,
    cornerRadiusDp: Dp = 10.dp,
    backgroundColor: Color = Color.Black
) {
    val path1 = remember { Path() }
    val path2 = remember { Path() }

    val radiusDp = diameter / 2

    val density = LocalDensity.current
    val cutoutRadius = density.run { radiusDp.toPx() }
    val cornerRadius = density.run { cornerRadiusDp.toPx() }
    Canvas(modifier = modifier
        .height(IntrinsicSize.Max)
        .fillMaxWidth()) {
        // Since we remember paths from each recomposition we reset them to have fresh ones
        // You can create paths here if you want to have new path instances
        path1.reset()
        path2.reset()
        val (_path1, _path2) = roundedRectanglePath(size, cornerRadius, cutoutRadius)
        path1.addPath(_path1)
        path2.addPath(_path2)
        val center = (size.width / 2f)
        drawPath(
            path2, Brush.linearGradient(
                listOf(
                    Color(0xFFAD5A69), Color(0xFFE74664), Color(0xFF7160FB)
                ),
                start = Offset(center - cutoutRadius, 0f),
                end = Offset(center + cutoutRadius, 0f),
            )
        )
        drawPath(path1, backgroundColor)
    }
}

private fun roundedRectanglePath(
    size: Size,
    cornerRadius: Float,
    fabRadius: Float,
): Pair<Path, Path> {
    val (path1, path2) = Path() to Path()
    val offset = 5f
    val centerX = size.width / 2
    val x0 = centerX - fabRadius
    val y0 = 0f
    // Top left arc
    val radius = cornerRadius * 2

    val fabRadius2: Float = fabRadius + offset
    val x1: Float = centerX - fabRadius2
    val y1 = y0 - offset
    path1.arcTo(
        rect = Rect(
            left = 0f, top = 0f, right = radius, bottom = radius
        ), startAngleDegrees = 180.0f, sweepAngleDegrees = 90.0f, forceMoveTo = false
    )

    path1.lineTo(x = x0, y = y0)
    path2.lineTo(x = x1, y = y1)

    // bezier curve with (P2, C1, C2, P3)
    path1.cubicTo(
        x1 = x0, y1 = y0, x2 = x0, y2 = y0 - fabRadius, x3 = centerX, y3 = -fabRadius
    )

    path2.cubicTo(
        x1 = x1, y1 = y1, x2 = x1, y2 = -fabRadius2, x3 = centerX, y3 = -fabRadius2
    )

    // bezier curve with (P3, C4, C3, P4)
    path1.cubicTo(
        x1 = centerX,
        y1 = -fabRadius,
        x2 = centerX + fabRadius,
        y2 = -fabRadius,
        x3 = centerX + fabRadius,
        y3 = y0
    )

    path2.cubicTo(
        x1 = centerX,
        y1 = -fabRadius2,
        x2 = centerX + fabRadius,
        y2 = -fabRadius2,
        x3 = centerX + fabRadius2,
        y3 = y1
    )
    path1.lineTo(x = size.width - cornerRadius, y = 0f)
    path2.lineTo(x = size.width - cornerRadius, y = 0f)

    // Top right arc
    path1.arcTo(
        rect = Rect(
            left = size.width - radius, top = 0f, right = size.width, bottom = radius
        ), startAngleDegrees = -90.0f, sweepAngleDegrees = 90.0f, forceMoveTo = false
    )

    path1.lineTo(x = 0f + size.width, y = size.height)

    // Bottom right arc
    path1.arcTo(
        rect = Rect(
            left = size.width, top = size.height, right = size.width, bottom = size.height
        ), startAngleDegrees = 0f, sweepAngleDegrees = 90.0f, forceMoveTo = false
    )

    path1.lineTo(x = cornerRadius, y = size.height)

    // Bottom left arc
    path1.arcTo(
        rect = Rect(
            left = 0f, top = size.height, right = 0f, bottom = size.height
        ), startAngleDegrees = 90.0f, sweepAngleDegrees = 90.0f, forceMoveTo = false
    )

    path1.lineTo(x = 0f, y = cornerRadius)
    path1.close()
    path2.close()
    return path1 to path2
}
