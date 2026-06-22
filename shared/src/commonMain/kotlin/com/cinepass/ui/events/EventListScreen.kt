package com.cinepass.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.ui.theme.FanAccent
import com.cinepass.ui.theme.FanCard
import com.cinepass.ui.theme.FanMuted

@Composable
fun EventListScreen(
    onEventClick: (String) -> Unit,
    viewModel: EventViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("All Events", style = MaterialTheme.typography.headlineMedium)
        when {
            state.isLoading -> Text("Loading events...", color = FanMuted)
            state.error != null -> Text(state.error ?: "Unable to load events", color = FanAccent)
            state.events.isEmpty() -> Text("No events available", color = FanMuted)
            else -> state.events.forEach { event ->
                FanCard(modifier = Modifier.fillMaxWidth()) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    Text("${event.city} • ${event.date}", style = MaterialTheme.typography.bodySmall, color = FanMuted)
                    Button(
                        onClick = { onEventClick(event.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = FanAccent),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Text("Open Event")
                    }
                }
            }
        }
    }
}

