package com.cinepass.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Auto-navigate after 2.5 s
    LaunchedEffect(Unit) {
        delay(2500)
        onGetStarted()
    }

    // Staggered dot pulse
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1200; 1f at 150; 0.25f at 600 },
            repeatMode = RepeatMode.Restart
        ), label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1200; 0.25f at 150; 1f at 350; 0.25f at 750 },
            repeatMode = RepeatMode.Restart
        ), label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1200; 0.25f at 350; 1f at 550; 0.25f at 950 },
            repeatMode = RepeatMode.Restart
        ), label = "d3"
    )

    val Gold = Color(0xFFC9973A)
    val GoldPale = Color(0xFFF5D78E)
    val InkDark = Color(0xFF0A0806)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InkDark),
        contentAlignment = Alignment.Center
    ) {
        // Subtle radial gold glow behind logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x33C9973A), Color(0x00000000)),
                    center = Offset(size.width / 2, size.height * 0.38f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width / 2, size.height * 0.38f)
            )
            // Decorative concentric arcs
            val cx = size.width / 2
            val cy = size.height * 0.38f
            val radii = listOf(size.width * 0.36f, size.width * 0.52f, size.width * 0.70f)
            val alphas = listOf(0.10f, 0.06f, 0.035f)
            radii.forEachIndexed { i, r ->
                drawCircle(
                    color = Gold.copy(alpha = alphas[i]),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Film icon box with gradient
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(colors = listOf(Color(0xFF8A5C1A), Gold))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎬", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "RS³ FILMS",
                color = GoldPale,
                fontSize = 26.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                letterSpacing = 7.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "FAN COMMUNITY",
                color = Gold.copy(alpha = 0.38f),
                fontSize = 10.sp,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(dot1, dot2, dot3).forEach { a ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .alpha(a)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                }
            }
        }
    }
}
