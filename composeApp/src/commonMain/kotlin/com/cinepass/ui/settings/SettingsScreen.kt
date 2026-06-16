package com.cinepass.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SGold    = Color(0xFFC9973A)
private val SInk2    = Color(0xFF1E1A10)
private val SSurface = Color(0xFFFDFAF3)
private val SFaint   = Color(0xFFEDE8DC)
private val SMuted   = Color(0xFF9A8A6A)
private val SWhite   = Color(0xFFFFFFFF)

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    var notifReferral by remember { mutableStateOf(true) }
    var notifCoins    by remember { mutableStateOf(true) }
    var notifOffers   by remember { mutableStateOf(true) }
    var notifUpdates  by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(SSurface)) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(
                    listOf(Color(0xFF0E0A06), Color(0xFF1E1408)),
                    start = Offset(0f, 0f),
                    end   = Offset(400f, 200f)
                ))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = SGold, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Settings", color = SWhite, fontSize = 20.sp, fontFamily = FontFamily.Serif)
                    Text("Preferences & account", color = SWhite.copy(alpha = 0.4f), fontSize = 11.sp)
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                SettingsSectionLabel("Notifications")
                SettingsToggleRow(Icons.Default.Notifications, "Referral Alerts",
                    "When someone uses your code", notifReferral) { notifReferral = it }
                SettingsToggleRow(Icons.Default.Stars, "Coin Updates",
                    "When you earn or spend coins", notifCoins) { notifCoins = it }
                SettingsToggleRow(Icons.Default.LocalOffer, "Offers & Deals",
                    "New exclusive offers", notifOffers) { notifOffers = it }
                SettingsToggleRow(Icons.Default.Campaign, "App Updates",
                    "News and announcements", notifUpdates) { notifUpdates = it }
            }
            item {
                SettingsSectionLabel("Account")
                SettingsLinkRow(Icons.Default.Phone,  "Phone Number",  "Update your mobile")
                SettingsLinkRow(Icons.Default.Email,  "Email Address", "Update email")
                SettingsLinkRow(Icons.Default.Lock,   "Password",      "Change password")
            }
            item {
                SettingsSectionLabel("Legal")
                SettingsLinkRow(Icons.Default.Description, "Privacy Policy",  "How we handle your data")
                SettingsLinkRow(Icons.Default.Gavel,       "Terms of Service","Usage terms")
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        title, color = SMuted,
        fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 22.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector, label: String, sub: String,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp)).background(SWhite)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(9.dp))
                .background(SGold.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = SGold, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = SInk2, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(sub, color = SMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SWhite,
                checkedTrackColor = SGold,
                uncheckedTrackColor = SFaint,
                uncheckedThumbColor = SMuted
            )
        )
    }
}

@Composable
private fun SettingsLinkRow(icon: ImageVector, label: String, sub: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp)).background(SWhite)
            .clickable { }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(9.dp))
                .background(SGold.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = SGold, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = SInk2, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(sub, color = SMuted, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = SMuted, modifier = Modifier.size(16.dp))
    }
}
