package com.cinepass.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
expect fun LoopingVideoPlayer(url: String, modifier: Modifier = Modifier)

@Composable
expect fun YouTubeDialog(videoId: String, onDismiss: () -> Unit)

@Composable
fun YoutubeThumb(
    ytId: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbUrl: String? = null
) {
    val imageUrl = when {
        !thumbUrl.isNullOrBlank() -> thumbUrl
        !ytId.isNullOrBlank() -> "https://img.youtube.com/vi/$ytId/hqdefault.jpg"
        else -> ""
    }
    
    Box(
        modifier = modifier.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "YouTube Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Play button overlay
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play Video",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
