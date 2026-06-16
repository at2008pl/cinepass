package com.cinepass.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.data.models.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onWalletClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CinePass") },
                actions = {
                    IconButton(onClick = onWalletClick) {
                        Icon(Icons.Default.AccountBalanceWallet, "Wallet")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, "Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coins Banner
            item {
                CoinsBanner(
                    coins = uiState.userCoins,
                    onWalletClick = onWalletClick
                )
            }

            // Featured Events (horizontal scroll)
            item {
                Text(
                    "Featured Events",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.featuredEvents) { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }

            // Upcoming Events (vertical list)
            item {
                Text(
                    "Upcoming Near You",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(uiState.upcomingEvents) { event ->
                EventListItem(
                    event = event,
                    onClick = { onEventClick(event.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun CoinsBanner(coins: Int, onWalletClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onWalletClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Your Coins", style = MaterialTheme.typography.labelMedium)
                Text(
                    "$coins 🪙",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(
                "Refer & Earn →",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(200.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Replace with AsyncImage (Coil) for real image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🎬") // Placeholder
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(event.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(event.date, style = MaterialTheme.typography.bodySmall)
            Text(event.city, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EventListItem(event: Event, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🎭", style = MaterialTheme.typography.headlineMedium)
            }
            Column {
                Text(event.title, style = MaterialTheme.typography.titleSmall)
                Text("${event.date} • ${event.venue}", style = MaterialTheme.typography.bodySmall)
                Text("₹${event.ticketPrice}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
