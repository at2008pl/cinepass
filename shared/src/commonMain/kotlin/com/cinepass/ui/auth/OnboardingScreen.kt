package com.cinepass.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ══════════════════════════════════════════════════════════════════════════
   Onboarding — 3 slides matching RS³ Films JSX design
══════════════════════════════════════════════════════════════════════════ */

private data class Slide(
    val bgStart: Color,
    val bgEnd: Color,
    val accent: Color,
    val icon: String,
    val tag: String,
    val title: String,
    val subtitle: String
)

private val slides = listOf(
    Slide(
        bgStart  = Color(0xFF0E0A06), bgEnd   = Color(0xFF1E1408),
        accent   = Color(0xFFC9973A),
        icon     = "🎬",
        tag      = "YOUR PASS TO THE UNIVERSE",
        title    = "Welcome to\nRS³ Films",
        subtitle = "India's most exclusive film fan club. Live events, merch drops, and more."
    ),
    Slide(
        bgStart  = Color(0xFF0A0E08), bgEnd   = Color(0xFF141E10),
        accent   = Color(0xFF4A9A6A),
        icon     = "🔗",
        tag      = "MULTI-LEVEL REWARDS",
        title    = "Refer Friends.\nEarn Coins.",
        subtitle = "Earn at 3 levels. Your referrals' referrals earn you coins too."
    ),
    Slide(
        bgStart  = Color(0xFF0A080E), bgEnd   = Color(0xFF14101E),
        accent   = Color(0xFF6A4A9A),
        icon     = "🎫",
        tag      = "100+ LIVE OFFERS",
        title    = "Unlock Exclusive\nOffers",
        subtitle = "Free movie tickets, premiere passes, merchandise, and more."
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var current by remember { mutableIntStateOf(0) }
    val slide = slides[current]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(slide.bgStart, slide.bgEnd),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 1000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Icon area ──────────────────────────────────────────────────
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(168.dp)) {
                Box(
                    modifier = Modifier.size(168.dp).clip(CircleShape)
                        .background(slide.accent.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier.size(126.dp).clip(CircleShape)
                        .background(slide.accent.copy(alpha = 0.13f))
                )
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(slide.accent.copy(alpha = 0.45f), slide.accent.copy(alpha = 0.65f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = slide.icon, fontSize = 38.sp)
                }
            }

            // ── Text content ───────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tag pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(slide.accent.copy(alpha = 0.14f))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = slide.tag,
                        color = slide.accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = slide.title,
                    color = Color(0xFFFBF0D8),
                    fontSize = 34.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    lineHeight = 42.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = slide.subtitle,
                    color = Color.White.copy(alpha = 0.42f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // ── Dots + button ──────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 22.dp)
                ) {
                    slides.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .width(if (i == current) 24.dp else 6.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i == current) slide.accent
                                    else Color.White.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
                Button(
                    onClick = { if (current < slides.lastIndex) current++ else onFinish() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = slide.accent,
                        contentColor = Color(0xFF0E0C08)
                    )
                ) {
                    Text(
                        text = if (current < slides.lastIndex) "Continue →" else "Get Started →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
