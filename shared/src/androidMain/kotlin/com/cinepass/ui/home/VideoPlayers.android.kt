package com.cinepass.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

private fun String.encodeSpaces() = replace(" ", "%20")

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
actual fun LoopingVideoPlayer(url: String, modifier: Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            val item = MediaItem.fromUri(Uri.parse(url.encodeSpaces()))
            setMediaItem(item)
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(exoPlayer) { onDispose { exoPlayer.release() } }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}

@Composable
actual fun YouTubeDialog(videoId: String, onDismiss: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var playerError by remember { mutableStateOf<String?>(null) }
    var ytPlayerView by remember { mutableStateOf<YouTubePlayerView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            ytPlayerView?.release()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            Column {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1008))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                        Text("YouTube", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                if (playerError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color(0xFF1A1008)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(36.dp))
                            Text(
                                text = playerError ?: "Playback error",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Icon(Icons.Default.OpenInNew, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open in YouTube", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            YouTubePlayerView(ctx).also { view ->
                                ytPlayerView = view
                                lifecycleOwner.lifecycle.addObserver(view)
                                view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        youTubePlayer.loadVideo(videoId, 0f)
                                    }
                                    override fun onError(
                                        youTubePlayer: YouTubePlayer,
                                        error: PlayerConstants.PlayerError
                                    ) {
                                        playerError = when (error.name) {
                                            "VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYERS" ->
                                                "This video can't be played here (embedding disabled by owner)"
                                            "VIDEO_NOT_FOUND" ->
                                                "Video not found"
                                            "INVALID_PARAMETER_IN_REQUEST" ->
                                                "Invalid video"
                                            else -> "Playback error (${error.name})"
                                        }
                                    }
                                })
                            }
                        },
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    )
                }
            }
        }
    }
}
