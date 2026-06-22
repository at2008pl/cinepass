package com.cinepass.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.LoginRequest
import com.cinepass.data.prefs.UserPrefs
import kotlinx.coroutines.launch

// RS³ Design tokens (local aliases)
private val LGold    = Color(0xFFC9973A)
private val LGold2   = Color(0xFFE8B84B)
private val LGoldPale= Color(0xFFF5D78E)
private val LInk     = Color(0xFF0E0C08)
private val LInk2    = Color(0xFF1E1A10)
private val LSurface = Color(0xFFFDFAF3)
private val LMuted   = Color(0xFF9A8A6A)
private val LFaint   = Color(0xFFEDE8DC)
private val LRed     = Color(0xFFC0392B)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val userPrefs = remember { UserPrefs() }
    var identifier by remember { mutableStateOf(userPrefs.rememberedIdentifier ?: "") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val identifierError = submitted && identifier.isBlank()
    val passwordError = submitted && password.length < 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LSurface)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Dark branding header ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0E0A06), Color(0xFF1E1408)),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(400f, 300f)
                    )
                )
                .clip(
                    RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp)) {
                // Logo row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF8A5C1A), LGold))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎬", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RS³ FILMS",
                            color = LGoldPale,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "FAN COMMUNITY",
                            color = LGoldPale.copy(alpha = 0.38f),
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Welcome\nBack",
                    color = Color(0xFFFBF0D8),
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sign in to your fan account",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )
            }
        }

        // ── Form area ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(top = 32.dp, bottom = 40.dp)
        ) {
            // Email / identifier
            Text("Email or Mobile Number", color = LMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("arjun@mail.com", color = LMuted.copy(alpha = 0.5f)) },
                singleLine = true,
                isError = identifierError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LGold,
                    unfocusedBorderColor = LFaint,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = LInk2,
                    unfocusedTextColor = LInk2
                )
            )

            Spacer(Modifier.height(16.dp))

            // Password
            Text("Password", color = LMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••", color = LMuted.copy(alpha = 0.5f)) },
                singleLine = true,
                isError = passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = LMuted
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LGold,
                    unfocusedBorderColor = LFaint,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = LInk2,
                    unfocusedTextColor = LInk2
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    color = LGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { }
                )
            }

            if (errorMessage.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = errorMessage, color = LRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Sign In — gold button
            Button(
                onClick = {
                    submitted = true
                    if (identifierError || passwordError) {
                        errorMessage = "Enter valid credentials."
                        return@Button
                    }
                    loading = true
                    errorMessage = ""
                    coroutineScope.launch {
                        try {
                            val response = ApiClient.apiService.login(
                                LoginRequest(identifier = identifier.trim(), password = password)
                            )
                            if (response.isSuccessful && response.body()?.success == true) {
                                val auth = response.body()?.data
                                if (auth != null) {
                                    userPrefs.saveAuthData(
                                        accessToken = auth.accessToken,
                                        refreshToken = auth.refreshToken,
                                        userId = auth.user.id,
                                        name = auth.user.name,
                                        email = auth.user.email,
                                        phone = auth.user.phone,
                                        referralCode = auth.user.referralCode,
                                        coins = auth.user.coins,
                                        isVerified = auth.user.isVerified,
                                        selfieUrl = auth.user.selfieUrl,
                                    )
                                }
                                onLoginSuccess()
                            } else {
                                errorMessage = response.body()?.message ?: "Login failed"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Network error"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LGold,
                    contentColor = LInk
                )
            ) {
                Text(
                    text = if (loading) "Signing In…" else "Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Create Account — ghost button
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LFaint),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LInk2)
            ) {
                Text(text = "Create Account", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }

            Spacer(Modifier.height(28.dp))

            // Security note
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Powered by secure OTP + JWT", color = LMuted, fontSize = 11.sp)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = LMuted, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("End-to-end encrypted", color = LMuted, fontSize = 11.sp)
                }
            }
        }
    }
}
