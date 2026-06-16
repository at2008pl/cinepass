package com.cinepass.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val RGold     = Color(0xFFC9973A)
private val RGold3    = Color(0xFFF5D78E)
private val RInk      = Color(0xFF0E0C08)
private val RInk2     = Color(0xFF1E1A10)
private val RSurface  = Color(0xFFFDFAF3)
private val RWhite    = Color(0xFFFFFFFF)
private val RMuted    = Color(0xFF9A8A6A)
private val RFaint    = Color(0xFFEDE8DC)

@Composable
fun ReferralScreen_New(
    onNavigateToTree: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ReferralViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RSurface)
    ) {
        // ── White header ─────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth().background(RWhite)
                .padding(horizontal = 24.dp).padding(top = 12.dp, bottom = 14.dp)
        ) {
            Text("Refer & Earn", color = RInk2, fontSize = 21.sp, fontFamily = FontFamily.Serif)
            Text("Share your code. Earn coins at every level.", color = RMuted, fontSize = 12.sp)
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Gold referral code card ───────────────────────────────
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF2A1E08), Color(0xFF1A1206))))
                    ) {
                        // Subtle radial glow
                        Box(
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0x26C9973A), Color(0x00000000))
                                    )
                                )
                        )
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "YOUR REFERRAL CODE",
                                color = RGold3.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                uiState.referralCode.ifEmpty { "——" },
                                color = RGold3,
                                fontSize = 26.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                            Spacer(Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Copy button
                                Button(
                                    onClick = {
                                        val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        cm.setPrimaryClip(android.content.ClipData.newPlainText("code", uiState.referralCode))
                                        copied = true
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        contentColor = Color(0xFFE8C96A)
                                    )
                                ) {
                                    Text(if (copied) "Copied!" else "Copy Code", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                // Share button
                                Button(
                                    onClick = {
                                        val code = uiState.referralCode.ifEmpty { return@Button }
                                        val intent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT,
                                                "🎬 Join RS³ Films – India's most exclusive film fan club!\n\nDownload the app using my referral link and get exclusive access:\n\nhttp://117.198.99.60:8085/dl?ref=$code")
                                            type = "text/plain"
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share"))
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RGold,
                                        contentColor = RInk
                                    )
                                ) {
                                    Text("Share Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ── How You Earn rows ────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(RWhite).padding(18.dp)
                    ) {
                        Text("How You Earn", color = RInk2, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))
                        val levels = listOf(
                            Triple("L1", "Direct referral joins", "+100 coins"),
                            Triple("L2", "Their referral joins",    "+40 coins"),
                            Triple("L3", "Chain referral joins",    "+15 coins")
                        )
                        levels.forEachIndexed { i, (lvl, label, coins) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = if (i < 2) 12.dp else 0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF8A5C1A), RGold))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lvl, color = RInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(label, color = Color(0xFF3A3020), fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(coins, color = RGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ── Stats grid ───────────────────────────────────────────
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(
                            uiState.totalReferrals.toString() to "Total Refs",
                            uiState.coinsEarned.toString()    to "Coins Earned"
                        ).forEach { (v, l) ->
                            Column(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                    .background(RWhite).padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(v, color = RGold, fontSize = 22.sp, fontFamily = FontFamily.Serif)
                                Spacer(Modifier.height(2.dp))
                                Text(l, color = RMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }

                // ── View tree link ───────────────────────────────────────
                item {
                    TextButton(
                        onClick = onNavigateToTree,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.People, null, tint = RGold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("View My Referral Tree", color = RGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
