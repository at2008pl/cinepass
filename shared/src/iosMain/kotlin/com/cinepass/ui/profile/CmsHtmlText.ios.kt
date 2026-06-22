package com.cinepass.ui.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
actual fun CmsHtmlText(html: String, modifier: Modifier) {
    Text(
        text = html.replace(Regex("<[^>]*>"), ""),
        color = Color(0xFF9A8A6A),
        fontSize = 12.sp,
        modifier = modifier
    )
}
