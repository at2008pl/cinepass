package com.cinepass.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cinepass.utils.resolveMediaUrl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.awaitCancellation
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LoopingVideoPlayer(url: String, modifier: Modifier) {
    val resolvedUrl = remember(url) { resolveMediaUrl(url) ?: url }
    val player = remember { AVPlayer() }
    val playerLayer = remember { AVPlayerLayer.playerLayerWithPlayer(player) }

    LaunchedEffect(resolvedUrl) {
        val nsUrl = NSURL(string = resolvedUrl) ?: return@LaunchedEffect
        val asset = AVURLAsset(nsUrl, null)
        val playerItem = AVPlayerItem(asset = asset)
        player.replaceCurrentItemWithPlayerItem(item = playerItem)
        player.muted = true
        player.play()

        val token = NSNotificationCenter.defaultCenter.addObserverForName(
            AVPlayerItemDidPlayToEndTimeNotification,
            playerItem,
            null
        ) { _ ->
            player.seekToTime(CMTimeMake(value = 0, timescale = 1))
            player.play()
        }
        try {
            awaitCancellation()
        } finally {
            NSNotificationCenter.defaultCenter.removeObserver(token)
            player.replaceCurrentItemWithPlayerItem(null)
        }
    }

    val factory = remember(playerLayer) {
        {
            playerLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            val view = UIView()
            view.layer.addSublayer(playerLayer)
            view
        }
    }

    UIKitView(
        modifier = modifier,
        background = Color.Black,
        factory = factory,
        onResize = { _, rect ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            playerLayer.setFrame(rect)
            CATransaction.commit()
        }
    )
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
