package com.informatique.tawsekmisr.ui.activities


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.common.util.LocalAppLocale
import com.informatique.tawsekmisr.data.datastorehelper.TokenManager
import com.informatique.tawsekmisr.ui.base.BaseActivity
import com.informatique.tawsekmisr.ui.screens.HomePageScreen
import com.informatique.tawsekmisr.ui.screens.FindOfficeScreen
import com.informatique.tawsekmisr.ui.screens.OfficeDetailsScreen
import com.informatique.tawsekmisr.ui.screens.ReservationTicketScreen
import com.informatique.tawsekmisr.ui.screens.BookingInquiryScreen
import com.informatique.tawsekmisr.ui.screens.WebViewScreen
import com.informatique.tawsekmisr.ui.screens.OtherServicesScreen
import com.informatique.tawsekmisr.ui.theme.AppTheme
import com.informatique.tawsekmisr.ui.theme.ThemeOption
import com.informatique.tawsekmisr.ui.viewmodels.LanguageViewModel
import com.informatique.tawsekmisr.ui.viewmodels.LandingUiState
import com.informatique.tawsekmisr.ui.viewmodels.LandingViewModel
import com.informatique.tawsekmisr.ui.viewmodels.SharedUserViewModel
import com.informatique.tawsekmisr.viewmodel.ThemeViewModel
import com.informatique.tawsekmisr.ui.providers.LocalOffices
import com.informatique.tawsekmisr.ui.providers.LocalGovernments
import com.informatique.tawsekmisr.ui.screens.OfficeReservationScreen
import com.informatique.tawsekmisr.ui.screens.SettingsScreen
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
import com.informatique.tawsekmisr.utils.RootUtil
import dagger.hilt.android.AndroidEntryPoint
import com.abanoub.versionchecker.UpdateAvailableCallback
import com.abanoub.versionchecker.VersionChecker
import com.informatique.tawsekmisr.ui.components.CustomAlertDialog
import java.net.URLDecoder
import java.util.Locale

@AndroidEntryPoint
class LandingActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Don't install splash screen API - we use custom overlay instead
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge for proper Android 36 support
        enableEdgeToEdge()

        val configuration = resources.configuration
        val originalScale = configuration.fontScale
        val limitedScale = originalScale.coerceIn(0.85f, 1.15f)

        if (originalScale != limitedScale) {
            configuration.fontScale = limitedScale
            val newContext = createConfigurationContext(configuration)
            applyOverrideConfiguration(newContext.resources.configuration)
        }

        setContent {
            // Set window to non-translucent once Compose is ready to avoid flickering
            LaunchedEffect(Unit) {
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }

            val languageViewModel: LanguageViewModel = hiltViewModel()
            val sharedUserViewModel: SharedUserViewModel = hiltViewModel()
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val landingViewModel: LandingViewModel = hiltViewModel()

            val lang by languageViewModel.languageFlow.collectAsState(initial = "ar")
            val currentLocale = Locale.forLanguageTag(lang)
            val themeOption by themeViewModel.theme.collectAsState(initial = ThemeOption.SYSTEM_DEFAULT)
            val offices by landingViewModel.offices.collectAsState()
            val governments by landingViewModel.governments.collectAsState()
            val uiState by landingViewModel.uiState.collectAsState()

            // Control splash screen visibility
            var showContent by remember { mutableStateOf(false) }

            // Root detection state
            var showRootDialog by remember { mutableStateOf(false) }
            var isRootCheckComplete by remember { mutableStateOf(false) }

            // Play Store update check state
            var showPlayStoreUpdateDialog by remember { mutableStateOf(false) }
            var isPlayStoreCheckComplete by remember { mutableStateOf(false) }

            // Check for Play Store updates with timeout
            LaunchedEffect(Unit) {
                // Use withTimeoutOrNull for automatic timeout handling
                kotlinx.coroutines.withTimeoutOrNull(5000) {
                    try {
                        val checker = VersionChecker.getInstance(this@LandingActivity)
                        checker.check(object : UpdateAvailableCallback {
                            override fun onUpdateAvailableListener(updateAvailable: Boolean) {
                                if (updateAvailable) {
                                    showPlayStoreUpdateDialog = true
                                }
                                isPlayStoreCheckComplete = true
                            }

                            override fun onCheckFailureListener() {
                                // Failed to check Play Store, continue with normal flow
                                isPlayStoreCheckComplete = true
                            }
                        })

                        // Keep the coroutine alive to prevent timeout from completing prematurely
                        kotlinx.coroutines.delay(Long.MAX_VALUE)
                    } catch (_: Exception) {
                        // Error in Play Store check, continue with normal flow
                        isPlayStoreCheckComplete = true
                    }
                }

                // Timeout occurred or exception caught, ensure we complete the check
                if (!isPlayStoreCheckComplete) {
                    isPlayStoreCheckComplete = true
                }
            }

            // Check for rooted device during splash
            LaunchedEffect(Unit) {
                val rootUtil = RootUtil()
                val isRooted = rootUtil.isDeviceRooted(applicationContext)
                if (isRooted) {
                    showRootDialog = true
                }
                isRootCheckComplete = true
            }

            // Modified: Always continue to app after checks complete, even if data failed
            LaunchedEffect(uiState, isRootCheckComplete, isPlayStoreCheckComplete) {
                // Wait for all checks to complete
                if (isRootCheckComplete && isPlayStoreCheckComplete) {
                    // Check UI state
                    when (val state = uiState) {
                        is LandingUiState.Loading -> {
                            // Still loading, wait
                        }
                        is LandingUiState.Success -> {
                            // Data loaded successfully
                            showContent = true
                        }
                        is LandingUiState.Error -> {
                            // Data failed to load, but continue to app
                            // Show toast notification
                            val message = if (lang == "ar") {
                                "الخدمة خارج الخدمة حالياً - يمكنك المحاولة لاحقاً"
                            } else {
                                "Service is currently unavailable - You can try later"
                            }
                            Toast.makeText(
                                this@LandingActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                            showContent = true
                        }
                        is LandingUiState.UpdateRequired -> {
                            // Only block if it's a critical update from API
                            // But we'll show the content anyway since we have VersionChecker as backup
                            showContent = true
                        }
                    }
                }
            }

            // Handle version check and update required
            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }

            LaunchedEffect(uiState) {
                when (val state = uiState) {
                    is LandingUiState.UpdateRequired -> {
                        showUpdateDialog = true
                        updateInfo = Pair(state.currentVersion, state.requiredVersion)
                    }
                    else -> {
                        showUpdateDialog = false
                    }
                }
            }

            // Show Play Store update dialog first (if needed)
            if (showPlayStoreUpdateDialog) {
                UpdateRequiredBottomSheet(
                    currentVersion = packageManager.getPackageInfo(packageName, 0).versionCode,
                    requiredVersion = 0,
                    onClose = {
                        (this@LandingActivity as? ComponentActivity)?.finish()
                    }
                )
            }





            CompositionLocalProvider(
                LocalLayoutDirection provides if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalAppLocale provides currentLocale,
                LocalOffices provides offices,
                LocalGovernments provides governments
            ) {
                AppTheme(themeOption = themeOption) {
                    // Show API update required dialog - user cannot dismiss it
                    if (showUpdateDialog && updateInfo != null) {
                        UpdateRequiredBottomSheet(
                            currentVersion = updateInfo!!.first,
                            requiredVersion = updateInfo!!.second,
                            onClose = {
                                (this@LandingActivity as? ComponentActivity)?.finish()
                            }
                        )
                    }
                    // Show root detection dialog if device is rooted - Using CustomAlertDialog
//            if (showRootDialog) {
//                CustomAlertDialog(
//                    showDialog = showRootDialog,
//                    onDismiss = {
//                        // Force close app when user clicks close button
//                        (this@LandingActivity as? ComponentActivity)?.finish()
//                    },
//                    icon = Icons.Default.Warning,
//                    iconTint = Color.White,
//                    iconBackgroundTint = Color(0xFFE53935), // Red color for warning
//                    title = if (lang == "ar") "تحذير: جهاز مروت" else "Warning: Rooted Device",
//                    message = if (lang == "ar")
//                        "تم اكتشاف أن جهازك مروت. يرجى ملاحظة أن استخدام هذا التطبيق على جهاز مروت قد يعرض بياناتك للخطر. نوصي باستخدام التطبيق على جهاز غير مروت."
//                    else
//                        "It has been detected that your device is rooted. Please note that using this app on a rooted device may expose your data to security risks. We recommend using the app on a non-rooted device.",
//                    dismissButtonText = if (lang == "ar") "إغلاق" else "Close",
//                    onConfirm = null, // No confirm button - only close button
//                    showDismissButton = true
//                )
//            }
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Show custom splash overlay with logo and branding during loading
                            AnimatedVisibility(
                                visible = !showContent,
                                exit = fadeOut(animationSpec = tween(400))
                            ) {
                                SplashScreenOverlay()
                            }

                            // Show main content when ready
                            AnimatedVisibility(
                                visible = showContent,
                                enter = fadeIn(animationSpec = tween(400))
                            ) {
                                val navController = rememberNavController()
                                var startDestination by remember { mutableStateOf<String?>(null) }

                                LaunchedEffect(Unit) {
                                    val token = TokenManager.getToken(applicationContext)
                                    startDestination = if (token.isNullOrEmpty()) "homepage" else "homepage"
                                }

                                if (startDestination != null) {
                                    NavHost(
                                        navController = navController,
                                        startDestination = startDestination!!
                                    ) {
                                        composable("homepage") {
                                            HomePageScreen(navController)
                                        }

                                        // Find Office Screen - for branch_load and book_appointment
                                        composable("branch_load") {
                                            FindOfficeScreen(
                                                navController = navController,
                                                showBookingButton = false // Hide booking button for branch services
                                            )
                                        }

                                        composable("book_appointment") {
                                            FindOfficeScreen(
                                                navController = navController,
                                                showBookingButton = true // Show booking button for appointment booking
                                            )
                                        }

                                        // Office Details Screen - shows detailed info about selected office
                                        composable("office_details/{officeId}/{distance}/{showBookingButton}") { backStackEntry ->
                                            val officeId = backStackEntry.arguments?.getString("officeId") ?: ""
                                            val distance = backStackEntry.arguments?.getString("distance")?.toDoubleOrNull() ?: 0.0
                                            val showBookingButton = backStackEntry.arguments?.getString("showBookingButton")?.toBoolean() ?: true

                                            // Get the actual office from LocalOffices provider
                                            val offices = LocalOffices.current
                                            val office = offices.firstOrNull { it.id == officeId }

                                            if (office != null) {
                                                OfficeDetailsScreen(
                                                    navController = navController,
                                                    office = office,
                                                    distance = distance,
                                                    showBookingButton = showBookingButton
                                                )
                                            } else {
                                                // Handle case where office is not found - show error or navigate back
                                                LaunchedEffect(Unit) {
                                                    navController.navigateUp()
                                                }
                                            }
                                        }

                                        // Office Reservation Screen - for booking appointments
                                        composable("booking_form/{officeId}") { backStackEntry ->
                                            val officeId = backStackEntry.arguments?.getString("officeId") ?: ""

                                            // Get the actual office from LocalOffices provider
                                            val offices = LocalOffices.current
                                            val office = offices.firstOrNull { it.id == officeId }

                                            OfficeReservationScreen(
                                                navController = navController,
                                                officeId = officeId,
                                                officeName = office?.name ?: "Office", // Use actual office name
                                                orgTypeDesc = office?.type ?: "" // Use actual office type description
                                            )
                                        }

                                        // Booking Inquiry Screen - check user reservations
                                        composable("booking_inquiry") {
                                            BookingInquiryScreen(navController = navController)
                                        }

                                        // Forms Screen - WebView (Direct)
                                        composable("forms") {
                                            val url = "https://rern.gov.eg/pages/requests/2"
                                            val title = "النماذج"

                                            WebViewScreen(
                                                navController = navController,
                                                url = url,
                                                title = title
                                            )
                                        }

                                        // E-Notarization Screen - WebView (Direct)
                                        composable("enotary") {
                                            val url = "https://digital.gov.eg/categories/5cfcf9aa4ae2fc5e38d82959"
                                            val title = "التوثيق الإلكتروني"

                                            WebViewScreen(
                                                navController = navController,
                                                url = url,
                                                title = title
                                            )
                                        }

                                        // Other Services Screen
                                        composable("other_services") {
                                            OtherServicesScreen(navController = navController)
                                        }

                                        // WebView Screen - Common for all web content
                                        composable("webview/{url}/{title}") { backStackEntry ->
                                            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                                            val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""
                                            val url = URLDecoder.decode(encodedUrl, "UTF-8")
                                            val title = URLDecoder.decode(encodedTitle, "UTF-8")

                                            WebViewScreen(
                                                navController = navController,
                                                url = url,
                                                title = title
                                            )
                                        }

                                        // ⚙️ شاشة الإعدادات (اختيار الثيم)
                                        composable(
                                            route = "settings_screen",
                                            enterTransition = { defaultEnterTransition() },
                                            exitTransition = { defaultExitTransition() }
                                        ) {
                                            SettingsScreen(
                                                navController = navController,
                                                sharedUserViewModel = sharedUserViewModel,
                                                viewModel = themeViewModel
                                            )
                                        }

                                        // Reservation Ticket Screen - shows the ticket for the reserved appointment
                                        composable("reservation_ticket/{nationalId}/{officeName}/{attendedNo}/{ticketNo}/{reservationDate}/{reservationTime}") { backStackEntry ->
                                            val nationalId = URLDecoder.decode(
                                                backStackEntry.arguments?.getString("nationalId") ?: "",
                                                "UTF-8"
                                            )
                                            val officeName = URLDecoder.decode(
                                                backStackEntry.arguments?.getString("officeName") ?: "",
                                                "UTF-8"
                                            )
                                            val attendedNo = backStackEntry.arguments?.getString("attendedNo")?.toIntOrNull() ?: 0
                                            val ticketNo = URLDecoder.decode(
                                                backStackEntry.arguments?.getString("ticketNo") ?: "",
                                                "UTF-8"
                                            )
                                            val reservationDate = URLDecoder.decode(
                                                backStackEntry.arguments?.getString("reservationDate") ?: "",
                                                "UTF-8"
                                            )
                                            val reservationTime = URLDecoder.decode(
                                                backStackEntry.arguments?.getString("reservationTime") ?: "",
                                                "UTF-8"
                                            )

                                            ReservationTicketScreen(
                                                navController = navController,
                                                nationalId = nationalId,
                                                officeName = officeName,
                                                attendanceNo = attendedNo,
                                                reservationDate = reservationDate,
                                                reservationTime = reservationTime
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Remove the checkPlayStoreUpdate() method - no longer needed
}
@Composable
fun SplashScreenOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val extraColors = LocalExtraColors.current

    // Animated background circles scale
    val circle1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle1Scale"
    )

    val circle2Scale by infiniteTransition.animateFloat(
        initialValue = 1.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle2Scale"
    )

    // Logo entrance animation
    var logoOpacity by remember { mutableStateOf(0f) }
    var logoScale by remember { mutableStateOf(0.8f) }

    LaunchedEffect(Unit) {
        // Animate logo entrance
        androidx.compose.animation.core.animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        ) { value, _ ->
            logoOpacity = value
            logoScale = 0.8f + (0.2f * value)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background Layer with Blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(extraColors.iconDarkBlue)
                .blur(radius = 12.dp)
        ) {
            // Animated Background Circle 1
            Box(
                modifier = Modifier
                    .offset(x = (-100).dp, y = (-200).dp)
                    .size(300.dp)
                    .scale(circle1Scale)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )

            // Animated Background Circle 2
            Box(
                modifier = Modifier
                    .offset(x = 150.dp, y = 250.dp)
                    .size(400.dp)
                    .scale(circle2Scale)
                    .background(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )
        }

        // Content Layer (Clear, No Blur)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(0.35f))

            // Logo Container with Shadow Circles
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoOpacity)
            ) {
                // Logo Image
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(horizontal = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(logoOpacity)
                    .padding(horizontal = 32.dp)
            ) {
                // Main Title (Arabic)
                Text(
                    text = "أرغب في عمل توكيل",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle (English)
                Text(
                    text = "I want to make a power of attorney",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.weight(0.45f))

            // Loading Indicator Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(logoOpacity)
                    .padding(bottom = 60.dp)
            ) {
                // Modern Loading Animation - Bouncing Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    repeat(3) { index ->
                        val dotScale by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 600,
                                    delayMillis = index * 200,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot$index"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(10.dp)
                                .scale(dotScale)
                                .background(
                                    color = Color.White,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Loading text
                Text(
                    text = "جاري التحميل...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// Animation functions for navigation transitions (Navigation Compose 2.8.0+)
fun defaultEnterTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(durationMillis = 300)
    ) + slideInHorizontally(
        initialOffsetX = { it / 4 },
        animationSpec = tween(durationMillis = 300)
    )
}

fun defaultExitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(durationMillis = 300)
    ) + slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(durationMillis = 300)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateRequiredBottomSheet(
    currentVersion: Int,
    requiredVersion: Int,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val locale = LocalAppLocale.current
    val isArabic = locale.language == "ar"
    val context = LocalContext.current

    // Handle back button press - close app
    BackHandler {
        onClose()
    }

    ModalBottomSheet(
        onDismissRequest = { /* Do not dismiss */ },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
        dragHandle = null,
        containerColor = Color.Transparent,
        scrimColor = LocalExtraColors.current.black
    ) {
        // Full screen gradient background content - matching the image colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2B5278), // Darker blue (top)
                            Color(0xFF1E3A5F)  // Even darker blue (bottom)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Download Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = "Update Required",
                        tint = Color(0xFF2B5278),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Title (Arabic) - Using app theme typography
                Text(
                    text = "تحديث مطلوب",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title (English) - Using app theme typography
                Text(
                    text = "Update Required",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Message (Arabic) - Using app theme typography
                Text(
                    text = "يتوفر إصدار جديد من التطبيق. يرجى التحديث للمتابعة.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    ),
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message (English) - Using app theme typography
                Text(
                    text = "A new version is available. Please update to continue.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Update Button - directly below text - Opens Play Store
                Button(
                    onClick = {
                        // Open Play Store when user clicks update
                        try {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=${context.packageName}")
                                )
                            )
                        } catch (e: android.content.ActivityNotFoundException) {
                            // Play Store not installed, open web browser
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                )
                            )
                        }
                        // Close app after opening store
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2B5278)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isArabic) "تحديث الآن | Update Now" else "Update Now | تحديث الآن",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
