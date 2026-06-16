package com.cinepass.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.ui.theme.FanAccent
import com.cinepass.ui.theme.FanCard
import com.cinepass.ui.theme.FanGold
import com.cinepass.ui.theme.FanMuted

@Composable
fun EventDetailScreen(
    eventId: String,
    onTicketBooked: () -> Unit,
    onBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    LaunchedEffect(state.registrationDone) {
        if (state.registrationDone) onTicketBooked()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Back") }

        val event = state.selectedEvent
        FanCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFFE8EF), Color(0xFFFFF2F8), Color(0xFFFFDCE6)),
                        ),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .padding(16.dp),
            ) {
                Text("EVENT DETAIL", style = MaterialTheme.typography.labelLarge)
                Text(
                    event?.title ?: "Loading...",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
                Text("${event?.city ?: ""} • ${event?.date ?: ""}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                event?.description ?: "Loading event details...",
                style = MaterialTheme.typography.bodyMedium,
                color = FanMuted,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Text("Coin reward ", color = FanMuted)
                Text("+${event?.coinsPerReferral ?: 0}", color = FanGold, style = MaterialTheme.typography.titleSmall)
            }
            if (!state.error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(state.error ?: "", color = FanAccent, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Button(
            //     onClick = { viewModel.registerForEvent(eventId) },
            //     enabled = !state.isLoading && event != null,
            //     colors = ButtonDefaults.buttonColors(containerColor = FanAccent),
            // ) {
            //     Text(if (state.isLoading) "Please wait..." else "Book Ticket")
            // }
        }
    }
}

