package com.cinepass.ui.offers

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.data.api.models.Rs3Offer
import kotlinx.coroutines.delay

/* ═══════════════════════════════════════════════════════════════════════════
   OfferDetailScreen — Offer Redemption Flow
   Shows: Offer details, eligibility check, redemption dialog, confirmation
═══════════════════════════════════════════════════════════════════════════ */

private val T_Bg = Color(0xFFFAF7F2)
private val T_Gold = Color(0xFFA67C2E)
private val T_Ink = Color(0xFF1C1408)
private val T_Muted = Color(0xFFA89880)
private val T_Card = Color(0xFFFFFDF9)
private val T_Green = Color(0xFF2E6B45)
private val T_Red = Color(0xFF8B2E2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offerId: Int,
    onBack: () -> Unit = {},
    viewModel: OfferDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRedeemConfirm by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Load offer on initial composition
    LaunchedEffect(offerId) {
        viewModel.loadOffer(offerId)
    }

    // Auto-show success dialog when offer is redeemed
    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            showSuccessDialog = true
            delay(3000)
            viewModel.clearSuccess()
            showSuccessDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Offer Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = T_Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = T_Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = T_Bg,
                    scrolledContainerColor = T_Bg
                )
            )
        },
        containerColor = T_Bg
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = T_Gold)
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    "Error",
                    modifier = Modifier.size(48.dp),
                    tint = T_Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    uiState.error ?: "An error occurred",
                    textAlign = TextAlign.Center,
                    color = T_Ink,
                    fontSize = 14.sp
                )
            }
        } else if (uiState.offer != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Offer Header
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = T_Card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Offer Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(T_Gold.copy(alpha = 0.1f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "LIMITED OFFER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = T_Gold,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Title
                            Text(
                                uiState.offer!!.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = T_Ink
                            )

                            // Description
                            Text(
                                uiState.offer!!.description ?: "",
                                fontSize = 13.sp,
                                color = T_Muted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Eligibility Check
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.canRedeem) T_Green.copy(alpha = 0.1f) else T_Red.copy(alpha = 0.1f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.canRedeem) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = if (uiState.canRedeem) "Eligible" else "Not Eligible",
                                modifier = Modifier.size(24.dp),
                                tint = if (uiState.canRedeem) T_Green else T_Red
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (uiState.canRedeem) "You're Eligible" else "Insufficient Coins",
                                    fontWeight = FontWeight.Bold,
                                    color = T_Ink,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "You have ${uiState.userCoins} coins. This offer costs ${uiState.offer!!.coinCost ?: 0} coins.",
                                    fontSize = 12.sp,
                                    color = T_Muted
                                )
                            }
                        }
                    }
                }

                // Offer Details
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Offer Details",
                            fontWeight = FontWeight.Bold,
                            color = T_Ink,
                            fontSize = 14.sp
                        )

                        DetailRow(
                            label = "Coin Cost",
                            value = "${uiState.offer!!.coinCost ?: 0} coins",
                            icon = Icons.Default.MonetizationOn
                        )

                        DetailRow(
                            label = "Validity",
                            value = uiState.offer!!.validUntil ?: "No expiry",
                            icon = Icons.Default.DateRange
                        )

                        DetailRow(
                            label = "Reward Type",
                            value = uiState.offer!!.rewardValue ?: "Unknown",
                            icon = Icons.Default.CardGiftcard
                        )

                        DetailRow(
                            label = "Claims Available",
                            value = "${uiState.offer!!.claimsCount ?: 0}/${uiState.offer!!.maxClaims ?: "Unlimited"}",
                            icon = Icons.Default.Groups
                        )
                    }
                }

                // Terms & Conditions
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = T_Card),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Terms & Conditions",
                                fontWeight = FontWeight.Bold,
                                color = T_Ink,
                                fontSize = 13.sp
                            )
                            BulletPoint("Valid for 30 days from claim date")
                            BulletPoint("Cannot be combined with other offers")
                            BulletPoint("One-time use redemption only")
                            BulletPoint("Reward sent to registered email within 24 hours")
                        }
                    }
                }

                // Redeem Button
                item {
                    Button(
                        onClick = {
                            if (uiState.canRedeem && !uiState.offer!!.claimed) {
                                showRedeemConfirm = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = uiState.canRedeem && !uiState.offer!!.claimed && !uiState.isRedeeming,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.offer!!.claimed) T_Muted else T_Gold,
                            disabledContainerColor = T_Muted.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isRedeeming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.CardGiftcard,
                                "Claim",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                tint = Color.White
                            )
                            Text(
                                if (uiState.offer!!.claimed) "Already Claimed" else "Claim Now",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Redemption Confirmation Dialog
    if (showRedeemConfirm) {
        AlertDialog(
            onDismissRequest = { showRedeemConfirm = false },
            title = {
                Text("Confirm Redemption", color = T_Ink, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "You're about to redeem this offer for ${uiState.offer?.coinCost ?: 0} coins.",
                        color = T_Ink,
                        fontSize = 14.sp
                    )
                    Text(
                        "You will receive: ${uiState.offer?.rewardValue ?: "Your reward"}",
                        color = T_Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "This action cannot be undone.",
                        color = T_Muted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.redeemOffer(uiState.offer?.id ?: return@Button)
                        showRedeemConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = T_Gold)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRedeemConfirm = false }) {
                    Text("Cancel", color = T_Gold)
                }
            },
            containerColor = T_Card,
            textContentColor = T_Ink
        )
    }

    // Success Dialog
    if (showSuccessDialog && uiState.success != null) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text("Success!", color = T_Green, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(uiState.success!!, color = T_Ink)
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = T_Green)
                ) {
                    Text("Great!", color = Color.White)
                }
            },
            containerColor = T_Card,
            textContentColor = T_Ink
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(T_Bg)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = T_Gold
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 11.sp,
                color = T_Muted,
                fontWeight = FontWeight.Light
            )
            Text(
                value,
                fontSize = 13.sp,
                color = T_Ink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("•", color = T_Gold, fontWeight = FontWeight.Bold)
        Text(
            text,
            fontSize = 12.sp,
            color = T_Muted,
            modifier = Modifier.weight(1f)
        )
    }
}
