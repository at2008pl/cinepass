package com.cinepass.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.cinepass.data.api.models.Rs3FeedPost
import com.cinepass.utils.shouldPlayInlineVideo

sealed class FullscreenMedia {
    abstract val url: String
    abstract val title: String?

    data class Image(
        override val url: String,
        override val title: String?,
    ) : FullscreenMedia()

    data class Video(
        override val url: String,
        override val title: String?,
    ) : FullscreenMedia()
}

private fun extractYouTubeId(url: String): String? {
    val patterns = listOf(
        Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/watch\?.*v=([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/shorts/([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/embed/([A-Za-z0-9_-]{11})""")
    )
    for (p in patterns) {
        val m = p.find(url)
        if (m != null) return m.groupValues[1]
    }
    return null
}

private fun String.encodeSpaces() = replace(" ", "%20")

fun canOpenFeedMediaFullscreen(post: Rs3FeedPost): Boolean {
    val ytId = post.link?.let { extractYouTubeId(it) }
    return post.shouldPlayInlineVideo() || !post.mediaUrl.isNullOrBlank() || ytId != null
}

fun openFeedMediaFullscreen(
    post: Rs3FeedPost,
    onYouTubeClick: (String) -> Unit,
    onOpenFullscreen: (FullscreenMedia) -> Unit,
) {
    val ytId = post.link?.let { extractYouTubeId(it) }
    when {
        post.shouldPlayInlineVideo() -> {
            post.mediaUrl?.let { url -> onOpenFullscreen(FullscreenMedia.Video(url, post.title)) }
        }
        ytId != null -> onYouTubeClick(ytId)
        !post.mediaUrl.isNullOrBlank() -> {
            onOpenFullscreen(FullscreenMedia.Image(post.mediaUrl!!, post.title))
        }
    }
}

@Composable
fun FullscreenMediaDialog(media: FullscreenMedia, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            when (media) {
                is FullscreenMedia.Image -> {
                    AsyncImage(
                        model = media.url.encodeSpaces(),
                        contentDescription = media.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onDismiss() },
                        contentScale = ContentScale.Fit,
                    )
                }
                is FullscreenMedia.Video -> {
                    FullscreenVideoPlayer(
                        url = media.url,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            media.title?.takeIf { it.isNotBlank() }?.let { title ->
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                        .background(Color(0x88000000))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/** Renders feed image/video preview without handling taps. */
@Composable
fun FeedMediaContent(
    post: Rs3FeedPost,
    modifier: Modifier = Modifier,
) {
    val ytId = post.link?.let { extractYouTubeId(it) }
    Box(modifier = modifier) {
        when {
            post.shouldPlayInlineVideo() ->
                LoopingVideoPlayer(url = post.mediaUrl!!, modifier = Modifier.fillMaxSize())
            ytId != null ->
                YoutubeThumb(
                    ytId = ytId,
                    onClick = {},
                    modifier = Modifier.fillMaxSize(),
                    thumbUrl = post.thumbnailUrl,
                )
            !post.mediaUrl.isNullOrBlank() ->
                AsyncImage(
                    model = post.mediaUrl.encodeSpaces(),
                    contentDescription = post.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            else ->
                Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun FeedMediaBox(
    post: Rs3FeedPost,
    modifier: Modifier = Modifier,
    onYouTubeClick: (String) -> Unit,
    onOpenFullscreen: (FullscreenMedia) -> Unit,
) {
    Box(
        modifier = modifier.clickable(enabled = canOpenFeedMediaFullscreen(post)) {
            openFeedMediaFullscreen(post, onYouTubeClick, onOpenFullscreen)
        },
    ) {
        FeedMediaContent(post = post, modifier = Modifier.fillMaxSize())
    }
}
