package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.data.ExpenseEntity
import com.example.data.PortfolioAssetEntity
import com.example.data.SaleEntity
import com.example.service.CoEShakeService
import com.example.ui.ExpenseViewModel
import com.example.model.BudgetPeriod
import com.example.model.Categories
import com.example.ui.chiko.ChikoCfoSheet
import com.example.ui.components.AddEditExpenseDialog
import com.example.ui.components.AddEditPortfolioAssetDialog
import com.example.ui.components.AddEditSaleDialog
import com.example.ui.components.BudgetEditDialog
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.FuturisticBottomBar
import com.example.ui.components.QuickShakeLogDialog
import com.example.ui.components.QuickShakeLogType
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DailyScreen
import com.example.ui.screens.ProfileHubScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.DateUtils
import com.example.util.LocalActivitySyncManager
import com.example.util.NotificationHelper
import com.example.util.ShakeDetector
import java.util.Calendar
import kotlin.math.abs

enum class MainTab {
    EXPENSES,
    SELLS,
    BUDGET,
    PROFILE
}

class MainActivity : ComponentActivity() {

    private var triggerShakeOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getBooleanExtra("EXTRA_TRIGGER_SHAKE", false) == true) {
            triggerShakeOnResume = true
        }

        setContent {
            val viewModel: ExpenseViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(themeMode = uiState.selectedTheme) {
                ExpenseApp(
                    viewModel = viewModel,
                    initialShakeTrigger = triggerShakeOnResume
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("EXTRA_TRIGGER_SHAKE", false)) {
            triggerShakeOnResume = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseApp(
    viewModel: ExpenseViewModel = viewModel(),
    initialShakeTrigger: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(MainTab.EXPENSES) }

    // Sub-page states for sweep gestures
    var expensesSubPage by remember { mutableIntStateOf(0) } // 0: Daily Expenses, 1: Expenses Analysis
    var sellsSubPage by remember { mutableIntStateOf(0) } // 0: Sell's Diary, 1: Sell's Analysis

    // Hide / Show Eye option for Net Worth (hidden by default with stars on launch)
    var isNetWorthHidden by remember { mutableStateOf(true) }
    var showNetWorthUnlockDialog by remember { mutableStateOf(false) }

    // Chiko AI CFO Sheet
    var showChikoSheet by remember { mutableStateOf(false) }

    // Dialog States
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var deletingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showAddSaleDialog by remember { mutableStateOf(false) }
    var editingSale by remember { mutableStateOf<SaleEntity?>(null) }
    var deletingSale by remember { mutableStateOf<SaleEntity?>(null) }

    var showAddPortfolioDialog by remember { mutableStateOf(false) }
    var editingPortfolioAsset by remember { mutableStateOf<PortfolioAssetEntity?>(null) }
    var deletingPortfolioAsset by remember { mutableStateOf<PortfolioAssetEntity?>(null) }

    // Shake Logger Window Pop-up
    var showQuickShakeDialog by remember { mutableStateOf(initialShakeTrigger) }

    var editingBudgetCategory by remember { mutableStateOf<String?>(null) }
    var editingBudgetLimit by remember { mutableStateOf(0.0) }
    var editingBudgetPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var editingBudgetThreshold by remember { mutableStateOf(80.0) }
    val notifiedNearBudgets = remember { androidx.compose.runtime.mutableStateListOf<String>() }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    // Dynamic Time-of-Day logo gradient colors
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (logoColor1, logoColor2) = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> Color(0xFFF59E0B) to Color(0xFF10B981) // Morning: Gold Sunrise & Emerald
            in 12..17 -> Color(0xFF10B981) to Color(0xFF0D9488) // Afternoon: Royal Emerald & Teal
            else -> Color(0xFF06B6D4) to Color(0xFF8B5CF6) // Evening / Night: Cyan & Mystic Violet
        }
    }

    // Initialize Local Activity Sync
    LaunchedEffect(Unit) {
        LocalActivitySyncManager.init(context)
    }

    // Request Notification permission on Android 13+ (Tiramisu+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Background Shake Service (only when enabled by user)
    LaunchedEffect(uiState.isShakeToLogEnabled, uiState.shakeSensitivity, uiState.shakeLaunchMode) {
        try {
            val serviceIntent = Intent(context, CoEShakeService::class.java).apply {
                putExtra("VIBRATION_MODE", uiState.shakeSensitivity)
                putExtra("LAUNCH_MODE", uiState.shakeLaunchMode)
                putExtra("RESET_LAUNCH_FLAG", true)
            }
            if (uiState.isShakeToLogEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                context.stopService(serviceIntent)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to toggle CoEShakeService", e)
        }
    }

    // Interactive Foreground Shake Detector
    val shakeDetector = remember {
        ShakeDetector(context) {
            showQuickShakeDialog = true
        }
    }

    DisposableEffect(Unit) {
        shakeDetector.isEnabled = true
        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
        }
    }

    // Budget exceeded notification triggers
    LaunchedEffect(uiState.exceededBudgetWarnings) {
        uiState.exceededBudgetWarnings.forEach { ws ->
            NotificationHelper.showBudgetExceededNotification(
                context = context,
                category = ws.category,
                spent = ws.spent,
                limit = ws.limit,
                currencySymbol = uiState.currencySymbol,
                periodName = ws.period.title
            )
        }
    }

    // Proactive Nearing Budget Limit notification triggers (alerts when nearing spending limits)
    LaunchedEffect(uiState.nearBudgetWarnings) {
        uiState.nearBudgetWarnings.forEach { ws ->
            val key = "${ws.category}_${ws.period.id}_${(ws.percentage * 10).toInt()}"
            if (!notifiedNearBudgets.contains(key)) {
                notifiedNearBudgets.add(key)
                NotificationHelper.showNearBudgetLimitNotification(
                    context = context,
                    category = ws.category,
                    spent = ws.spent,
                    limit = ws.limit,
                    percentage = ws.percentage,
                    currencySymbol = uiState.currencySymbol,
                    periodName = ws.period.title
                )
            }
        }
    }

    // Back handler: pop sub-pages or return to EXPENSES
    BackHandler(enabled = expensesSubPage != 0 || sellsSubPage != 0 || selectedTab != MainTab.EXPENSES) {
        if (expensesSubPage != 0) {
            expensesSubPage = 0
        } else if (sellsSubPage != 0) {
            sellsSubPage = 0
        } else {
            selectedTab = MainTab.EXPENSES
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedTab = MainTab.PROFILE }
                            .padding(vertical = 4.dp)
                            .testTag("top_left_net_worth_container")
                    ) {
                        // Dynamic Time & Theme App Logo (Slightly bigger: 44dp)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(logoColor1, logoColor2))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_coe_logo),
                                contentDescription = "CoE Logo",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CoE Net Worth",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (!isNetWorthHidden && uiState.totalNetWorth > 0.0) IncomeGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (!isNetWorthHidden && uiState.totalNetWorth > 0.0) "+14.2%" else "••••%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isNetWorthHidden && uiState.totalNetWorth > 0.0) IncomeGreen else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isNetWorthHidden) "${uiState.currencySymbol} ★ ★ ★ ★ ★" else DateUtils.formatCurrency(uiState.totalNetWorth, uiState.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // Security Eye Option to show or hide net worth
                                IconButton(
                                    onClick = {
                                        if (isNetWorthHidden) {
                                            // Every time user wants to view: authenticate via Passcode (offline) or OTP (online)
                                            showNetWorthUnlockDialog = true
                                        } else {
                                            // Lock back into stars immediately
                                            isNetWorthHidden = true
                                        }
                                    },
                                    modifier = Modifier.size(24.dp).testTag("net_worth_eye_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isNetWorthHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isNetWorthHidden) "Unlock & View Net Worth" else "Hide Net Worth",
                                        tint = if (isNetWorthHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Chiko AI CFO Button in Top-Right
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showChikoSheet = true }
                            .testTag("chiko_cfo_top_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Chiko (CFO)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            FuturisticBottomBar(
                currentTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onLogClick = { showQuickShakeDialog = true }
            )
        }
    ) { innerPadding ->
        // Gesture Navigation:
        // Sweep DOWN (instead of sweep left) opens Chiko CFO chatbot from any screen except Profile!
        // Sweep LEFT switches between main tabs (Expenses -> Sells -> Budget -> Profile)
        // Sweep RIGHT switches back (Profile -> Budget -> Sells -> Expenses)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(selectedTab) {
                    var totalDragX = 0f
                    var totalDragY = 0f
                    detectDragGestures(
                        onDragEnd = {
                            if (totalDragY > 150f && abs(totalDragY) > abs(totalDragX) * 1.2f) {
                                // Sweep DOWN -> Open Chiko CFO (except when on Profile page)
                                if (selectedTab != MainTab.PROFILE) {
                                    showChikoSheet = true
                                }
                            } else if (totalDragX < -140f && abs(totalDragX) > abs(totalDragY)) {
                                // Sweep LEFT -> Switch to next tab
                                selectedTab = when (selectedTab) {
                                    MainTab.EXPENSES -> MainTab.SELLS
                                    MainTab.SELLS -> MainTab.BUDGET
                                    MainTab.BUDGET -> MainTab.PROFILE
                                    MainTab.PROFILE -> MainTab.EXPENSES
                                }
                            } else if (totalDragX > 140f && abs(totalDragX) > abs(totalDragY)) {
                                // Sweep RIGHT -> Switch to previous tab
                                selectedTab = when (selectedTab) {
                                    MainTab.EXPENSES -> MainTab.PROFILE
                                    MainTab.SELLS -> MainTab.EXPENSES
                                    MainTab.BUDGET -> MainTab.SELLS
                                    MainTab.PROFILE -> MainTab.BUDGET
                                }
                            }
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { _, dragAmount ->
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        }
                    )
                }
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut())
                },
                label = "MainTabTransition"
            ) { tab ->
                when (tab) {
                    MainTab.EXPENSES -> {
                        if (expensesSubPage == 0) {
                            DailyScreen(
                                uiState = uiState,
                                onPreviousDay = { viewModel.changeDay(-1) },
                                onNextDay = { viewModel.changeDay(1) },
                                onSelectDate = { viewModel.setSelectedDate(it) },
                                onAddQuickPreset = { preset ->
                                    viewModel.addExpense(
                                        title = preset.title,
                                        amount = preset.amount,
                                        category = preset.category,
                                        paymentMethod = preset.paymentMethod,
                                        timestamp = uiState.selectedDate
                                    )
                                },
                                onOpenAddDialog = {
                                    editingExpense = null
                                    showAddExpenseDialog = true
                                },
                                onEditExpense = { expense ->
                                    editingExpense = expense
                                    showAddExpenseDialog = true
                                },
                                onDeleteExpense = { expense ->
                                    deletingExpense = expense
                                },
                                onSimulateShake = {
                                    shakeDetector.simulateShake()
                                },
                                onToggleShake = { enabled ->
                                    viewModel.toggleShakeToLog(enabled)
                                }
                            )
                        } else {
                            AnalyticsScreen(
                                uiState = uiState,
                                onTimeRangeChange = { range -> viewModel.setTimeRange(range) }
                            )
                        }
                    }

                    MainTab.SELLS -> {
                        if (sellsSubPage == 0) {
                            SalesScreen(
                                uiState = uiState,
                                onPreviousDay = { viewModel.changeDay(-1) },
                                onNextDay = { viewModel.changeDay(1) },
                                onSelectDate = { viewModel.setSelectedDate(it) },
                                onAddQuickSale = { preset ->
                                    viewModel.addSale(
                                        productName = preset.productName,
                                        quantity = 1,
                                        sellingPrice = preset.sellingPrice,
                                        costPrice = preset.costPrice,
                                        date = uiState.selectedDate
                                    )
                                },
                                onOpenAddSaleDialog = {
                                    editingSale = null
                                    showAddSaleDialog = true
                                },
                                onEditSale = { sale ->
                                    editingSale = sale
                                    showAddSaleDialog = true
                                },
                                onDeleteSale = { sale ->
                                    deletingSale = sale
                                },
                                onSimulateShake = {
                                    shakeDetector.simulateShake()
                                },
                                onToggleShake = { enabled ->
                                    viewModel.toggleShakeToLog(enabled)
                                }
                            )
                        } else {
                            AnalyticsScreen(
                                uiState = uiState,
                                onTimeRangeChange = { range -> viewModel.setTimeRange(range) }
                            )
                        }
                    }

                    MainTab.BUDGET -> {
                        BudgetsScreen(
                            uiState = uiState,
                            onPeriodChange = { period -> viewModel.setSelectedBudgetPeriod(period) },
                            onEditBudget = { category, limit, period, threshold ->
                                editingBudgetCategory = category
                                editingBudgetLimit = limit
                                editingBudgetPeriod = period
                                editingBudgetThreshold = threshold
                            },
                            onResetSampleData = { viewModel.resetSampleData() },
                            onClearAllData = { showClearConfirmation = true },
                            onOpenChiko = { showChikoSheet = true }
                        )
                    }

                    MainTab.PROFILE -> {
                        ProfileHubScreen(
                            uiState = uiState,
                            authManager = authManager,
                            onAddPortfolioAsset = {
                                editingPortfolioAsset = null
                                showAddPortfolioDialog = true
                            },
                            onEditPortfolioAsset = { asset ->
                                editingPortfolioAsset = asset
                                showAddPortfolioDialog = true
                            },
                            onDeletePortfolioAsset = { asset ->
                                deletingPortfolioAsset = asset
                            },
                            onUpdateAnumatiPfEsi = { res ->
                                viewModel.updateAnumatiPfEsi(res)
                            },
                            onSearchChange = { query -> viewModel.setSearchQuery(query) },
                            onCategoryFilterChange = { cat -> viewModel.setCategoryFilter(cat) },
                            onTypeFilterChange = { type -> viewModel.setTypeFilter(type) },
                            onEditExpense = { exp ->
                                editingExpense = exp
                                showAddExpenseDialog = true
                            },
                            onDeleteExpense = { exp ->
                                deletingExpense = exp
                            },
                            onCurrencySelect = { symbol -> viewModel.setCurrency(symbol) },
                            onToggleShake = { enabled -> viewModel.toggleShakeToLog(enabled) },
                            onSetShakeSensitivity = { sens -> viewModel.setShakeSensitivity(sens) },
                            onSetVibrationMode = { mode -> viewModel.setShakeSensitivity(mode) },
                            onSetShakeLaunchMode = { mode -> viewModel.setShakeLaunchMode(mode) },
                            onThemeSelect = { theme -> viewModel.setTheme(theme) },
                            onResetSampleData = { viewModel.resetSampleData() },
                            onClearAllData = { showClearConfirmation = true },
                            onCloudSync = { userId -> viewModel.syncWithCloud(userId) },
                            onExportCsv = { viewModel.exportTransactionsCsv(context) },
                            onImportCsv = { csv -> viewModel.importTransactionsCsv(csv) { /* completed */ } },
                            onSetNetWorthPasscode = { pin -> viewModel.setNetWorthPasscode(pin) },
                            isSyncing = isSyncing,
                            lastSyncTime = lastSyncTime,
                            syncMessage = syncMessage
                        )
                    }
                }
            }
        }
    }

    // Chiko Grand AI CFO Bottom Sheet (Online Gemini AI + World Market News)
    if (showChikoSheet) {
        ChikoCfoSheet(
            uiState = uiState,
            netWorth = uiState.totalNetWorth,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { showChikoSheet = false },
            onAddPortfolioOpportunity = { opp ->
                viewModel.addPortfolioAsset(
                    name = opp.title,
                    category = if (opp.market.contains("Indian")) "STOCK_INDIAN" else "STOCK_FOREIGN",
                    balanceOrValue = 25000.0,
                    investedAmount = 25000.0,
                    institution = "AI Growth Portfolio",
                    notes = opp.thesis
                )
                showChikoSheet = false
                selectedTab = MainTab.PROFILE
            }
        )
    }

    // Quick Shake Logger Window
    if (showQuickShakeDialog) {
        QuickShakeLogDialog(
            currencySymbol = uiState.currencySymbol,
            onDismiss = { showQuickShakeDialog = false },
            onSaveSale = { sale, openFullApp ->
                viewModel.addSale(
                    productName = sale.productName,
                    quantity = sale.quantity,
                    sellingPrice = sale.sellingPrice,
                    costPrice = sale.costPrice,
                    date = sale.date,
                    customerName = sale.customerName,
                    paymentMethod = sale.paymentMethod,
                    notes = sale.notes
                )
                showQuickShakeDialog = false
                if (openFullApp) {
                    selectedTab = MainTab.SELLS
                }
            },
            onSaveExpense = { expense, openFullApp ->
                viewModel.addExpense(
                    title = expense.title,
                    amount = expense.amount,
                    category = expense.category,
                    type = expense.type,
                    timestamp = expense.timestamp,
                    paymentMethod = expense.paymentMethod,
                    note = expense.note,
                    tag = expense.tag
                )
                showQuickShakeDialog = false
                if (openFullApp) {
                    selectedTab = MainTab.EXPENSES
                }
            },
            onOpenFullAppDirectly = { logType ->
                showQuickShakeDialog = false
                selectedTab = if (logType == QuickShakeLogType.SALE) MainTab.SELLS else MainTab.EXPENSES
            }
        )
    }

    // Add or Edit Expense Dialog
    if (showAddExpenseDialog) {
        AddEditExpenseDialog(
            initialExpense = editingExpense,
            defaultDate = uiState.selectedDate,
            currencySymbol = uiState.currencySymbol,
            onDismiss = {
                showAddExpenseDialog = false
                editingExpense = null
            },
            onSave = { expense ->
                if (editingExpense == null) {
                    viewModel.addExpense(
                        title = expense.title,
                        amount = expense.amount,
                        category = expense.category,
                        type = expense.type,
                        timestamp = expense.timestamp,
                        paymentMethod = expense.paymentMethod,
                        note = expense.note,
                        tag = expense.tag
                    )
                } else {
                    viewModel.updateExpense(expense)
                }
                showAddExpenseDialog = false
                editingExpense = null
            }
        )
    }

    // Add or Edit Sale Dialog
    if (showAddSaleDialog) {
        AddEditSaleDialog(
            initialSale = editingSale,
            currencySymbol = uiState.currencySymbol,
            onDismiss = {
                showAddSaleDialog = false
                editingSale = null
            },
            onSave = { sale ->
                if (editingSale == null) {
                    viewModel.addSale(
                        productName = sale.productName,
                        quantity = sale.quantity,
                        sellingPrice = sale.sellingPrice,
                        costPrice = sale.costPrice,
                        customerName = sale.customerName,
                        paymentMethod = sale.paymentMethod,
                        notes = sale.notes
                    )
                } else {
                    viewModel.updateSale(sale)
                }
                showAddSaleDialog = false
                editingSale = null
            }
        )
    }

    // Add or Edit Portfolio Asset Dialog
    if (showAddPortfolioDialog) {
        AddEditPortfolioAssetDialog(
            initialAsset = editingPortfolioAsset,
            currencySymbol = uiState.currencySymbol,
            onDismiss = {
                showAddPortfolioDialog = false
                editingPortfolioAsset = null
            },
            onSave = { asset ->
                if (editingPortfolioAsset == null) {
                    viewModel.addPortfolioAsset(
                        name = asset.name,
                        category = asset.category,
                        balanceOrValue = asset.balanceOrValue,
                        investedAmount = asset.investedAmount,
                        institution = asset.institution,
                        accountNumberMasked = asset.accountNumberMasked,
                        notes = asset.notes
                    )
                } else {
                    viewModel.updatePortfolioAsset(asset)
                }
                showAddPortfolioDialog = false
                editingPortfolioAsset = null
            }
        )
    }

    // Delete Confirmation Dialogs
    if (deletingExpense != null) {
        DeleteConfirmationDialog(
            itemTitle = deletingExpense!!.title,
            onConfirm = {
                viewModel.deleteExpense(deletingExpense!!)
                deletingExpense = null
            },
            onDismiss = { deletingExpense = null }
        )
    }

    if (deletingSale != null) {
        DeleteConfirmationDialog(
            itemTitle = deletingSale!!.productName,
            onConfirm = {
                viewModel.deleteSale(deletingSale!!)
                deletingSale = null
            },
            onDismiss = { deletingSale = null }
        )
    }

    if (deletingPortfolioAsset != null) {
        DeleteConfirmationDialog(
            itemTitle = deletingPortfolioAsset!!.name,
            onConfirm = {
                viewModel.deletePortfolioAsset(deletingPortfolioAsset!!)
                deletingPortfolioAsset = null
            },
            onDismiss = { deletingPortfolioAsset = null }
        )
    }

    // Edit Budget Limit Dialog
    if (editingBudgetCategory != null) {
        BudgetEditDialog(
            categoryName = editingBudgetCategory!!,
            currentLimit = editingBudgetLimit,
            currencySymbol = uiState.currencySymbol,
            initialPeriod = editingBudgetPeriod,
            initialThreshold = editingBudgetThreshold,
            availableCategories = Categories.expenseCategories.map { it.name },
            onDismiss = { editingBudgetCategory = null },
            onSave = { category, newLimit, period, threshold ->
                viewModel.setBudget(category, newLimit, period, threshold)
                editingBudgetCategory = null
            },
            onDelete = { category, period ->
                viewModel.deleteBudget(category, period)
                editingBudgetCategory = null
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All Records?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear all transactions, daily sales records, and custom portfolios. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmation = false
                    }
                ) {
                    Text("Clear All", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Net Worth Security Shield Unlock Dialog (Offline Passcode or Online OTP)
    if (showNetWorthUnlockDialog) {
        com.example.ui.components.NetWorthUnlockDialog(
            userPasscode = uiState.netWorthPasscode,
            onPasscodeSuccess = {
                isNetWorthHidden = false
                showNetWorthUnlockDialog = false
                android.widget.Toast.makeText(context, "Net Worth Unlocked!", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showNetWorthUnlockDialog = false },
            onChangeSavedPasscode = { newPin ->
                viewModel.setNetWorthPasscode(newPin)
                android.widget.Toast.makeText(context, "Passcode updated to $newPin!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}
