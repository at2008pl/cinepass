package com.cinepass.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.data.api.models.Rs3CoinTransaction
import com.cinepass.data.api.models.Rs3Offer
import com.cinepass.ui.components.ScreenTopBar

private val WGold   = Color(0xFFC9973A)
private val WGold3  = Color(0xFFF5D78E)
private val WInk    = Color(0xFF0E0C08)
private val WInk2   = Color(0xFF1E1A10)
private val WSurface= Color(0xFFFDFAF3)
private val WWhite  = Color(0xFFFFFFFF)
private val WMuted  = Color(0xFF9A8A6A)
private val WFaint  = Color(0xFFEDE8DC)
private val WGreen  = Color(0xFF1E7B4A)
private val WRed    = Color(0xFFC0392B)

@Composable
fun WalletScreen_New(
    onBack: () -> Unit = {},
    onReferralClick: () -> Unit = {},
    onOfferClick: (Int) -> Unit = {},
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf("history") }

    Column(modifier = Modifier.fillMaxSize().background(WSurface)) {
        // White header
        ScreenTopBar(backgroundColor = WWhite) {
            Text("Wallet", color = WInk2, fontSize = 21.sp, fontFamily = FontFamily.Serif)
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Balance card
                item {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 4.dp)
                            .fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF2A1E08), Color(0xFF1A1206))))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(170.dp)
                                .background(Brush.radialGradient(listOf(Color(0x33C9973A), Color(0x00000000))))
                        )
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("COIN BALANCE", color = WGold3.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    uiState.coinBalance.toString(),
                                    color = WGold3,
                                    fontSize = 46.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Light,
                                    lineHeight = 50.sp
                                )
                                Text("coins", color = WGold3.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.Light,
                                    modifier = Modifier.padding(bottom = 8.dp))
                            }
                            Text(
                                "Min ${uiState.minRedeemCoins} coins to redeem",
                                color = WGold3.copy(alpha = 0.4f),
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                listOf(uiState.coinsEarned.toString() to "Earned All Time",
                                       uiState.coinsSpent.toString() to "Spent").forEach { (v, l) ->
                                    Column {
                                        Text(v, color = Color(0xFFC9973A), fontSize = 17.sp, fontFamily = FontFamily.Serif)
                                        Spacer(Modifier.height(2.dp))
                                        Text(l, color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, letterSpacing = 0.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Tabs
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp)
                            .fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(WFaint)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("history" to "History", "offers" to "Redeem").forEach { (id, label) ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                    .background(if (activeTab == id) WWhite else Color.Transparent)
                                    .clickable { activeTab = id }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (activeTab == id) WInk2 else WMuted,
                                    fontSize = 13.sp,
                                    fontWeight = if (activeTab == id) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                if (activeTab == "history") {
                    if (uiState.transactions.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No transactions yet", color = WMuted, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(uiState.transactions) { txn ->
                            TxnRow(txn)
                        }
                    }
                } else {
                    // ── Offers / Redeem tab ──────────────────────────────
                    if (uiState.offers.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CardGiftcard, null, tint = WMuted, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(12.dp))
                                    Text("No offers available right now", color = WMuted, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(uiState.offers) { offer ->
                            OfferCard(
                                offer = offer,
                                userCoins = uiState.coinBalance,
                                onClick = { onOfferClick(offer.id) }
                            )
                        }
                    }
                    item {
                        // Earn more coins CTA
                        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                    .background(WWhite).padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Refer Friends", color = WInk2, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text("Earn coins with every referral", color = WMuted, fontSize = 12.sp)
                                    }
                                    Text("🔗", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = onReferralClick,
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WGold, contentColor = WInk)
                                ) {
                                    Text("Go to Referrals", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Note: Using standard clickable for tab switching

@Composable
private fun OfferCard(offer: Rs3Offer, userCoins: Int, onClick: () -> Unit) {
    val canAfford = !offer.claimed && userCoins >= (offer.coinCost ?: 0)
    val statusColor = when {
        offer.claimed -> WMuted
        canAfford -> WGreen
        else -> WRed
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WWhite)
            .clickable(enabled = !offer.claimed) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp))
                .background(WGold.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CardGiftcard, null, tint = WGold, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(offer.title, color = WInk2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            if ((offer.coinCost ?: 0) > 0) {
                Text("🪙 ${offer.coinCost} coins", color = WMuted, fontSize = 11.sp)
            } else if (offer.targetReferrals != null) {
                Text("👥 ${offer.targetReferrals} referrals needed", color = WMuted, fontSize = 11.sp)
            }
            if (offer.validUntil != null) {
                Text("Valid until ${offer.validUntil}", color = WMuted, fontSize = 10.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (offer.claimed) "Claimed" else if (canAfford) "Redeem" else "Locked",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TxnRow(txn: Rs3CoinTransaction) {
    val isEarn = txn.coins > 0
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(if (isEarn) Color(0xFF1E7B4A).copy(alpha = 0.1f) else Color(0xFFC0392B).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isEarn) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                null,
                tint = if (isEarn) Color(0xFF1E7B4A) else Color(0xFFC0392B),
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(txn.note ?: "Transaction", color = Color(0xFF1E1A10), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(txn.createdAt ?: "", color = Color(0xFF9A8A6A), fontSize = 11.sp)
        }
        Text(
            text = "${if (isEarn) "+" else ""}${txn.coins}",
            color = if (isEarn) Color(0xFF1E7B4A) else Color(0xFFC0392B),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Divider(
        modifier = Modifier,
        thickness = 0.5.dp,
        color = Color(0xFFEDE8DC)
    )
}