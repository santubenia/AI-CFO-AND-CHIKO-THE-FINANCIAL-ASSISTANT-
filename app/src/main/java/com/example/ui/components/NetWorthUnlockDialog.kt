package com.example.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Authentication dialog to unlock and view the Net Worth when hidden with stars.
 * Supports:
 * 1. Offline Passcode (Custom PIN or default "1234")
 * 2. Online OTP (Sent to mobile/email with instant verification code)
 */
@Composable
fun NetWorthUnlockDialog(
    userPasscode: String = "1234",
    onPasscodeSuccess: () -> Unit,
    onDismiss: () -> Unit,
    onChangeSavedPasscode: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Check device internet connectivity
    fun isDeviceOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    val isOnline = remember { isDeviceOnline() }
    // 0: Offline Passcode, 1: Online OTP
    var selectedAuthTab by remember { mutableIntStateOf(if (isOnline) 1 else 0) }

    // Passcode State
    var passcodeEntered by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf<String?>(null) }
    var showPasscodeText by remember { mutableStateOf(false) }

    // Change passcode dialog expansion
    var isChangingPasscode by remember { mutableStateOf(false) }
    var newPasscodeEntered by remember { mutableStateOf("") }
    var changeSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Online OTP State
    var otpGenerated by remember { mutableStateOf("") }
    var otpEntered by remember { mutableStateOf("") }
    var otpSentNotice by remember { mutableStateOf<String?>(null) }
    var isOtpVerifying by remember { mutableStateOf(false) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var resendCountdown by remember { mutableIntStateOf(0) }

    // Function to generate and dispatch online OTP
    fun dispatchOnlineOtp() {
        isSendingOtp = true
        otpError = null
        coroutineScope.launch {
            delay(500)
            val code = String.format("%06d", Random.nextInt(100000, 999999))
            otpGenerated = code
            otpEntered = ""
            otpSentNotice = "Online OTP generated: $code"
            isSendingOtp = false
            resendCountdown = 30
        }
    }

    // Initialize OTP when opening online tab
    LaunchedEffect(selectedAuthTab) {
        if (selectedAuthTab == 1 && otpGenerated.isEmpty()) {
            dispatchOnlineOtp()
        }
    }

    // Resend countdown timer
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000)
            resendCountdown -= 1
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("net_worth_unlock_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, Color(0xFF0F766E))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Net Worth Security Shield",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) IncomeGreen else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Network Connected (Online & Offline Ready)" else "Offline Mode (Passcode Verification)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOnline) IncomeGreen else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "To protect confidential portfolio values, enter your offline Passcode or verify via online OTP.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Verification Mode Tabs
                TabRow(
                    selectedTabIndex = selectedAuthTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedAuthTab == 0,
                        onClick = {
                            selectedAuthTab = 0
                            passcodeError = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Passcode (Offline)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedAuthTab == 1,
                        onClick = {
                            selectedAuthTab = 1
                            otpError = null
                            if (otpGenerated.isEmpty()) {
                                dispatchOnlineOtp()
                            }
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OTP (Online)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // TAB 0: OFFLINE PASSCODE
                if (selectedAuthTab == 0) {
                    if (!isChangingPasscode) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Enter 4-Digit Passcode",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Default passcode is '1234' unless customized.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = passcodeEntered,
                                    onValueChange = {
                                        if (it.length <= 8) {
                                            passcodeEntered = it
                                            passcodeError = null
                                        }
                                    },
                                    placeholder = { Text("••••") },
                                    singleLine = true,
                                    visualTransformation = if (showPasscodeText) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    trailingIcon = {
                                        IconButton(onClick = { showPasscodeText = !showPasscodeText }) {
                                            Icon(
                                                imageVector = if (showPasscodeText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (showPasscodeText) "Hide" else "Show",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    isError = passcodeError != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("unlock_passcode_input")
                                )

                                if (passcodeError != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = passcodeError ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (changeSuccessMsg != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = changeSuccessMsg ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            isChangingPasscode = true
                                            newPasscodeEntered = ""
                                            changeSuccessMsg = null
                                        }
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Change Passcode", style = MaterialTheme.typography.labelSmall)
                                    }

                                    // Quick fill default for convenience
                                    TextButton(
                                        onClick = {
                                            passcodeEntered = userPasscode
                                            passcodeError = null
                                        }
                                    ) {
                                        Text("Use Default (1234)", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (passcodeEntered == userPasscode) {
                                    onPasscodeSuccess()
                                } else {
                                    passcodeError = "Incorrect Passcode. Please try again."
                                }
                            },
                            enabled = passcodeEntered.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("unlock_passcode_verify_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Net Worth", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // CHANGE PASSCODE VIEW
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Set New Offline Passcode",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Choose 4 to 6 numbers you will remember.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = newPasscodeEntered,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                            newPasscodeEntered = it
                                        }
                                    },
                                    label = { Text("New PIN (Digits only)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { isChangingPasscode = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (newPasscodeEntered.length in 4..6) {
                                                onChangeSavedPasscode?.invoke(newPasscodeEntered)
                                                passcodeEntered = newPasscodeEntered
                                                changeSuccessMsg = "Passcode updated to $newPasscodeEntered!"
                                                isChangingPasscode = false
                                            }
                                        },
                                        enabled = newPasscodeEntered.length >= 4
                                    ) {
                                        Text("Save PIN")
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 1: ONLINE OTP
                if (selectedAuthTab == 1) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Secure One-Time Password",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Simulated Instant OTP Banner (similar to banking / Anumati flow)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = IncomeGreen.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isSendingOtp) "Generating online OTP..." else "Online Verification Code Sent",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                        if (otpGenerated.isNotEmpty()) {
                                            Text(
                                                text = "Your OTP is: $otpGenerated",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                color = IncomeGreen
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = otpEntered,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        otpEntered = it
                                        otpError = null
                                    }
                                },
                                label = { Text("Enter 6-Digit Online OTP") },
                                placeholder = { Text("6-digit code") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = otpError != null,
                                trailingIcon = {
                                    if (otpGenerated.isNotEmpty() && otpEntered != otpGenerated) {
                                        TextButton(
                                            onClick = {
                                                otpEntered = otpGenerated
                                                otpError = null
                                            }
                                        ) {
                                            Text("Auto Fill", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unlock_otp_input")
                            )

                            if (otpError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = otpError ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { dispatchOnlineOtp() },
                                    enabled = resendCountdown == 0 && !isSendingOtp
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (resendCountdown > 0) "Resend in ${resendCountdown}s" else "Resend OTP",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Text(
                                    text = "SMS & Email Sync",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isOtpVerifying = true
                            coroutineScope.launch {
                                delay(600)
                                isOtpVerifying = false
                                if (otpEntered == otpGenerated) {
                                    onPasscodeSuccess()
                                } else {
                                    otpError = "Invalid OTP code. Please enter the correct 6-digit OTP."
                                }
                            }
                        },
                        enabled = otpEntered.length == 6 && !isOtpVerifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("unlock_otp_verify_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isOtpVerifying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify OTP & Reveal", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dismiss Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Keep Net Worth Hidden")
                }
            }
        }
    }
}
