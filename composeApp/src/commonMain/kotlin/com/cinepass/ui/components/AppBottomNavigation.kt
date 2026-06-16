package com.cinepass.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined. Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cinepass.ui.theme.FanAccent
import com.cinepass.ui.theme.FanBorder
import com.cinepass.ui.theme.FanMuted

enum class BottomNavTab {
    HOME, REFERRAL, WALLET, PROFILE
}

@Composable
fun AppBottomNavigation(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(1.dp, FanBorder),
        colors = CardDefaults.cardColors(containerColor = Color(0xF6FFFFFF)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = currentTab == BottomNavTab.HOME,
                onClick = { onTabSelected(BottomNavTab.HOME) }
            )
            BottomNavItem(
                icon = Icons.Outlined.People,
                label = "Referral",
                selected = currentTab == BottomNavTab.REFERRAL,
                onClick = { onTabSelected(BottomNavTab.REFERRAL) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Wallet,
                label = "Wallet",
                selected = currentTab == BottomNavTab.WALLET,
                onClick = { onTabSelected(BottomNavTab.WALLET) }
            )
            BottomNavItem(
                icon = Icons.Outlined.AccountCircle,
                label = "Profile",
                selected = currentTab == BottomNavTab.PROFILE,
                onClick = { onTabSelected(BottomNavTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) FanAccent else Color(0xFF7F8DA0),
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color(0xFF1E2A38) else FanMuted,
        )
    }
}
