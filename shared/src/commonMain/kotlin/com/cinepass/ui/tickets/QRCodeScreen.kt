package com.cinepass.ui.tickets

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.cinepass.utils.QRCodeGenerator

@Composable
fun QRCodeScreen(
    ticketId: String,
    onBack: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel(),
) {
    val ticket by viewModel.getTicket(ticketId).collectAsState(initial = null)
    val qrBitmap: ImageBitmap? = ticket?.let { QRCodeGenerator.generate(it.qrData, 512) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.weight(1f))
            Text("YOUR TICKET", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
        }

        ticket?.let { t ->
            FanCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x11FF3C5F), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                ) {
                    Text(t.eventTitle.uppercase(), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Text("${t.eventDate} • ${t.venue}", style = MaterialTheme.typography.bodySmall, color = FanMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "PRIORITY PASS",
                        style = MaterialTheme.typography.labelLarge,
                        color = FanAccent,
                        modifier = Modifier
                            .background(Color(0x22FF3C5F), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,

                            contentDescription = "Ticket QR",
                            modifier = Modifier
                                .size(180.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                TicketInfo("Movie", t.eventTitle)
                TicketInfo("Date", t.eventDate)
                TicketInfo("Seat", t.seatInfo)
                TicketInfo("Venue", t.venue)
                TicketInfo("Pass Type", if (t.isPriority) "Priority" else "General")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (t.isPriority) "Priority booking benefits applied" else "Standard booking pass",
                    style = MaterialTheme.typography.bodySmall,
                    color = FanGold,
                    modifier = Modifier
                        .background(Color(0x22FFD166), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                )
            }

            FanCard {
                Text("After Event Rewards", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Attend the event → +150 attendance bonus", color = FanGold, style = MaterialTheme.typography.bodySmall)
                Text("Post a story tagging FanVerse → +50", color = FanGold, style = MaterialTheme.typography.bodySmall)
                Text("Rate the event → +25", color = FanGold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TicketInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = FanMuted)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}
