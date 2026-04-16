package com.example.nammaride.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// Pulling in our custom Theme & Network!
import com.example.nammaride.network.RetrofitClient
import com.example.nammaride.network.AuthRequest
import com.example.nammaride.theme.LocalNammaColors
import com.example.nammaride.theme.SuccessGreen
import com.example.nammaride.theme.ErrorRed

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun formatMaskedPhone(phone: String): String {
    if (phone.length < 10) return phone
    return "+91 ${phone.take(4)}... ${phone.takeLast(3)}"
}

@Composable
fun OtpDigitInput(digit: String, onDigitChange: (String) -> Unit, modifier: Modifier = Modifier, focusRequester: FocusRequester) {
    val colors = LocalNammaColors.current
    OutlinedTextField(
        value = digit, onValueChange = { if (it.length <= 1) onDigitChange(it) },
        modifier = modifier.padding(horizontal = 2.dp).height(64.dp).focusRequester(focusRequester),
        singleLine = true, textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = colors.text),
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = colors.input, unfocusedContainerColor = colors.input, unfocusedBorderColor = colors.border, focusedBorderColor = colors.accent, cursorColor = colors.accent, errorBorderColor = ErrorRed),
        shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpInputView(otpCode: String, onCodeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val currentCodeList = remember { mutableStateListOf(*Array(6) { "" }) }
    LaunchedEffect(otpCode) { otpCode.take(6).forEachIndexed { index, char -> currentCodeList[index] = char.toString() } }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until 6) {
            OtpDigitInput(
                digit = currentCodeList[i],
                onDigitChange = { newDigit ->
                    currentCodeList[i] = newDigit
                    onCodeChange(currentCodeList.joinToString(""))
                    if (newDigit.length == 1 && i < 5) focusRequesters[i + 1].requestFocus()
                }, modifier = Modifier.weight(1f), focusRequester = focusRequesters[i]
            )
        }
    }
}

@Composable
fun PhoneInputView(phoneNumber: String, onPhoneChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalNammaColors.current
    OutlinedTextField(
        value = phoneNumber, onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) onPhoneChange(it) },
        prefix = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🇮🇳", fontSize = 20.sp); Spacer(modifier = Modifier.width(8.dp))
                Text("+91", fontSize = 18.sp, color = colors.text, fontWeight = FontWeight.Medium); Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(colors.border)); Spacer(modifier = Modifier.width(12.dp))
            }
        },
        singleLine = true, textStyle = TextStyle(fontSize = 18.sp, color = colors.text, letterSpacing = 2.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = colors.input, unfocusedContainerColor = colors.input, unfocusedBorderColor = colors.border, focusedBorderColor = colors.accent, cursorColor = colors.accent, focusedLabelColor = colors.accent, errorBorderColor = ErrorRed),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PhoneAuthScreen(auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val colors = LocalNammaColors.current

    var phoneNumber by remember { mutableStateOf("") }
    var lastPhoneNumber by remember { mutableStateOf("") } // ADDED: To track if they changed the number
    var otpCode by remember { mutableStateOf("") }
    var storedVerificationId by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isAuthenticatedLocal by remember { mutableStateOf(false) }

    // Kannada Translation Animation State
    var showKannada by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Wait 3 seconds
            showKannada = !showKannada // Toggle the language
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
        Spacer(modifier = Modifier.height(48.dp))

        if (isAuthenticatedLocal) {
            LaunchedEffect(Unit) {
                delay(2000)
                // Tell the main router to switch to the dashboard!
                onLoginSuccess()
            }
            Text("✅", fontSize = 72.sp); Spacer(modifier = Modifier.height(16.dp))
            Text("Securely Logged In!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Text("You are now logged in. Proceed to book a ride!", textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp), color = colors.subtext)

        } else if (!isOtpSent) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Column {
                    // Static first line
                    Text(
                        text = "Get started",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.text,
                        lineHeight = 40.sp
                    )
                    // Second line: Static "with " + Animated App Name
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(48.dp)) {
                        Text(
                            text = "with ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text,
                            lineHeight = 40.sp
                        )
                        AnimatedContent(
                            targetState = showKannada,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()) togetherWith
                                        (slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "appNameAnimation"
                        ) { isKannada ->
                            Text(
                                text = if (isKannada) "ನಮ್ಮರೈಡ್" else "NammaRide",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.text,
                                lineHeight = 40.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Enter your mobile number to log in or register.", color = colors.subtext, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(40.dp))
                PhoneInputView(phoneNumber = phoneNumber, onPhoneChange = { phoneNumber = it })
                Spacer(modifier = Modifier.height(32.dp))

                if (statusMessage.isNotEmpty()) {
                    val isError = statusMessage.contains("Failed") || statusMessage.contains("Error") || statusMessage.contains("Demo Mode")
                    Text(text = statusMessage, color = if (isError) colors.primary else SuccessGreen, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                }

                Button(
                    onClick = {
                        // 1. PRE-VALIDATION: Check if it's exactly 10 digits
                        if (phoneNumber.trim().length != 10) {
                            Toast.makeText(context, "Please enter a valid 10-digit mobile number.", Toast.LENGTH_SHORT).show()
                            statusMessage = "" // Clear any ugly error text
                            return@Button // Stop the code here, don't call Firebase!
                        }

                        // 2. FIREBASE THROTTLING BYPASS:
                        // If the user hit back, but didn't change their number, jump straight to OTP screen!
                        if (phoneNumber == lastPhoneNumber && storedVerificationId.isNotEmpty()) {
                            isOtpSent = true
                            statusMessage = ""
                            return@Button
                        }

                        // 3. IF VALID & NEW NUMBER: Proceed with Firebase
                        statusMessage = "Sending secure OTP..."
                        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber("+91$phoneNumber").setTimeout(60L, TimeUnit.SECONDS).setActivity(activity)
                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                                override fun onVerificationFailed(e: FirebaseException) {
                                    val errorString = e.message ?: ""
                                    statusMessage = if (errorString.contains("BILLING_NOT_ENABLED") || errorString.contains("internal error")) {
                                        "Please use a registered test number."
                                    } else {
                                        "Verification Failed: ${e.message}"
                                    }
                                }
                                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    storedVerificationId = verificationId
                                    lastPhoneNumber = phoneNumber // Save the successfully pinged number!
                                    isOtpSent = true
                                    statusMessage = ""
                                }
                            }).build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) { Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        } else {

            // Hardware Back Button interceptor to return to Phone Input Screen
            BackHandler {
                isOtpSent = false
                statusMessage = ""
            }

            var showTestBanner by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(2500); showTestBanner = true; delay(4500); showTestBanner = false }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                AnimatedVisibility(visible = showTestBanner) {
                    Surface(color = colors.input, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, colors.accent), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                        Text("💬 SMS: Your OTP for NammaRide is 123456", color = SuccessGreen, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    }
                }
                Text("Verification Code", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = colors.text)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter the 6-digit code sent via SMS at\n${formatMaskedPhone(phoneNumber)}", color = colors.subtext, fontSize = 16.sp, lineHeight = 24.sp)
                Spacer(modifier = Modifier.height(40.dp))
                OtpInputView(otpCode = otpCode, onCodeChange = { otpCode = it })
                Spacer(modifier = Modifier.height(32.dp))

                if (statusMessage.isNotEmpty()) {
                    val isError = statusMessage.contains("Failed") || statusMessage.contains("Invalid") || statusMessage.contains("Error")
                    Text(text = statusMessage, color = if (isError) colors.primary else colors.accent, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                }

                Button(
                    onClick = {
                        // Intercept empty OTP to prevent app crash!
                        if (otpCode.trim().length != 6) {
                            Toast.makeText(context, "Please enter the 6-digit verification code.", Toast.LENGTH_SHORT).show()
                            statusMessage = ""
                            return@Button
                        }

                        statusMessage = "Verifying..."
                        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otpCode)
                        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                statusMessage = "Firebase Verified! Syncing with Database..."
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response = RetrofitClient.api.authenticateUser(AuthRequest(firebase_uid = user!!.uid, phone_number = "+91$phoneNumber"))
                                        withContext(Dispatchers.Main) { isAuthenticatedLocal = true }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            // Prints the exact network error (e.g. IP address failed to connect)
                                            statusMessage = "Database Sync Failed: ${e.message}"
                                        }
                                    }
                                }
                            } else {
                                // Prints exact Firebase failure message
                                statusMessage = "Invalid OTP Code: ${task.exception?.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) { Text("Verify & Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }
}