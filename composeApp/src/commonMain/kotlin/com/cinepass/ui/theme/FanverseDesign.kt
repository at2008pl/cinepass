package com.cinepass.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val FanRadius = 16.dp

@Composable
fun FanGradientBackground(content: @Composable ColumnScope.() -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        FanBg,
                        Color(0xFFF9FAFF),
                        Color(0xFFF4F7FF),
                    )
                )
            ),
        content = content,
    )
}

@Composable
fun FanCard(
    modifier: Modifier = Modifier,
    accentBorder: Color = FanBorder,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(FanRadius))
            .border(1.dp, accentBorder, RoundedCornerShape(FanRadius)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(FanRadius),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}
