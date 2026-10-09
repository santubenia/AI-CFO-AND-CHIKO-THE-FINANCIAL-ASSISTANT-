package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.auth.FirebaseAuthDialog
import com.example.data.ExpenseEntity
import com.example.data.PortfolioAssetEntity
import com.example.ui.ExpenseUiState
import com.example.ui.anumati.AnumatiConsentDialog
import com.example.ui.anumati.AnumatiPfEsiResult
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.DateUtils
import com.example.util.LocalActivityData
import com.example.util.LocalActivitySyncManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileHubScreen(
    uiState: ExpenseUiState,
    authManager: AuthManager,
    onAddPortfolioAsset: () -> Unit,
    onEditPortfolioAsset: (PortfolioAssetEntity) -> Unit,
    onDeletePortfolioAsset: (PortfolioAssetEntity) -> Unit,
    onUpdateAnumatiPfEsi: (AnumatiPfEsiResult) -> Unit,
    // History Actions
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onTypeFilterChange: (String?) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    // Settings Actions
    onCurrencySelect: (String) -> Unit,
    onToggleShake: (Boolean) -> Unit,
    onSetShakeSensitivity: (String) -> Unit,
    onSetVibrationMode: (String) -> Unit,
    onSetShakeLaunchMode: (String) -> Unit = {},
    onThemeSelect: (String) -> Unit = {},
    onResetSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    // Cloud Sync & CSV Backup Actions
    onCloudSync: (String) -> Unit = {},
    onExportCsv: () -> Unit = {},
    onImportCsv: (String) -> Unit = {},
    // Passcode Security Action
    onSetNetWorthPasscode: (String) -> Unit = {},
    isSyncing: Boolean = false,
    lastSyncTime: Long = 0L,
    syncMessage: String? = null,
    initialSubTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(initialSubTab) } // 0: Passbook & Net Worth, 1: History, 2: Settings
    var showAuthDialog by remember { mutableStateOf(false) }
    var showAnumatiDialog by remember { mutableStateOf(false) }
    var isPassbookNetWorthHidden by remember { mutableStateOf(true) }
    var showUnlockDialog by remember { mutableStateOf(false) }
    val authUser by authManager.authState.collectAsState()
    val localActivity by LocalActivitySyncManager.activityData.collectAsState()
    val context = LocalContext.current

    var animTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animTrigger = true }

    // Profile morph scale animation
    val avatarScale by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0.85f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "AvatarScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_hub_screen")
    ) {
        // Sub-Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Passbook & Assets", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("History", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Settings", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        when (selectedSubTab) {
            0 -> PassbookSection(
                uiState = uiState,
                isNetWorthHidden = isPassbookNetWorthHidden,
                onToggleNetWorth = {
                    if (isPassbookNetWorthHidden) {
                        // User wants to reveal -> require Passcode (offline) or OTP (online)
                        showUnlockDialog = true
                    } else {
                        // User hides -> hide immediately
                        isPassbookNetWorthHidden = true
                    }
                },
                onAddAsset = onAddPortfolioAsset,
                onEditAsset = onEditPortfolioAsset,
                onDeleteAsset = onDeletePortfolioAsset,
                onOpenAnumati = { showAnumatiDialog = true }
            )
            1 -> HistoryScreen(
                uiState = uiState,
                onSearchChange = onSearchChange,
                onCategoryFilterChange = onCategoryFilterChange,
                onTypeFilterChange = onTypeFilterChange,
                onEditExpense = onEditExpense,
                onDeleteExpense = onDeleteExpense
            )
            2 -> SettingsSection(
                uiState = uiState,
                authUser = authUser,
                localActivity = localActivity,
                avatarScale = avatarScale,
                onOpenAuthDialog = { showAuthDialog = true },
                onSignOut = { authManager.signOut() },
                onCurrencySelect = onCurrencySelect,
                onToggleShake = onToggleShake,
                onSetShakeSensitivity = onSetShakeSensitivity,
                onSetVibrationMode = onSetVibrationMode,
                onSetShakeLaunchMode = onSetShakeLaunchMode,
                onThemeSelect = onThemeSelect,
                onToggleReminders = { LocalActivitySyncManager.setRemindersEnabled(it) },
                onResetSampleData = onResetSampleData,
                onClearAllData = onClearAllData,
                onCloudSync = onCloudSync,
                onExportCsv = onExportCsv,
                onImportCsv = onImportCsv,
                onSetNetWorthPasscode = onSetNetWorthPasscode,
                isSyncing = isSyncing,
                lastSyncTime = lastSyncTime,
                syncMessage = syncMessage
            )
        }
    }

    if (showUnlockDialog) {
        com.example.ui.components.NetWorthUnlockDialog(
            userPasscode = uiState.netWorthPasscode,
            onPasscodeSuccess = {
                isPassbookNetWorthHidden = false
                showUnlockDialog = false
                Toast.makeText(context, "Net Worth Unlocked!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showUnlockDialog = false },
            onChangeSavedPasscode = { newPin ->
                onSetNetWorthPasscode(newPin)
                Toast.makeText(context, "Passcode updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAuthDialog) {
        FirebaseAuthDialog(
            authManager = authManager,
            onDismiss = { showAuthDialog = false }
        )
    }

    if (showAnumatiDialog) {
        AnumatiConsentDialog(
            currencySymbol = uiState.currencySymbol,
            onDismiss = { showAnumatiDialog = false },
            onSuccess = { res ->
                onUpdateAnumatiPfEsi(res)
                Toast.makeText(context, "Accurate EPFO & ESI balances updated via Anumati AA!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
private fun PassbookSection(
    uiState: ExpenseUiState,
    isNetWorthHidden: Boolean,
    onToggleNetWorth: () -> Unit,
    onAddAsset: () -> Unit,
    onEditAsset: (PortfolioAssetEntity) -> Unit,
    onDeleteAsset: (PortfolioAssetEntity) -> Unit,
    onOpenAnumati: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Consolidated Net Worth Hero Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("net_worth_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF064E3B),
                                    Color(0xFF0F766E),
                                    Color(0xFF1E3A8A)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Consolidated Net Worth",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                if (isNetWorthHidden) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "PIN/OTP Protected",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFFA7F3D0), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (!isNetWorthHidden && uiState.totalNetWorth > 0.0) "+14.2% YTD" else "••••% YTD",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isNetWorthHidden) "${uiState.currencySymbol} ★ ★ ★ ★ ★" else DateUtils.formatCurrency(uiState.totalNetWorth, uiState.currencySymbol),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            IconButton(
                                onClick = onToggleNetWorth,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("passbook_net_worth_eye_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isNetWorthHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isNetWorthHidden) "Unlock & Show Net Worth" else "Hide Net Worth",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isNetWorthHidden) "Tap the eye to enter Passcode (offline) or OTP (online)" else "All accounts, investments, EPFO & ESI minus credit liabilities",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Summary Strip
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Banks", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text(if (isNetWorthHidden) "★★★★" else DateUtils.formatCurrency(uiState.totalBankBalance, uiState.currencySymbol), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("Investments", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text(if (isNetWorthHidden) "★★★★" else DateUtils.formatCurrency(uiState.totalInvestments, uiState.currencySymbol), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                                }
                                Column {
                                    Text("EPFO & ESI", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text(if (isNetWorthHidden) "★★★★" else DateUtils.formatCurrency(uiState.totalRetirementGovt, uiState.currencySymbol), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFDE68A))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Liabilities", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text(if (isNetWorthHidden) "★★★★" else "-${DateUtils.formatCurrency(uiState.totalLiabilities, uiState.currencySymbol)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Govt of India Scheme: Anumati AA Sync Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAnumati)
                    .testTag("anumati_sync_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F766E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Anumati AA Sync", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = IncomeGreen.copy(alpha = 0.2f)) {
                                    Text("Govt. Scheme", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Text("Fetch verified real-time PF & ESI wage records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = onOpenAnumati,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Connect", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Section Header & Add Account Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Passbook & Asset Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.allPortfolioAssets.size} active passbooks & portfolios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onAddAsset)
                        .testTag("add_asset_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Account", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Asset Cards List
        items(uiState.allPortfolioAssets, key = { it.id }) { asset ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (asset.category) {
                                        "CREDIT_CARD" -> ExpenseRed.copy(alpha = 0.15f)
                                        "MUTUAL_FUND", "STOCK_INDIAN", "STOCK_FOREIGN" -> IncomeGreen.copy(alpha = 0.15f)
                                        "EPFO", "ESI" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (asset.category) {
                                    "CREDIT_CARD" -> Icons.Default.CreditCard
                                    "MUTUAL_FUND", "STOCK_INDIAN", "STOCK_FOREIGN" -> Icons.Default.ShowChart
                                    "EPFO", "ESI" -> Icons.Default.Security
                                    else -> Icons.Default.AccountBalance
                                },
                                contentDescription = null,
                                tint = when (asset.category) {
                                    "CREDIT_CARD" -> ExpenseRed
                                    "MUTUAL_FUND", "STOCK_INDIAN", "STOCK_FOREIGN" -> IncomeGreen
                                    "EPFO", "ESI" -> Color(0xFFD97706)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = asset.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (asset.notes.contains("Anumati")) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = IncomeGreen, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = "${asset.institution} ${if (asset.accountNumberMasked.isNotBlank()) "• ${asset.accountNumberMasked}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = (if (asset.isLiability) "- " else "") + DateUtils.formatCurrency(asset.balanceOrValue, uiState.currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (asset.isLiability) ExpenseRed else MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onEditAsset(asset) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { onDeleteAsset(asset) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = ExpenseRed.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsSection(
    uiState: ExpenseUiState,
    authUser: com.example.auth.AuthUserState,
    localActivity: LocalActivityData,
    avatarScale: Float,
    onOpenAuthDialog: () -> Unit,
    onSignOut: () -> Unit,
    onCurrencySelect: (String) -> Unit,
    onToggleShake: (Boolean) -> Unit,
    onSetShakeSensitivity: (String) -> Unit,
    onSetVibrationMode: (String) -> Unit,
    onSetShakeLaunchMode: (String) -> Unit,
    onThemeSelect: (String) -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    onResetSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    onCloudSync: (String) -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: (String) -> Unit,
    onSetNetWorthPasscode: (String) -> Unit,
    isSyncing: Boolean,
    lastSyncTime: Long,
    syncMessage: String?
) {
    val context = LocalContext.current
    var isEditingPasscodeInSettings by remember { mutableStateOf(false) }
    var newSettingsPasscode by remember { mutableStateOf("") }
    val currencies = listOf("₹" to "INR (₹)", "$" to "USD ($)", "€" to "EUR (€)", "£" to "GBP (£)", "¥" to "JPY (¥)", "C$" to "CAD (C$)")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Firebase Account Card with Avatar Morph Animation
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("firebase_account_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .scale(avatarScale)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, Color(0xFF0D9488))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = authUser.displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = authUser.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (authUser.isLoggedIn && !authUser.isAnonymous) IncomeGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (authUser.isLoggedIn && !authUser.isAnonymous) "Firebase Auth" else "Offline Guest",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (authUser.isLoggedIn && !authUser.isAnonymous) IncomeGreen else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(text = authUser.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                if (authUser.isLoggedIn && !authUser.isAnonymous) {
                                    onSignOut()
                                } else {
                                    onOpenAuthDialog()
                                }
                            }
                    ) {
                        Text(
                            text = if (authUser.isLoggedIn && !authUser.isAnonymous) "Log Out" else "Log In / Sign Up",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Firebase Firestore Multi-Device Cloud Sync Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("firestore_cloud_sync_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Firebase Firestore Cloud Sync", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Multi-device persistence & automatic cloud backup", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (lastSyncTime > 0) {
                        Text(
                            text = "Last Synced: ${DateUtils.formatDateTime(lastSyncTime)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = IncomeGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (syncMessage != null) {
                        Text(
                            text = syncMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (authUser.isLoggedIn && !authUser.isAnonymous) {
                                onCloudSync(authUser.uid)
                            } else {
                                onOpenAuthDialog()
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing with Firestore...", style = MaterialTheme.typography.labelSmall)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (authUser.isLoggedIn && !authUser.isAnonymous) "Sync with Cloud Now" else "Sign in to Sync with Cloud", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // CSV Import & Export Backup Card
        item {
            var showImportDialog by remember { mutableStateOf(false) }
            var csvInputText by remember { mutableStateOf("") }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("csv_backup_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Account Data CSV Backup", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Export and restore account ledger data via CSV", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExportCsv,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import CSV", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (showImportDialog) {
                Dialog(onDismissRequest = { showImportDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Import Transactions CSV", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Paste CSV data exported from CoE or formatted as ID,Amount,Category,Date,Description,Type", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = csvInputText,
                                onValueChange = { csvInputText = it },
                                label = { Text("CSV Text") },
                                placeholder = { Text("ID,Amount,Category,DateMillis,FormattedDate,Description,Type...") },
                                minLines = 4,
                                maxLines = 7,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = { showImportDialog = false }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (csvInputText.isNotBlank()) {
                                            onImportCsv(csvInputText)
                                            showImportDialog = false
                                            Toast.makeText(context, "CSV Import initiated!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("Import Now")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Local Device Steps Sync (Debugged & Replaced Google Fit)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("local_activity_sync_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Local Device Activity Sync", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Pedometer & reminder notifications without external cloud", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Today's Steps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("${localActivity.steps} steps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Calories Burned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("${localActivity.calories} kcal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Daily Wealth & Health Reminder", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = localActivity.remindersEnabled, onCheckedChange = onToggleReminders)
                    }
                }
            }
        }

        // Shake to Log Settings (Battery-optimized: default off in background, vibration control, overlay permission)
        item {
            val hasOverlayPermission = remember(context) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("shake_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Always Run Shake in Background", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Off by default for battery savings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Switch(checked = uiState.isShakeToLogEnabled, onCheckedChange = onToggleShake)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Background Launch Mode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Text("Control how shake triggers when the app is in background or closed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.shakeLaunchMode == "FIRST_TIME_ONLY",
                            onClick = { onSetShakeLaunchMode("FIRST_TIME_ONLY") },
                            label = { Text("First shake after app closed (Default)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = uiState.shakeLaunchMode == "CONTINUOUS",
                            onClick = { onSetShakeLaunchMode("CONTINUOUS") },
                            label = { Text("Every shake", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Full Screen & Overlay Launch Permission", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Text("Required by Android to pop up shake logger seamlessly over other apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasOverlayPermission) Icons.Default.CheckCircle else Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (hasOverlayPermission) IncomeGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (hasOverlayPermission) "Overlay Permission Active" else "Permission Setup Needed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasOverlayPermission) IncomeGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (hasOverlayPermission) "Seamless background shake logger enabled" else "Tap below to enable full screen launch",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    if (!hasOverlayPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Open System Settings -> Apps -> CoE -> Display over other apps", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Grant Full Screen / Overlay Permission", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Haptic Feedback / Vibration Speed", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Text("Slow down or remove vibration to maximize battery savings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Off", "Soft (Battery Saver)", "Normal").forEach { mode ->
                            FilterChip(
                                selected = uiState.shakeSensitivity == mode || (mode == "Soft (Battery Saver)" && uiState.shakeSensitivity !in listOf("Normal", "Off")),
                                onClick = { onSetVibrationMode(mode) },
                                label = { Text(if (mode == "Off") "Off (Battery Saver)" else mode, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Net Worth Privacy & Passcode Setting
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("net_worth_security_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Net Worth Privacy Shield", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Hidden by default with stars on launch", style = MaterialTheme.typography.bodySmall, color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Every time you launch the app, Net Worth is masked with stars. Viewing it requires entering your offline Passcode or a 6-digit online OTP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Current Offline Passcode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("•••• (Saved)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { isEditingPasscodeInSettings = !isEditingPasscodeInSettings },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEditingPasscodeInSettings) "Close" else "Change Passcode", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    if (isEditingPasscodeInSettings) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newSettingsPasscode,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    newSettingsPasscode = it
                                }
                            },
                            label = { Text("Enter New Passcode (4-6 Digits)") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isEditingPasscodeInSettings = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newSettingsPasscode.length >= 4) {
                                        onSetNetWorthPasscode(newSettingsPasscode)
                                        Toast.makeText(context, "New passcode saved successfully!", Toast.LENGTH_SHORT).show()
                                        isEditingPasscodeInSettings = false
                                        newSettingsPasscode = ""
                                    }
                                },
                                enabled = newSettingsPasscode.length >= 4
                            ) {
                                Text("Save Passcode")
                            }
                        }
                    }
                }
            }
        }

        // Currency Setting
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Primary Display Currency", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Used for net worth, sales diary, and reports", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currencies.forEach { (symbol, label) ->
                            FilterChip(
                                selected = uiState.currencySymbol == symbol,
                                onClick = { onCurrencySelect(symbol) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Theme & Styling Setting
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("theme_selection_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Theme & Display Appearance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Select your preferred visual style and color scheme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val themes = listOf(
                            "SYSTEM" to "System Default",
                            "DARK" to "OLED Dark Mode",
                            "LIGHT" to "Executive Light",
                            "GOLD_EMERALD" to "CoE Gold & Emerald",
                            "CYBER_VIOLET" to "Cyberpunk Neon"
                        )
                        themes.forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.selectedTheme == key,
                                onClick = { onThemeSelect(key) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Data Reset / Clear Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onResetSampleData,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset All Data (Clean Slate)")
                }

                Button(
                    onClick = onClearAllData,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.1f), contentColor = ExpenseRed),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All Data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
