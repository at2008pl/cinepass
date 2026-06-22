@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.cinepass.ui.auth

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
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSDateFormatter
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraCaptureMode
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

private val T_Line = Color(0xFFE4DDD0)
private val T_LineLight = Color(0xFFEDE8DE)
private val T_Ink = Color(0xFF1C1408)
private val T_InkLight = Color(0xFF7A6A50)
private val T_Muted = Color(0xFFA89880)
private val T_Red = Color(0xFF8B2E2E)
private val T_Surface = Color(0xFFFFFFFF)

private fun UIImage.toSelfieImage(): SelfieImage? {
    val data = UIImageJPEGRepresentation(this, 0.9) ?: return null
    val bytes = data.toByteArray()
    val skiaImage = Image.makeFromEncoded(bytes)
    return SelfieImage(skiaImage.toComposeImageBitmap(), bytes)
}

private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, length.toULong())
    }
    return bytes
}

private class ImagePickerDelegate(
    private val onResult: (SelfieImage?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        onResult(image?.toSelfieImage())
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
        onResult(null)
    }
}

@Composable
actual fun SelfiePickerField(
    selfie: SelfieImage?,
    onSelfieSelected: (SelfieImage) -> Unit,
    submitted: Boolean,
    modifier: Modifier,
) {
    val rootController = LocalUIViewController.current
    var showDialog by remember { mutableStateOf(false) }
    val pickerDelegate = remember { ImagePickerDelegate { image -> image?.let(onSelfieSelected) } }

    fun openPicker(sourceType: UIImagePickerControllerSourceType) {
        val picker = UIImagePickerController()
        picker.sourceType = sourceType
        if (sourceType == UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera) {
            picker.cameraCaptureMode =
                UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModePhoto
        }
        picker.delegate = pickerDelegate
        rootController.presentViewController(picker, true, null)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("SELFIE", fontSize = 9.sp, color = T_InkLight)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (submitted && selfie == null) T_Red else T_Line,
                    RoundedCornerShape(8.dp),
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
                Text(if (selfie != null) "Selfie uploaded" else "Upload selfie", fontSize = 14.sp, color = T_Ink)
                Text(if (selfie != null) "Tap to change" else "Tap to capture or select", fontSize = 11.sp, color = T_Muted)
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
                                openPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
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
                                openPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
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
            confirmButton = { TextButton(onClick = { showDialog = false }) { Text("CANCEL") } },
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
    Column(modifier = modifier.padding(bottom = 16.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, color = T_InkLight, modifier = Modifier.padding(bottom = 8.dp))
        BasicTextField(
            value = value,
            onValueChange = { input ->
                onValueChange(input.filter { it.isDigit() || it == '-' }.take(10))
            },
            textStyle = TextStyle(fontSize = 14.sp, color = T_Ink),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(T_Surface, RoundedCornerShape(6.dp))
                .border(1.dp, if (touched && error != null) T_Red else T_Line, RoundedCornerShape(6.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("YYYY-MM-DD", fontSize = 14.sp, color = T_Muted)
                }
                inner()
            },
        )
        if (touched && error != null) {
            Text(error, fontSize = 12.sp, color = T_Red)
        }
    }
}
