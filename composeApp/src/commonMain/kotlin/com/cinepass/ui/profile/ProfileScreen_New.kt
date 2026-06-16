package com.cinepass.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.data.api.models.Rs3Profile
import com.cinepass.data.api.models.Rs3Redemption

private val PGold   = Color(0xFFC9973A)
private val PGold3  = Color(0xFFF5D78E)
private val PInk    = Color(0xFF0E0C08)
private val PInk2   = Color(0xFF1E1A10)
private val PSurface= Color(0xFFFDFAF3)
private val PWhite  = Color(0xFFFFFFFF)
private val PMuted  = Color(0xFF9A8A6A)
private val PFaint  = Color(0xFFEDE8DC)
private val PRed    = Color(0xFFC0392B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen_New(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToTree: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    // Dialog/sheet state
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var showRewardsSheet by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    val cmsContent = uiState.cmsContent

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontFamily = FontFamily.Serif, color = PInk2) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.updateMessage?.let {
                        Text(it, color = Color(0xFF1E7B4A), fontSize = 12.sp)
                    }
                    uiState.error?.let {
                        Text(it, color = PRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(editName.trim(), editEmail.trim())
                    showEditDialog = false
                }) { Text("Save", color = PGold, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = PMuted) }
            }
        )
    }

    // My Rewards Bottom Sheet
    if (showRewardsSheet) {
        ModalBottomSheet(onDismissRequest = { showRewardsSheet = false }) {
            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                Text("My Rewards", fontSize = 18.sp, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold, color = PInk2,
                    modifier = Modifier.padding(bottom = 16.dp))
                if (uiState.redemptions.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CardGiftcard, null, tint = PMuted, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No rewards claimed yet", color = PMuted, fontSize = 13.sp)
                        }
                    }
                } else {
                    uiState.redemptions.forEach { redemption ->
                        RedemptionRow(redemption)
                        Divider(color = PFaint, thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    // Achievements Dialog
    if (showAchievementsDialog) {
        AlertDialog(
            onDismissRequest = { showAchievementsDialog = false },
            title = { Text("Achievements", fontFamily = FontFamily.Serif, color = PInk2) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val milestones = listOf(
                        "🥇 First Referral" to ((profile?.referralsCount ?: 0) >= 1),
                        "🥈 5 Referrals" to ((profile?.referralsCount ?: 0) >= 5),
                        "🥇 10 Referrals" to ((profile?.referralsCount ?: 0) >= 10),
                        "💰 100 Coins Earned" to ((profile?.coins ?: 0) >= 100),
                        "💎 500 Coins Earned" to ((profile?.coins ?: 0) >= 500),
                        "🌟 Ambassador Status" to true
                    )
                    milestones.forEach { (label, unlocked) ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(label, fontSize = 13.sp,
                                color = if (unlocked) PInk2 else PMuted,
                                modifier = Modifier.weight(1f))
                            if (unlocked) Icon(Icons.Default.CheckCircle, null,
                                tint = PGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAchievementsDialog = false }) { Text("Close", color = PGold) }
            }
        )
    }

    // About RS³ Films Dialog
    if (showAboutDialog) {
        CmsDialog(
            title = "About RS³ Films",
            htmlContent = cmsContent["about_us"]
                ?: "RS\u00b3 Films is a premier entertainment company dedicated to bringing exceptional cinematic experiences to audiences across India.",
            onDismiss = { showAboutDialog = false }
        )
    }

    // FAQ Dialog
    if (showFaqDialog) {
        CmsDialog(
            title = "FAQ",
            htmlContent = cmsContent["faq"]
                ?: "<b>Q: How do I earn coins?</b><br>A: Refer friends using your unique referral code.",
            onDismiss = { showFaqDialog = false }
        )
    }

    // Terms & Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Terms & Privacy", fontFamily = FontFamily.Serif, color = PInk2) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Terms & Conditions", color = PInk2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    CmsHtmlText(
                        html = cmsContent["terms_and_conditions"]
                            ?: "By using this app, you agree to our terms of service."
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Privacy Policy", color = PInk2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    CmsHtmlText(
                        html = cmsContent["privacy_policy"]
                            ?: "We collect only the information necessary to provide our services."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close", color = PGold) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(PSurface)) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PGold)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
                // Dark header
                item { ProfileHeader(profile = profile, onLogout = onLogout) }

                // Stats grid
                item {
                    val vals = listOf(
                        (profile?.referralsCount?.toString() ?: "0") to "Referrals",
                        (profile?.coins?.toString() ?: "0")         to "Coins",
                        "Ambassador"                              to "Status"
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)
                            .fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(PFaint),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        vals.forEach { (v, l) ->
                            Column(
                                modifier = Modifier.weight(1f).background(PWhite).padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    v, color = PGold,
                                    fontSize = if (l == "Status") 12.sp else 19.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 20.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(l, color = PMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }

                // Menu items
                item {
                    val menuItems = listOf(
                        Triple(Icons.Default.Edit,       "Edit Profile",       "Update your details")       to "edit",
                        Triple(Icons.Default.People,     "Referral Tree",      "See your full network")     to "tree",
                        Triple(Icons.Default.EmojiEvents,"Achievements",       "Your milestones")           to "achievements",
                        Triple(Icons.Default.CardGiftcard,"My Rewards",        "Claimed offers & codes")    to "rewards",
                        Triple(Icons.Default.Info,       "About RS\u00b3 Films",    "Our story")             to "about",
                        Triple(Icons.Default.HelpOutline,"FAQ",                 "Common questions")          to "faq",
                        Triple(Icons.Default.Shield,     "Terms & Privacy",    "Legal information")         to "privacy",
                        Triple(Icons.Default.Settings,   "Settings",           "Notifications, account")    to "settings",
                        Triple(Icons.Default.Logout,     "Sign Out",           "")                          to "logout"
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) {
                        menuItems.forEachIndexed { i, (item, action) ->
                            val (icon, label, sub) = item
                            val isDanger = action == "logout"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (action) {
                                            "tree"         -> onNavigateToTree()
                                            "logout"       -> onLogout()
                                            "settings"     -> onNavigateToSettings()
                                            "edit"         -> {
                                                editName = profile?.name ?: ""
                                                editEmail = profile?.email ?: ""
                                                showEditDialog = true
                                            }
                                            "rewards"      -> {
                                                viewModel.loadRedemptions()
                                                showRewardsSheet = true
                                            }
                                            "achievements" -> showAchievementsDialog = true
                                            "about"        -> showAboutDialog = true
                                            "faq"          -> showFaqDialog = true
                                            "privacy"      -> showPrivacyDialog = true
                                        }
                                    }
                                    .padding(vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (isDanger) PRed.copy(alpha = 0.08f) else PGold.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, null, tint = if (isDanger) PRed else PGold, modifier = Modifier.size(18.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, color = if (isDanger) PRed else PInk2, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    if (sub.isNotEmpty()) {
                                        Text(sub, color = PMuted, fontSize = 11.sp)
                                    }
                                }
                                if (!isDanger) {
                                    Icon(Icons.Default.ChevronRight, null, tint = PMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (i < menuItems.lastIndex) {
                                Divider(thickness = 0.5.dp, color = PFaint)
                            }
                        }
                    }
                }

                item {
                    Text(
                        "RS³ Films · v1.2.0 · Member since 2025",
                        color = PMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── CMS Helper Composables ──────────────────────────────────────────────────

/** Renders an HTML string as plain-ish text via Android's Html.fromHtml */
@Composable
private fun CmsHtmlText(html: String, modifier: Modifier = Modifier) {
    val spanned = remember(html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(html)
        }
    }
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            android.widget.TextView(ctx).apply {
                setTextColor(android.graphics.Color.parseColor("#FF9A8A6A"))
                textSize = 12f
                setLineSpacing(4f, 1f)
            }
        },
        update = { view -> view.text = spanned },
        modifier = modifier.fillMaxWidth()
    )
}

/** Full-screen scrollable dialog for CMS rich-text sections */
@Composable
private fun CmsDialog(title: String, htmlContent: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = FontFamily.Serif, color = PInk2) },
        text = {
            val scroll = androidx.compose.foundation.rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scroll)) {
                CmsHtmlText(html = htmlContent)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = PGold) }
        }
    )
}

@Composable
private fun ProfileHeader(profile: Rs3Profile?, onLogout: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFF0E0A06), Color(0xFF1E1408)),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(400f, 300f)))
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            .padding(bottom = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF2E2010), Color(0xFF5E4A20))))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF2E2010), Color(0xFF5E4A20)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "F",
                                color = Color(0xFFF5D78E),
                                fontSize = 26.sp,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                    // Verified badge
                    Box(
                        modifier = Modifier.size(18.dp).align(Alignment.BottomEnd)
                            .clip(CircleShape).background(PGold).padding(3.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = PInk, modifier = Modifier.fillMaxSize())
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        profile?.name ?: "Fan Member",
                        color = Color(0xFFFBF0D8),
                        fontSize = 19.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        listOfNotNull(profile?.email, "Mumbai").joinToString(" · "),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(7.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF8A5C1A), PGold)))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, null, tint = PInk, modifier = Modifier.size(11.dp))
                            Text("FAN AMBASSADOR", color = PInk, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Referral code row
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("YOUR CODE", color = PGold3.copy(alpha = 0.4f), fontSize = 9.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        profile?.referralCode ?: "——",
                        color = PGold3,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(PGold.copy(alpha = 0.2f))
                        .clickable {
                            val code = profile?.referralCode ?: return@clickable
                            val intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT,
                                    "🎬 Join RS³ Films – India's most exclusive film fan club!\n\nDownload the app using my referral link and get exclusive access:\n\nhttp://192.168.29.211:4001/dl?ref=$code")
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share"))
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Share", color = Color(0xFFE8C96A), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun RedemptionRow(redemption: Rs3Redemption) {
    val statusColor = when (redemption.status) {
        "fulfilled" -> Color(0xFF1E7B4A)
        "claimed"   -> PGold
        else        -> PMuted
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                .background(PGold.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CardGiftcard, null, tint = PGold, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(redemption.offerTitle ?: "Offer", color = PInk2, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            if (!redemption.rewardValue.isNullOrBlank()) {
                Text("Code: ${redemption.rewardValue}", color = PMuted, fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace)
            }
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(statusColor.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(redemption.status.replaceFirstChar { it.uppercase() },
                color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}