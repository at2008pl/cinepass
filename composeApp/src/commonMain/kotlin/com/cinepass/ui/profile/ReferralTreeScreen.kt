package com.cinepass.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val T_Gold    = Color(0xFFC9973A)
private val T_GoldL   = Color(0xFFF5D78E)
private val T_Muted   = Color(0xFFA89880)
private val T_Text    = Color(0xFF1C1408)
private val T_Bg      = Color(0xFFFDFAF3)
private val T_Card    = Color(0xFFFFFFFF)
private val T_Faint   = Color(0xFFEDE8DC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralTreeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReferralTreeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Network", color = T_Text, fontFamily = FontFamily.Serif, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = T_Text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = T_Card,
                    navigationIconContentColor = T_Text,
                    titleContentColor = T_Text
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = T_Gold)
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Failed to load tree",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        uiState.error ?: "",
                        color = T_Muted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            uiState.rootNode != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(T_Bg)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Stats row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Members", "${uiState.totalNodes - 1}", Modifier.weight(1f))
                        StatCard("Coins Earned", "${uiState.totalEarnings}", Modifier.weight(1f))
                        StatCard("Depth", "L${uiState.maxLevel}", Modifier.weight(1f))
                    }

                    // Level badges
                    if (uiState.levelStats.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(T_Card)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.levelStats.entries.sortedBy { it.key }.forEach { (level, count) ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape)
                                            .background(T_Gold.copy(alpha = if (level == 1) 1f else 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("L$level", color = if (level == 1) Color.White else T_Gold,
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("$count", color = T_Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("members", color = T_Muted, fontSize = 9.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Visual tree
                    Text("Network Tree", modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = T_Muted,
                        letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Root node (YOU)
                        RootNodeBubble(name = uiState.rootNode!!.name)

                        if (uiState.rootNode!!.children.isNotEmpty()) {
                            ConnectorLine(height = 20.dp)
                            TreeChildrenRow(
                                nodes = uiState.rootNode!!.children,
                                depth = 1
                            )
                        } else {
                            Spacer(Modifier.height(16.dp))
                            Text("No referrals yet", color = T_Muted, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available", color = T_Muted)
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(T_Card)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = T_Gold,
            fontFamily = FontFamily.Serif)
        Spacer(Modifier.height(2.dp))
        Text(title, fontSize = 10.sp, color = T_Muted, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RootNodeBubble(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape)
                .background(T_Gold),
            contentAlignment = Alignment.Center
        ) {
            Text(name.firstOrNull()?.uppercaseChar()?.toString() ?: "Y",
                color = Color.White, fontSize = 26.sp, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(name, color = T_Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(T_Gold.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text("YOU", color = T_Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ConnectorLine(height: androidx.compose.ui.unit.Dp = 24.dp) {
    Canvas(modifier = Modifier.width(2.dp).height(height)) {
        drawLine(
            color = T_Gold.copy(alpha = 0.4f),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
        )
    }
}

@Composable
private fun TreeChildrenRow(nodes: List<TreeNode>, depth: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal branch line if >1 child
        if (nodes.size > 1) {
            val lineColor = T_Gold.copy(alpha = 0.3f)
            Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                drawLine(color = lineColor,
                    start = Offset(size.width * 0.1f, size.height / 2),
                    end = Offset(size.width * 0.9f, size.height / 2),
                    strokeWidth = 2f)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            nodes.forEach { node ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ConnectorLine(16.dp)
                    MemberNodeCard(node = node, depth = depth)
                    if (node.children.isNotEmpty()) {
                        ConnectorLine(16.dp)
                        TreeChildrenRow(nodes = node.children, depth = depth + 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberNodeCard(node: TreeNode, depth: Int) {
    val bgColor = when (depth) {
        1 -> Color(0xFFEAD7A8)
        2 -> Color(0xFFF5EDE0)
        else -> T_Faint
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(T_Gold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(node.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = T_Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            node.name.split(" ").firstOrNull() ?: node.name,
            color = T_Text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        if (node.coinsAwarded > 0) {
            Text("🪙 ${node.coinsAwarded}", color = T_Gold, fontSize = 9.sp)
        }
        if (node.children.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text("${node.children.size} ref", color = T_Muted, fontSize = 8.sp)
        }
    }
}
