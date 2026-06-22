package com.cinepass.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthLogo() {
    Text(
        text = "RS\u00B3 FILMS",
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            letterSpacing = 4.sp,
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFFD4A017),
                    Color(0xFFF2C94C),
                    Color(0xFFD4A017),
                ),
            ),
        ),
    )
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 12.dp)
            .width(40.dp)
            .height(1.dp)
            .background(Color(0x99D4A017)),
    )
    Text(
        text = "An Emotional Journey of Love",
        style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = Color(0xFF6B7A8D),
        ),
    )
}

@Composable
fun AuthGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD6FFFFFF)),
        border = BorderStroke(1.dp, Color(0xFFD7E6F3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x99FFFFFF), Color(0xFFF4F8FC)),
                    ),
                )
                .padding(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                content = content,
            )
        }
    }
}

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        textStyle = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            letterSpacing = 0.sp,
            color = Color(0xFF1E2A38),
        ),
        shape = RoundedCornerShape(15.dp),
        singleLine = true,
        isError = isError,
        label = {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 0.1.sp,
                ),
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFD4A017),
            unfocusedBorderColor = Color(0xFFC5D8EA),
            errorBorderColor = Color(0xFFE85D75),
            focusedContainerColor = Color(0xFFFFF8EA),
            unfocusedContainerColor = Color(0xFFF2F6FA),
            errorContainerColor = Color(0xFFFFF0F3),
            cursorColor = Color(0xFF1E2A38),
            focusedLabelColor = Color(0xFFD4A017),
            unfocusedLabelColor = Color(0xFF7A8A9A),
            focusedLeadingIconColor = Color(0x591E2A38),
            unfocusedLeadingIconColor = Color(0x591E2A38),
            focusedTrailingIconColor = Color(0x591E2A38),
            unfocusedTrailingIconColor = Color(0x591E2A38),
            focusedTextColor = Color(0xFF1E2A38),
            unfocusedTextColor = Color(0xFF1E2A38),
            errorTextColor = Color(0xFF1E2A38),
        ),
    )
}

@Composable
fun GoldGradientButton(
    text: String,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(17.dp),
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "auth_button_scale",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .shadow(10.dp, shape)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        if (pressed) Color(0xFFB8870F) else Color(0xFFD09A0E),
                        Color(0xFFF2C94C),
                        if (pressed) Color(0xFFC89512) else Color(0xFFF1D26A),
                        if (pressed) Color(0xFFB8870F) else Color(0xFFD09A0E),
                    ),
                ),
                shape = shape,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color(0xFF1E2A38),
            )
        }
    }
}

@Composable
fun FilmStrip() {
    Row {
        listOf(8, 8, 40, 8, 8, 40, 8, 8).forEachIndexed { index, width ->
            Box(
                modifier = Modifier
                    .padding(end = if (index == 7) 0.dp else 6.dp)
                    .width(width.dp)
                    .height(8.dp)
                    .background(Color(0x1FD4A017), RoundedCornerShape(2.dp)),
            )
        }
    }
}
