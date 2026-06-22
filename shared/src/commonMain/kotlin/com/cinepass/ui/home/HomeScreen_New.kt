package com.cinepass.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.cinepass.data.api.models.Rs3FeedPost
import com.cinepass.data.api.models.Rs3Offer
import com.cinepass.data.prefs.UserPrefs

// ━━ RS³ Design Tokens ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private val HGold     = Color(0xFFC9973A)
private val HGoldPale = Color(0xFFF5D78E)
private val HInk      = Color(0xFF0E0C08)
private val HInk2     = Color(0xFF1E1A10)
private val HSurface  = Color(0xFFFDFAF3)
private val HWhite    = Color(0xFFFFFFFF)
private val HMuted    = Color(0xFF9A8A6A)
private val HFaint    = Color(0xFFEDE8DC)

// ━━ Helpers ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private fun extractYouTubeId(url: String): String? {
    val patterns = listOf(
        Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/watch\?.*v=([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/shorts/([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/embed/([A-Za-z0-9_-]{11})""")
    )
    for (p in patterns) { val m = p.find(url); if (m != null) return m.groupValues[1] }
    return null
}

private fun String.encodeSpaces() = replace(" ", "%20")

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  HomeScreen_New
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun HomeScreen_New(
    onEventClick: (String) -> Unit = {},
    onWalletClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReferralClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userPrefs = remember { UserPrefs() }
    val userName = userPrefs.userName ?: "Fan"

    // YouTube dialog state
    var ytVideoId by remember { mutableStateOf<String?>(null) }

    if (ytVideoId != null) {
        YouTubeDialog(videoId = ytVideoId!!, onDismiss = { ytVideoId = null })
    }

    Box(modifier = Modifier.fillMaxSize().background(HSurface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top header ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HWhite)
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("GOOD EVENING", color = HMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                        Text(userName, color = HInk2, fontSize = 21.sp,
                            fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFBF4E3))
                                .clickable { onWalletClick() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🪙", fontSize = 12.sp)
                                Text(uiState.userCoins.toString(), color = Color(0xFF8A6020),
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(HFaint).clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = HInk2, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // ── Feed ────────────────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HGold)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(uiState.feedPosts) { post ->
                        FeedPostItem(post = post, onYouTubeClick = { id -> ytVideoId = id })
                    }
                    if (uiState.offers.isNotEmpty()) {
                        item {
                            Text("Limited Offers", color = HInk2, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                        }
                        items(uiState.offers) { offer ->
                            OfferRow(offer = offer, onClaim = { viewModel.claimOffer(it) })
                        }
                    }
                    if (uiState.feedPosts.isEmpty() && uiState.offers.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No updates yet", color = HMuted, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ━━ Layout Dispatcher ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun FeedPostItem(post: Rs3FeedPost, onYouTubeClick: (String) -> Unit) {
    when (post.layout.lowercase()) {
        "hero"   -> HeroPostCard(post, onYouTubeClick)
        "reel"   -> ReelCard(post, onYouTubeClick)
        "banner" -> BannerCard(post, onYouTubeClick)
        "card"   -> ContentCard(post, onYouTubeClick)
        "update" -> TextUpdateCard(post)
        "grid2"  -> FeedCard(post, onYouTubeClick)
        else     -> FeedCard(post, onYouTubeClick)
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  HERO — Full bleed image/video, title overlay at bottom
//  Distinctive: edge-to-edge (no horizontal padding), tall 260dp, strong gradient
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun HeroPostCard(post: Rs3FeedPost, onYouTubeClick: (String) -> Unit) {
    val ytId = post.link?.let { extractYouTubeId(it) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .padding(top = 0.dp, bottom = 8.dp)
            .height(260.dp)
    ) {
        when {
            post.type == "video" && !post.mediaUrl.isNullOrEmpty() ->
                LoopingVideoPlayer(url = post.mediaUrl!!, modifier = Modifier.fillMaxSize())
            ytId != null ->
                YoutubeThumb(ytId = ytId, onClick = { onYouTubeClick(ytId) }, modifier = Modifier.fillMaxSize(), thumbUrl = post.thumbnailUrl)
            !post.mediaUrl.isNullOrEmpty() ->
                AsyncImage(
                    model = post.mediaUrl.encodeSpaces(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            else ->
                Box(modifier = Modifier.fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF1A1008), Color(0xFF2A1E06)))))
        }
        // Strong dark gradient
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(Color.Transparent, Color(0xCC000000), Color(0xF0000000)),
                startY = 80f
            )))
        // Gold left accent bar
        Box(modifier = Modifier.width(4.dp).height(48.dp)
            .align(Alignment.BottomStart)
            .offset(x = 20.dp, y = (-20).dp)
            .clip(RoundedCornerShape(2.dp))
            .background(HGold))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 32.dp, end = 16.dp, bottom = 20.dp)) {
            if (!post.type.isNullOrEmpty()) {
                Text(post.type.uppercase(), color = HGold, fontSize = 9.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(4.dp))
            }
            Text(post.title ?: "", color = HWhite, fontSize = 24.sp,
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!post.subtitle.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(post.subtitle!!, color = Color.White.copy(alpha = 0.65f), fontSize = 13.sp, maxLines = 1)
            }
            if (ytId != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(HGold)
                        .clickable { onYouTubeClick(ytId) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = HInk, modifier = Modifier.size(14.dp))
                    Text("Watch Now", color = HInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  REEL — Dark letterbox, fills most of the screen, plays on loop like Instagram
//  Distinctive: dark bg, 9:16-ish ratio, muted-controls inline player
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun ReelCard(post: Rs3FeedPost, onYouTubeClick: (String) -> Unit) {
    val ytId = post.link?.let { extractYouTubeId(it) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0A0A0A))
        ) {
            when {
                post.type == "video" && !post.mediaUrl.isNullOrEmpty() ->
                    LoopingVideoPlayer(url = post.mediaUrl!!, modifier = Modifier.fillMaxSize())
                ytId != null ->
                    YoutubeThumb(ytId = ytId, onClick = { onYouTubeClick(ytId) }, modifier = Modifier.fillMaxSize(), thumbUrl = post.thumbnailUrl)
                !post.mediaUrl.isNullOrEmpty() ->
                    AsyncImage(
                        model = post.mediaUrl!!.encodeSpaces(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                else -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)))
            }
            // Bottom gradient
            Box(modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xAA000000), Color(0xDD000000)),
                    startY = 240f
                )))
            // REEL badge top-left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xBB000000))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.PlayCircle, null, tint = HGold, modifier = Modifier.size(12.dp))
                    Text("REEL", color = HWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
            }
            // Tap to watch if YouTube
            if (ytId != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xCCC9973A))
                        .clickable { onYouTubeClick(ytId) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = HWhite, modifier = Modifier.size(32.dp))
                }
            }
            // Title at bottom
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(post.title ?: "", color = HWhite, fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!post.subtitle.isNullOrEmpty())
                    Text(post.subtitle!!, color = Color.White.copy(0.6f), fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  BANNER — Tall portrait poster (like a movie poster)
//  Distinctive: portrait 3:4 ratio, centred title, gold frame border
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun BannerCard(post: Rs3FeedPost, onYouTubeClick: (String) -> Unit) {
    val ytId = post.link?.let { extractYouTubeId(it) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(320.dp)
            .border(1.dp, HGold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        if (!post.mediaUrl.isNullOrEmpty()) {
            AsyncImage(
                model = post.mediaUrl.encodeSpaces(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFF2A1E06), Color(0xFF0E0C08)))))
        }
        // Scrim
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(Color.Transparent, Color(0xBB000000)),
                startY = 140f
            )))
        // "POSTER" tag top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(HGold)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) { Text("POSTER", color = HInk, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(post.title ?: "", color = HWhite, fontSize = 20.sp,
                fontFamily = FontFamily.Serif, maxLines = 2, overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (!post.subtitle.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(post.subtitle!!, color = Color.White.copy(0.6f), fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1)
            }
            if (ytId != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(HGold)
                        .clickable { onYouTubeClick(ytId) }
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = HInk, modifier = Modifier.size(14.dp))
                    Text("Watch Trailer", color = HInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  CONTENT CARD — Classic card: fixed 200dp image, info row below with coin chip
//  Distinctive: white bg, logo-style left stripe, structured info row
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun ContentCard(post: Rs3FeedPost, onYouTubeClick: (String) -> Unit) {
    val ytId = post.link?.let { extractYouTubeId(it) }
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(HWhite)
    ) {
        // Image area
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            when {
                post.type == "video" && !post.mediaUrl.isNullOrEmpty() ->
                    LoopingVideoPlayer(url = post.mediaUrl!!, modifier = Modifier.fillMaxSize())
                ytId != null ->
                    YoutubeThumb(ytId = ytId, onClick = { onYouTubeClick(ytId) }, modifier = Modifier.fillMaxSize(), thumbUrl = post.thumbnailUrl)
                !post.mediaUrl.isNullOrEmpty() ->
                    AsyncImage(
                        model = post.mediaUrl.encodeSpaces(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                else ->
                    Box(modifier = Modifier.fillMaxSize().background(HFaint))
            }
            // Content type badge
            if (!post.type.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) { Text(post.type.uppercase(), color = HGoldPale, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
            }
        }
        // Info row with left gold stripe accent
        Row(modifier = Modifier.fillMaxWidth()) {
            // Gold bar
            Box(modifier = Modifier.width(4.dp).height(72.dp).background(HGold))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 12.dp)) {
                Text(post.title ?: "", color = HInk2, fontSize = 15.sp,
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!post.subtitle.isNullOrEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text(post.subtitle!!, color = HMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            if (!post.link.isNullOrEmpty() && ytId == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(HFaint)
                        .clickable {
                            uriHandler.openUri(post.link)
                        },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowForward, null, tint = HGold, modifier = Modifier.size(16.dp)) }
            }
            if (ytId != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(HGold)
                        .clickable { onYouTubeClick(ytId) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.PlayArrow, null, tint = HInk, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  FEED CARD — Standard card (default / grid2)
//  Distinctive: thin gold top border, smaller image 140dp, compact
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun FeedCard(post: Rs3FeedPost, onYouTubeClick: (String) -> Unit) {
    val ytId = post.link?.let { extractYouTubeId(it) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, HFaint, RoundedCornerShape(14.dp))
            .background(HWhite)
    ) {
        // Top gold accent stripe
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(
            Brush.horizontalGradient(listOf(HGold, HGoldPale, HFaint))
        ))
        when {
            post.type == "video" && !post.mediaUrl.isNullOrEmpty() ->
                LoopingVideoPlayer(url = post.mediaUrl!!, modifier = Modifier.fillMaxWidth().height(140.dp))
            ytId != null ->
                YoutubeThumb(ytId = ytId, onClick = { onYouTubeClick(ytId) },
                    modifier = Modifier.fillMaxWidth().height(140.dp), thumbUrl = post.thumbnailUrl)
            !post.mediaUrl.isNullOrEmpty() ->
                AsyncImage(
                    model = post.mediaUrl.encodeSpaces(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
            else -> {}
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(post.title ?: "", color = HInk2, fontSize = 15.sp, fontFamily = FontFamily.Serif,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!post.subtitle.isNullOrEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(post.subtitle!!, color = HMuted, fontSize = 12.sp, maxLines = 2, lineHeight = 17.sp)
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
//  TEXT UPDATE CARD — Text-only / link post
//  Distinctive: parchment background, italic quote style, link CTA
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun TextUpdateCard(post: Rs3FeedPost) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFBF4E3))
            .border(1.dp, HGold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Gold vertical bar
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(HGold))
            Column(modifier = Modifier.weight(1f).padding(14.dp)) {
                if (!post.type.isNullOrEmpty()) {
                    Text(post.type.uppercase(), color = HGold, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(6.dp))
                }
                Text(post.title ?: "", color = HInk2, fontSize = 16.sp,
                    fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic)
                if (!post.subtitle.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(post.subtitle!!, color = HMuted, fontSize = 13.sp, lineHeight = 20.sp)
                }
                if (!post.body.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(post.body!!, color = Color(0xFF4A3D22), fontSize = 13.sp, lineHeight = 20.sp)
                }
                if (!post.link.isNullOrEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(HGold.copy(alpha = 0.15f))
                            .clickable {
                                uriHandler.openUri(post.link)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Link, null, tint = HGold, modifier = Modifier.size(14.dp))
                        Text("Open Link", color = HGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ━━ Offer Row ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun OfferRow(offer: Rs3Offer, onClaim: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(HWhite)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏷️", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(offer.title, color = HInk2, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (offer.coinCost > 0)
                    Text("${offer.coinCost} coins", color = HGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onClaim(offer.id) },
                colors = ButtonDefaults.buttonColors(containerColor = HGold, contentColor = HInk),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) { Text(if (offer.claimed) "Claimed" else "Redeem", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// Private video helper functions removed (migrated to VideoPlayers.kt)
