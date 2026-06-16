package com.cinepass.ui.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.ui.theme.FanAccent
import com.cinepass.ui.theme.FanCard
import com.cinepass.ui.theme.FanGold
import com.cinepass.ui.theme.FanMuted

@Composable
fun TicketScreen(
    onTicketClick: (String) -> Unit,
    viewModel: TicketViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.bookedTicketId) {
        val booked = uiState.bookedTicketId
        if (!booked.isNullOrBlank()) {
            onTicketClick(booked)
            viewModel.clearBookedTicket()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FanCard(accentBorder = Color(0x44FF3C5F)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚡", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Priority Booking Window Open", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Text("Early access before public booking starts.", style = MaterialTheme.typography.bodySmall, color = FanMuted)
                }
                Text("LIVE", style = MaterialTheme.typography.labelLarge, color = FanGold)
            }
        }

        FanCard {
            Text(uiState.eventTitle.uppercase(), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold))
            Text(uiState.eventMeta.ifBlank { "Booking details loading..." }, style = MaterialTheme.typography.bodySmall, color = FanMuted)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Select Showtime", style = MaterialTheme.typography.labelLarge, color = FanMuted)
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.showtimes.isEmpty()) {
                Text("No showtimes available", color = FanMuted, style = MaterialTheme.typography.bodySmall)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    uiState.showtimes.forEach { show ->
                        val selected = show.id == uiState.selectedShowtimeId
                        Text(
                            text = "${show.label}\n₹${show.price}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(
                                    if (selected) FanAccent else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { viewModel.selectShowtime(show.id) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SeatMap()
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Use Coins for Discount", style = MaterialTheme.typography.titleSmall)
                    Text("You have ${uiState.availableCoins} coins", style = MaterialTheme.typography.bodySmall, color = FanMuted)
                }
                Switch(checked = uiState.useCoins, onCheckedChange = viewModel::setUseCoins)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("2 seats selected (B-12, B-13)", style = MaterialTheme.typography.bodySmall, color = FanMuted)
                    val selected = uiState.showtimes.firstOrNull { it.id == uiState.selectedShowtimeId }
                    Text(
                        "₹${(selected?.price ?: 0) * 2}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = FanGold,
                    )
                    Text(
                        if (uiState.useCoins) "Coins will be applied automatically"
                        else "No coin discount applied",
                        style = MaterialTheme.typography.bodySmall,
                        color = FanMuted,
                    )
                    if (!uiState.error.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(uiState.error ?: "", color = FanAccent, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(
                    onClick = { viewModel.bookNow() },
                    enabled = !uiState.isBooking && !uiState.isLoading && uiState.showtimes.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = FanAccent),
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text(if (uiState.isBooking) "Booking..." else "Confirm")
                }
            }
        }
    }
}

@Composable
private fun SeatMap() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("SCREEN", style = MaterialTheme.typography.labelLarge, color = FanMuted)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(100.dp)),
        )
        Spacer(modifier = Modifier.height(8.dp))
        repeat(4) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(('A' + row).toString(), style = MaterialTheme.typography.labelSmall, color = FanMuted)
                repeat(8) { col ->
                    val status = when {
                        row == 1 && (col == 1 || col == 2) -> 1
                        row == 0 && col <= 2 -> 2
                        (row + col) % 3 == 0 -> 3
                        else -> 0
                    }
                    val color = when (status) {
                        1 -> FanAccent
                        2 -> Color(0x22FFD166)
                        3 -> Color(0x22FFFFFF)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(color, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}

