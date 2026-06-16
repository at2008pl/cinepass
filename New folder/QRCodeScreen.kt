package com.cinepass.ui.tickets

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinepass.utils.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScreen(
    ticketId: String,
    onBack: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel()
) {
    val ticket by viewModel.getTicket(ticketId).collectAsState(initial = null)
    val qrBitmap: Bitmap? = ticket?.let {
        QRCodeGenerator.generate(it.qrData, 512)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry Pass") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ticket?.let { t ->
                Text(t.eventTitle, style = MaterialTheme.typography.headlineSmall)
                Text(t.eventDate, style = MaterialTheme.typography.bodyMedium)
                Text(t.venue, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(16.dp))

                if (qrBitmap != null) {
                    Card(modifier = Modifier.size(280.dp)) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Entry QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                } else {
                    CircularProgressIndicator()
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Show this QR at the entrance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (t.isPriority) {
                    Badge { Text("Priority Pass 🌟") }
                }
            } ?: CircularProgressIndicator()
        }
    }
}
