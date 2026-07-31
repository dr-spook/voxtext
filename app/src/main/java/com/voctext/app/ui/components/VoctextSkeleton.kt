package com.voctext.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VoctextSkeleton(
    lineCount: Int = 8,
    modifier: Modifier = Modifier,
) {
    val shimmerColors = listOf(
        androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.6f),
        androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.2f),
        androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim),
    )

    // Line widths mimicking real text
    val lineWidths = listOf(0.9f, 0.75f, 0.95f, 0.6f, 0.85f, 0.7f, 0.8f, 0.5f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(lineCount) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = lineWidths.getOrElse(index) { 0.8f })
                    .height(16.dp)
                    .background(
                        brush = brush,
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}