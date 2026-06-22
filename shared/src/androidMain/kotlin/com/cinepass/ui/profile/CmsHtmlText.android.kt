package com.cinepass.ui.profile

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun CmsHtmlText(html: String, modifier: Modifier) {
    val spanned = remember(html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }
    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(android.graphics.Color.parseColor("#FF9A8A6A"))
                textSize = 12f
                setLineSpacing(4f, 1f)
            }
        },
        update = { view -> view.text = spanned },
        modifier = modifier
    )
}
