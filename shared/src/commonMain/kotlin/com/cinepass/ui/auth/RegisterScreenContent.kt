package com.cinepass.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.RegisterFormData
import com.cinepass.data.api.lookupPincode
import com.cinepass.data.prefs.UserPrefs
import com.cinepass.data.preferences.ReferralPreferences
import com.cinepass.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

private val EMAIL_REGEX = Regex("""^[A-Za-z0-9+_.'-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

private fun validateRegistrationForm(
    fullName: String,
    email: String,
    phone: String,
    gender: String,
    dob: String,
    addressLine: String,
    city: String,
    state: String,
    pincode: String,
    referralCode: String,
    password: String,
    confirmPassword: String,
    otp: String,
    hasSelfie: Boolean,
): String? {
    val trimmedName = fullName.trim()
    when {
        trimmedName.isEmpty() -> return "Full name is required"
        trimmedName.length < 3 -> return "Minimum 3 characters for full name"
        !trimmedName.matches(Regex("^[a-zA-Z\\s.'-]+$")) -> return "Full name: letters and spaces only"
    }

    val trimmedEmail = email.trim()
    when {
        trimmedEmail.isEmpty() -> return "Email address is required"
        !EMAIL_REGEX.matches(trimmedEmail) -> return "Invalid email format"
    }

    val phoneDigits = phone.filter { it.isDigit() }
    when {
        phoneDigits.isEmpty() -> return "Mobile number is required"
        phoneDigits.length != 10 -> return "Mobile number must be exactly 10 digits"
        !phoneDigits.matches(Regex("^[6-9]\\d{9}$")) -> return "Mobile number must begin with 6, 7, 8 or 9"
    }

    if (gender.isBlank()) return "Please select gender"
    if (dob.isBlank()) return "Date of birth is required"

    val trimmedPincode = pincode.trim()
    when {
        trimmedPincode.isEmpty() -> return "Pincode is required"
        !trimmedPincode.matches(Regex("^\\d{6}$")) -> return "Pincode must be exactly 6 digits"
    }

    if (state.trim().isEmpty()) return "State is required"
    if (city.trim().isEmpty()) return "City is required"

    when {
        addressLine.trim().isEmpty() -> return "Address is required"
        addressLine.trim().length < 5 -> return "Please enter your full address"
    }

    val trimmedReferral = referralCode.trim()
    when {
        trimmedReferral.isEmpty() -> return "Referral code is required"
        !trimmedReferral.matches(Regex("^RS3_[A-Z0-9]{4,10}$", RegexOption.IGNORE_CASE)) ->
            return "Invalid referral code — expected RS3_XXXXXX"
    }

    when {
        password.isEmpty() -> return "Password is required"
        password.length < 8 -> return "Password must be at least 8 characters"
        confirmPassword.isEmpty() -> return "Please confirm your password"
        confirmPassword != password -> return "Passwords do not match"
    }

    val trimmedOtp = otp.trim()
    when {
        trimmedOtp.isEmpty() -> return "OTP is required"
        !trimmedOtp.matches(Regex("^\\d{4,6}$")) -> return "Enter the 4–6 digit OTP"
    }

    if (!hasSelfie) return "Selfie is required"

    return null
}

/* ═══════════════════════════════════════════════════════════════════════════
   RS³ FILMS — REGISTRATION  |  Light · Luxury · Editorial
   Palette: Warm ivory parchment + champagne gold + near-black ink
═══════════════════════════════════════════════════════════════════════════ */

// Color Palette matching RegisterScreen.jsx
private val T_Bg         = Color(0xFFFAF7F2)
private val T_BgAlt      = Color(0xFFF5F0E8)
private val T_Surface    = Color(0xFFFFFFFF)
private val T_Card       = Color(0xFFFFFDF9)
private val T_Line       = Color(0xFFE4DDD0)
private val T_LineLight  = Color(0xFFEDE8DE)
private val T_Gold       = Color(0xFFA67C2E)
private val T_GoldBright = Color(0xFFC9A84C)
private val T_GoldDim    = Color(0xFF7A5C1E)
private val T_GoldPale   = Color(0xFFFDF6E3)
private val T_Ink        = Color(0xFF1C1408)
private val T_InkMid     = Color(0xFF3D2E10)
private val T_InkLight   = Color(0xFF7A6A50)
private val T_Muted      = Color(0xFFA89880)
private val T_MutedLight = Color(0xFFC4B49A)
private val T_Red        = Color(0xFF8B2E2E)
private val T_RedPale    = Color(0xFFFDF2F2)
private val T_Green      = Color(0xFF2E6B45)

// Gradients matching JSX
private val G_Gold = Brush.linearGradient(
    listOf(T_GoldDim, T_Gold, T_GoldBright, Color(0xFFE8C96A), T_Gold)
)
private val G_GoldH = Brush.horizontalGradient(
    listOf(T_GoldDim, T_Gold, T_GoldBright, Color(0xFFE8C96A))
)
private val G_Bg = Brush.verticalGradient(
    listOf(T_Bg, T_BgAlt, T_LineLight)
)

@Composable
fun RegisterScreenContent(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository: AuthRepository = koinInject()
    val userPrefs = remember { UserPrefs() }
    val referralPrefs = remember { ReferralPreferences() }
    
    // ── Form State ───────────────────────────────────────────────────────────
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var addressLine by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    // Auto-fill referral code from deep link / clipboard (stored in ReferralPreferences)
    LaunchedEffect(Unit) {
        val pending = referralPrefs.getPendingReferralCode().first()
        if (!pending.isNullOrBlank() && referralCode.isBlank()) {
            referralCode = pending
        } else if (referralCode.isBlank()) {
            referralCode = "RS3_DEFAULT"
        }
    }
    
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var selfie by remember { mutableStateOf<SelfieImage?>(null) }
    
    // Pincode auto-fill (matching JSX behavior)
    var pincodeStatus by remember { mutableStateOf<String?>(null) } // null, "ok", or "err"
    
    LaunchedEffect(pincode) {
        val pin = pincode.filter { it.isDigit() }
        if (pin.length != 6) {
            if (pin.isNotEmpty() && pin.length < 6) {
                pincodeStatus = null
            }
            return@LaunchedEffect
        }

        pincodeStatus = null
        withContext(Dispatchers.Default) {
            val result = lookupPincode(ApiClient.httpClient, pin)
            withContext(Dispatchers.Main) {
                if (result != null) {
                    city = result.first
                    state = result.second
                    pincodeStatus = "ok"
                } else {
                    city = ""
                    state = ""
                    pincodeStatus = "err"
                }
            }
        }
    }
    
    // ── Validation (matching JSX rules) ──────────────────────────────────────
    val nameError = if (submitted) {
        val trimmed = fullName.trim()
        when {
            trimmed.isEmpty() -> "Full name is required"
            trimmed.length < 3 -> "Minimum 3 characters"
            !trimmed.matches(Regex("^[a-zA-Z\\s.'-]+$")) -> "Letters and spaces only"
            else -> null
        }
    } else null
    
    val emailError = if (submitted) {
        val trimmed = email.trim()
        when {
            trimmed.isEmpty() -> "Email address is required"
            !EMAIL_REGEX.matches(trimmed) -> "Invalid email format"
            else -> null
        }
    } else null
    
    val phoneDigits = phone.filter { it.isDigit() }
    val phoneError = if (submitted) {
        when {
            phoneDigits.isEmpty() -> "Mobile number is required"
            phoneDigits.length != 10 -> "Must be exactly 10 digits"
            !phoneDigits.matches(Regex("^[6-9]\\d{9}$")) -> "Must begin with 6, 7, 8 or 9"
            else -> null
        }
    } else null
    
    val genderError = if (submitted && gender.isBlank()) "Please select gender" else null
    val dobError = if (submitted && dob.isBlank()) "Date of birth is required" else null
    
    val pincodeError = if (submitted) {
        val trimmed = pincode.trim()
        when {
            trimmed.isEmpty() -> "Pincode is required"
            !trimmed.matches(Regex("^\\d{6}$")) -> "Must be exactly 6 digits"
            pincodeStatus == "err" -> "Invalid pincode - could not fetch location"
            else -> null
        }
    } else null
    
    val stateError = if (submitted && state.trim().isEmpty()) "State is required" else null
    
    val cityError = if (submitted && city.trim().isEmpty()) "City is required" else null
    
    val addressError = if (submitted) {
        when {
            addressLine.trim().isEmpty() -> "Address is required"
            addressLine.trim().length < 5 -> "Please enter full address"
            else -> null
        }
    } else null
    
    val referralError = if (submitted) {
        val trimmed = referralCode.trim()
        when {
            trimmed.isEmpty() -> "Referral code is required"
            !trimmed.matches(Regex("^RS3_[A-Z0-9]{4,10}$", RegexOption.IGNORE_CASE)) -> "Invalid format — expected RS3_XXXXXX"
            else -> null
        }
    } else null
    
    val passwordError = if (submitted) {
        when {
            password.isEmpty() -> "Password is required"
            password.length < 8 -> "Minimum 8 characters"
            else -> null
        }
    } else null
    
    val confirmError = if (submitted) {
        when {
            confirmPassword.isEmpty() -> "Please confirm your password"
            confirmPassword != password -> "Passwords do not match"
            else -> null
        }
    } else null
    
    val otpError = if (submitted) {
        val trimmed = otp.trim()
        when {
            trimmed.isEmpty() -> "OTP is required"
            !trimmed.matches(Regex("^\\d{4,6}$")) -> "Enter the 4–6 digit code"
            else -> null
        }
    } else null
    
    // Password strength indicator (matching JSX)
    fun passwordScore(p: String): Triple<String, Color, Float> {
        var score = 0
        if (p.length >= 8) score++
        if (p.length >= 12) score++
        if (p.any { it.isUpperCase() }) score++
        if (p.any { it.isDigit() }) score++
        if (p.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score <= 1 -> Triple("Very Weak", Color(0xFFC0392B), 0.15f)
            score == 2 -> Triple("Weak", Color(0xFFF39C12), 0.35f)
            score == 3 -> Triple("Okay", Color(0xFFF1C40F), 0.6f)
            score == 4 -> Triple("Strong", Color(0xFF27AE60), 0.85f)
            else -> Triple("Very Strong", T_Green, 1f)
        }
    }
    
    // Progress calculation (matching JSX)
    val requiredFields = listOf(
        fullName, email, phone, gender, dob, 
        addressLine, city, state, pincode, 
        referralCode, password, confirmPassword, otp
    )
    val filledCount = requiredFields.count { it.isNotBlank() }
    val progressValue = (filledCount.toFloat() / requiredFields.size * 100).toInt()
    
    
    // ── UI Layout ────────────────────────────────────────────────────────────
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(G_Bg)
    ) {
        // ── Static Header ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "An Emotional Journey of Love",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                color = T_Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            brush = G_Gold,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 10.sp
                        )
                    ) {
                        append("RS³ FILMS")
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(1.dp)
                    .background(G_GoldH)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MEMBER REGISTRATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 4.sp,
                color = T_Muted,
                textAlign = TextAlign.Center
            )
        }

        // ── Scrollable Form ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Progress Bar ──────────────────────────────────────────────
            // ── Progress Bar ──────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PROFILE COMPLETION",
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        color = T_Muted
                    )
                    Text(
                        text = "$progressValue%",
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = T_Gold,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(T_LineLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressValue / 100f)
                            .height(1.dp)
                            .background(G_GoldH)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            SectionDivider(num = "1", title = "Personal Details")

            // Full Name
            RegistrationField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                error = nameError,
                touched = submitted
            )
            
            // Email
            RegistrationField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                error = emailError,
                touched = submitted,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
                
                // Phone
                Row(modifier = Modifier.fillMaxWidth()) {
                    RegistrationField(
                        value = phone,
                        onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                        label = "Mobile Number",
                        error = phoneError,
                        touched = submitted,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        prefix = "+91 ",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Gender & DOB side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gender dropdown
                    GenderDropdownField(
                        value = gender,
                        onValueChange = { gender = it },
                        error = genderError,
                        touched = submitted,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DatePickerField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = "Date of Birth",
                        error = dobError,
                        touched = submitted,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // ── Section 2: Address ────────────────────────────────────────
                SectionDivider(num = "2", title = "Address")
                
                RegistrationField(
                    value = pincode,
                    onValueChange = { pincode = it.filter { c -> c.isDigit() }.take(6) },
                    label = "Pincode",
                    error = pincodeError,
                    touched = submitted,
                    hint = "Enter 6-digit pincode",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RegistrationField(
                        value = state,
                        onValueChange = { state = it },
                        label = "State",
                        error = stateError,
                        touched = submitted,
                        hint = "Auto-filled from pincode",
                        modifier = Modifier.weight(1f)
                    )
                    
                    RegistrationField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City",
                        error = cityError,
                        touched = submitted,
                        hint = "Auto-filled from pincode",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                RegistrationField(
                    value = addressLine,
                    onValueChange = { addressLine = it },
                    label = "Address Line",
                    error = addressError,
                    touched = submitted,
                    hint = "Street address, building, landmark"
                )
                
                // ── Section 3: Security & Verification ───────────────────────
                SectionDivider(num = "3", title = "Security & Verification")
                
                RegistrationField(
                    value = referralCode,
                    onValueChange = { referralCode = it },
                    label = "Referral Code",
                    error = referralError,
                    touched = submitted,
                    hint = "RS3_XXXXXX"
                )
                
                // Password with strength indicator
                Column(modifier = Modifier.fillMaxWidth()) {
                    RegistrationField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        error = passwordError,
                        touched = submitted,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingContent = {
                            Text(
                                text = if (showPassword) "HIDE" else "SHOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.5.sp,
                                color = T_Gold,
                                modifier = Modifier.clickable { showPassword = !showPassword }
                            )
                        }
                    )
                    
                    // Password strength indicator
                    if (password.isNotBlank()) {
                        val (strengthLabel, strengthColor, strengthProgress) = passwordScore(password)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(T_LineLight, RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = strengthProgress)
                                    .height(3.dp)
                                    .background(strengthColor, RoundedCornerShape(2.dp))
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "Strength: $strengthLabel",
                            fontSize = 11.sp,
                            color = strengthColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                RegistrationField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    error = confirmError,
                    touched = submitted,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingContent = {
                        Text(
                            text = if (showConfirmPassword) "HIDE" else "SHOW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = T_Gold,
                            modifier = Modifier.clickable { showConfirmPassword = !showConfirmPassword }
                        )
                    }
                )
                
                RegistrationField(
                    value = otp,
                    onValueChange = { otp = it.filter { c -> c.isDigit() }.take(6) },
                    label = "OTP",
                    error = otpError,
                    touched = submitted,
                    hint = "4-6 digit code",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                SelfiePickerField(
                    selfie = selfie,
                    onSelfieSelected = { selfie = it },
                    submitted = submitted,
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

        // ── Static Footer: Submit + Login link ─────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(T_Bg)
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 20.dp)
        ) {
            // Error message
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    fontSize = 13.sp,
                    color = T_Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(G_GoldH)
                    .border(1.dp, T_GoldBright, RoundedCornerShape(8.dp))
                    .clickable(enabled = !loading) {
                        submitted = true

                        val validationError = validateRegistrationForm(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            gender = gender,
                            dob = dob,
                            addressLine = addressLine,
                            city = city,
                            state = state,
                            pincode = pincode,
                            referralCode = referralCode,
                            password = password,
                            confirmPassword = confirmPassword,
                            otp = otp,
                            hasSelfie = selfie != null,
                        )

                        if (validationError != null) {
                            errorMessage = validationError
                            coroutineScope.launch { scrollState.scrollTo(0) }
                            return@clickable
                        }

                        loading = true
                        errorMessage = ""

                        coroutineScope.launch {
                            val form = RegisterFormData(
                                name = fullName.trim(),
                                email = email.trim(),
                                phone = "+91${phone.filter { it.isDigit() }}",
                                gender = gender.trim().ifBlank { null },
                                dob = dob.trim().ifBlank { null },
                                addressLine = addressLine.trim().ifBlank { null },
                                city = city.trim().ifBlank { null },
                                state = state.trim().ifBlank { null },
                                pincode = pincode.trim().ifBlank { null },
                                password = password,
                                confirmPassword = confirmPassword,
                                referralCode = referralCode.trim().ifBlank { null },
                                otp = otp.trim(),
                                selfieBytes = selfie!!.bytes,
                            )

                            authRepository.register(form)
                                .onSuccess {
                                    referralPrefs.clearPendingReferralCode()
                                    withContext(Dispatchers.Main) {
                                        onRegisterSuccess()
                                    }
                                }
                                .onFailure { error ->
                                    errorMessage = error.message ?: "Registration failed"
                                }

                            loading = false
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (loading) "REGISTERING..." else "CREATE ACCOUNT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp,
                    color = T_Surface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already a member? ",
                    fontSize = 13.sp,
                    color = T_Muted,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = "Login",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = T_Gold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

// ── Section Divider (matching JSX design) ────────────────────────────────────
@Composable
private fun SectionDivider(num: String, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .border(1.dp, T_GoldBright, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = num,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = T_Gold,
                letterSpacing = 0.5.sp
            )
        }
        
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 3.sp,
            color = T_InkLight
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(T_Line, Color.Transparent)
                    )
                )
        )
    }
}

// ── Registration Field (matching JSX Field component) ────────────────────────
@Composable
private fun RegistrationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    touched: Boolean = false,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: String? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 16.dp)) {
        // Label
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            color = T_InkLight,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Input container
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(T_Surface, RoundedCornerShape(6.dp))
                    .border(
                        width = 1.dp,
                        color = if (touched && error != null) T_Red else T_Line,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .then(
                        if (onClick != null) Modifier.clickable { onClick() }
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prefix != null) {
                    Text(
                        text = prefix,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = T_Ink,
                        letterSpacing = 0.2.sp
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = T_Ink,
                            letterSpacing = 0.2.sp
                        ),
                        keyboardOptions = keyboardOptions,
                        visualTransformation = visualTransformation,
                        singleLine = true,
                        readOnly = readOnly,
                        enabled = !readOnly,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty() && hint != null) {
                                Text(
                                    text = hint,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    color = T_Muted,
                                    letterSpacing = 0.2.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                trailingContent?.invoke()
            }
            
            // Underline (focus effect simulated)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (touched && error != null) T_Red else Color.Transparent)
            )
        }
        
        // Error message
        if (touched && error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                fontSize = 12.sp,
                color = T_Red,
                letterSpacing = 0.3.sp
            )
        }
    }
}
// ── Gender Dropdown Field ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    touched: Boolean = false,
    modifier: Modifier = Modifier
) {
    val genderOptions = listOf("Male", "Female", "Others")
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.padding(bottom = 10.dp)) {
        // Label
        Text(
            text = "GENDER",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            color = T_InkLight,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        // Dropdown container
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .background(T_Surface, RoundedCornerShape(6.dp))
                        .border(
                            width = 1.dp,
                            color = if (touched && error != null) T_Red else T_Line,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = value.ifBlank { "Select gender" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Light,
                        color = if (value.isBlank()) T_Muted else T_Ink,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = T_Muted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Underline (focus effect simulated)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (touched && error != null) T_Red else Color.Transparent)
                )
            }
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(T_Surface)
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                color = if (option == value) T_Gold else T_Ink,
                                letterSpacing = 0.2.sp
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (option == value) T_GoldPale else Color.Transparent
                        )
                    )
                }
            }
        }
        
        // Error message
        if (touched && error != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                fontSize = 11.sp,
                color = T_Red,
                letterSpacing = 0.3.sp
            )
        }
    }
}