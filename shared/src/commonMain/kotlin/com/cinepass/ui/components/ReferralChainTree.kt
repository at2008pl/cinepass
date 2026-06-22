package com.cinepass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinepass.data.models.ReferralTreeNode
import com.cinepass.ui.theme.FanAccent
import com.cinepass.ui.theme.FanCyan
import com.cinepass.ui.theme.FanGold
import com.cinepass.ui.theme.FanMuted

@Composable
fun ReferralChainTree(
    referrer: ReferralTreeNode?,
    children: List<ReferralTreeNode>,
    totalDescendants: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Referral Chain",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = "$totalDescendants Total",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Referrer (one above) - if exists
        if (referrer != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.ArrowUpward,
                    contentDescription = "Referred by",
                    tint = FanMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                TreeNodeCard(
                    node = referrer,
                    level = -1,
                    showChildren = false,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    Icons.Outlined.ArrowDownward,
                    contentDescription = "You",
                    tint = FanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Current user indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF9E5)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(FanGold, Color(0xFFFFD700)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "You",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "YOU",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FanGold
                )
            }
        }

        // Direct referrals (level 1)
        if (children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                Icons.Outlined.ArrowDownward,
                contentDescription = "Your referrals",
                tint = FanCyan,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))

            children.forEach { child ->
                TreeNodeCard(
                    node = child,
                    level = 1,
                    showChildren = true,
                )
            }
        } else {
            // Empty state
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No referrals yet. Share your link to start building your chain!",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FanMuted,
                )
            }
        }
    }
}

@Composable
private fun TreeNodeCard(
    node: ReferralTreeNode,
    level: Int,  // -1 = referrer, 1 = level  1, 2 = level 2, 3 = level 3
    showChildren: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (level) {
        -1 -> Color(0xFFF3E5F5)  // Purple tint for referrer
        1 -> Color(0xFFE3F2FD)   // Blue for level 1
        2 -> Color(0xFFE8F5E9)   // Green for level 2
        else -> Color(0xFFFFF3E0) // Orange for level 3
    }

    val accentColor = when (level) {
        -1 -> Color(0xFF9C27B0)
        1 -> FanCyan
        2 -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = node.name.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                        Column {
                            Text(
                                text = node.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = node.referralCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = FanMuted,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${node.coins} coins",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FanGold,
                        )
                        if (level > 0) {
                            Text(
                                text = "Level $level",
                                style = MaterialTheme.typography.labelSmall,
                                color = FanMuted,
                            )
                        }
                    }
                }
            }
        }

        // Show children recursively (level 2 and 3)
        if (showChildren && node.children.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                node.children.forEach { child ->
                    TreeNodeCard(
                        node = child,
                        level = level + 1,
                        showChildren = level < 2,  // Only show up to level 3
                    )
                }
            }
        }
    }
}
