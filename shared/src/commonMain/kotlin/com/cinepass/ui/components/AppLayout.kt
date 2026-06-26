package com.cinepass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppLayout {
    /** Horizontal inset for text headers and readable body copy. */
    val TextInset: Dp = 16.dp

    /** Vertical spacing between stacked feed/list blocks. */
    val BlockSpacing: Dp = 8.dp
}

@Composable
fun ScreenTopBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = AppLayout.TextInset)
            .padding(top = 12.dp, bottom = 14.dp),
        content = content,
    )
}
