package com.cinepass.ui.auth

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Calendar

private val T_Line = Color(0xFFE4DDD0)
private val T_LineLight = Color(0xFFEDE8DE)
private val T_Ink = Color(0xFF1C1408)
private val T_InkLight = Color(0xFF7A6A50)
private val T_Muted = Color(0xFFA89880)
private val T_Red = Color(0xFF8B2E2E)
private val T_Surface = Color(0xFFFFFFFF)

@Composable
actual fun SelfiePickerField(
    selfie: SelfieImage?,
    onSelfieSelected: (SelfieImage) -> Unit,
    submitted: Boolean,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val cameraUri = remember {
        val photoFile = File.createTempFile("selfie_", ".jpg", context.cacheDir)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            context.contentResolver.openInputStream(cameraUri)?.use { stream ->
                val bytes = stream.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    onSelfieSelected(SelfieImage(bitmap.asImageBitmap(), bytes))
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val bytes = stream.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    onSelfieSelected(SelfieImage(bitmap.asImageBitmap(), bytes))
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SELFIE",
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            color = T_InkLight,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (submitted && selfie == null) T_Red else T_Line,
                    shape = RoundedCornerShape(8.dp),
                )
                .clip(RoundedCornerShape(8.dp))
                .clickable { showDialog = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (selfie != null) {
                Image(
                    bitmap = selfie.preview,
                    contentDescription = "Selfie",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).background(T_LineLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = T_Muted)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selfie != null) "Selfie uploaded" else "Upload selfie",
                    fontSize = 14.sp,
                    color = T_Ink,
                )
                Text(
                    text = if (selfie != null) "Tap to change" else "Tap to capture or select",
                    fontSize = 11.sp,
                    color = T_Muted,
                )
            }
        }
        if (submitted && selfie == null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selfie is required", fontSize = 11.sp, color = T_Red)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = T_Surface,
            title = { Text("UPLOAD SELFIE") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, T_Line, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showDialog = false
                                cameraLauncher.launch(cameraUri)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = T_Muted)
                        Text("Take photo", color = T_Ink)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, T_Line, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = T_Muted)
                        Text("Choose from gallery", color = T_Ink)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("CANCEL") }
            },
        )
    }
}

@Composable
actual fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    touched: Boolean,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onValueChange("%04d-%02d-%02d".format(year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    RegistrationFieldShared(
        value = value,
        onValueChange = {},
        label = label,
        error = error,
        touched = touched,
        hint = "YYYY-MM-DD",
        readOnly = true,
        onClick = { datePickerDialog.show() },
        modifier = modifier,
    )
}

@Composable
internal fun RegistrationFieldShared(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    touched: Boolean = false,
    hint: String? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    prefix: String? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Minimal duplicate used only by DatePickerField on Android
    androidx.compose.foundation.layout.Column(modifier = modifier.padding(bottom = 16.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, color = T_InkLight)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(T_Surface, RoundedCornerShape(6.dp))
                .border(1.dp, if (touched && error != null) T_Red else T_Line, RoundedCornerShape(6.dp))
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                enabled = !readOnly,
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (value.isEmpty() && hint != null) Text(hint, color = T_Muted)
                    inner()
                },
            )
            trailingContent?.invoke()
        }
        if (touched && error != null) {
            Text(error, fontSize = 12.sp, color = T_Red)
        }
    }
}
