package com.cinepass.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

data class SelfieImage(
    val preview: ImageBitmap,
    val bytes: ByteArray,
)

@Composable
expect fun SelfiePickerField(
    selfie: SelfieImage?,
    onSelfieSelected: (SelfieImage) -> Unit,
    submitted: Boolean,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
)

@Composable
expect fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    touched: Boolean,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
)
