package com.cinepass.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
actual fun LoopingVideoPlayer(url: String, modifier: Modifier) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Looping Video Player (iOS)", color = Color.White, fontSize = 12.sp)
    }
}

@Composable
actual fun YouTubeDialog(videoId: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text("YouTube Player: $videoId (iOS)", color = Color.White, fontSize = 13.sp)
        }
    }
}
