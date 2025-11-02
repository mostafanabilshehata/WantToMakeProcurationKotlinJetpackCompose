package com.informatique.tawsekmisr.ui.screens

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.ui.components.localizedApp
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    navController: NavController,
    url: String,
    title: String? = null
) {
    val extraColors = LocalExtraColors.current

    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf(title ?: "") }
    var hasError by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf(url) }

    // ✅ Permission request state for camera/audio
    var permissionCallback by remember { mutableStateOf<android.webkit.PermissionRequest?>(null) }

    // ✅ Permission launcher for camera and audio
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            permissionCallback?.grant(permissionCallback?.resources)
        } else {
            permissionCallback?.deny()
        }
        permissionCallback = null
    }

    // Get localized strings ONCE in composable context
    val backContentDesc = localizedApp(R.string.webview_back)
    val refreshContentDesc = localizedApp(R.string.webview_refresh)
    val forwardContentDesc = localizedApp(R.string.webview_forward)
    val loadingText = localizedApp(R.string.webview_loading)
    val errorTitle = localizedApp(R.string.webview_error_title)
    val errorMessage = localizedApp(R.string.webview_error_message)
    val retryText = localizedApp(R.string.webview_retry)

    // Set initial title if empty
    if (pageTitle.isEmpty()) {
        pageTitle = title ?: loadingText
    }

    // ✅ Helper function to update navigation state
    fun updateNavigationState(view: WebView?) {
        canGoBack = view?.canGoBack() ?: false
        canGoForward = view?.canGoForward() ?: false
    }

    // ✅ Properly clean up WebView when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                try {
                    stopLoading()
                    loadUrl("about:blank")
                    clearHistory()
                    clearCache(true)
                    removeAllViews()
                    destroy()
                } catch (_: Exception) {
                    // Ignore
                }
            }
            webView = null
        }
    }

    Scaffold(
        topBar = {
            Column {
                // 🎯 Simple Top Bar matching rest of app
                TopAppBar(
                    title = {
                        Text(
                            text = pageTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            // ✅ Stop loading and navigate back immediately
                            webView?.stopLoading()
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )

                // ✅ Progress bar directly aligned at bottom of top bar
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { loadingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFF6EB3A6),
                        trackColor = Color.Transparent
                    )
                } else {
                    // Spacer to maintain layout consistency
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp))
                }
            }
        },
        bottomBar = {
            // Navigation Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(enabled = canGoBack) {
                                webView?.let { view ->
                                    if (view.canGoBack()) {
                                        view.goBack()
                                        // Update state immediately after navigation
                                        updateNavigationState(view)
                                    }
                                }
                            },
                        color = if (canGoBack) Color(0xFF6EB3A6).copy(alpha = 0.15f)
                               else Color(0xFFF2F4F7)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backContentDesc,
                            tint = if (canGoBack) Color(0xFF6EB3A6) else Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Forward button
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(enabled = canGoForward) {
                                webView?.let { view ->
                                    if (view.canGoForward()) {
                                        view.goForward()
                                        // Update state immediately after navigation
                                        updateNavigationState(view)
                                    }
                                }
                            },
                        color = if (canGoForward) Color(0xFF6EB3A6).copy(alpha = 0.15f)
                               else Color(0xFFF2F4F7)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = forwardContentDesc,
                            tint = if (canGoForward) Color(0xFF6EB3A6) else Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Refresh button
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                webView?.reload()
                                hasError = false
                            },
                        color = Color(0xFF6EB3A6).copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = refreshContentDesc,
                            tint = Color(0xFF6EB3A6),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // WebView content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(extraColors.background)
                .padding(paddingValues)
        ) {
            // WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            // ✅ Enable JavaScript and all web features
                            javaScriptEnabled = true
                            javaScriptCanOpenWindowsAutomatically = true

                            // ✅ Enable storage
                            domStorageEnabled = true

                            // ✅ Enable viewport and zoom
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = false
                            setSupportZoom(true)

                            // ✅ Enable file and content access
                            allowFileAccess = true
                            allowContentAccess = true

                            // ✅ Enable mixed content (HTTP + HTTPS) - IMPORTANT for rern.gov.eg forms
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                            // ✅ Enable caching for better performance and back/forward navigation
                            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                            // ✅ Enable plugins and media
                            mediaPlaybackRequiresUserGesture = false

                            // ✅ Enable geolocation
                            setGeolocationEnabled(true)

                            // ✅ Disable safe browsing to prevent blocking mixed content
                            safeBrowsingEnabled = false

                            // ✅ Enable HTML5 features
                            loadsImagesAutomatically = true
                            blockNetworkImage = false
                            blockNetworkLoads = false
                        }

                        // ✅ Enable touch handling
                        isFocusable = true
                        isFocusableInTouchMode = true
                        isClickable = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                hasError = false
                                loadingProgress = 0f
                                currentUrl = url ?: ""
                                // ✅ Update navigation state immediately when page starts
                                updateNavigationState(view)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                loadingProgress = 1f
                                // ✅ Update navigation state after page loads
                                updateNavigationState(view)
                                pageTitle = view?.title ?: title ?: ""
                                currentUrl = url ?: ""
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                // Only show error for main frame errors, ignore resource errors
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    isLoading = false
                                }
                            }

                            // ✅ Allow all URL navigation - return false to let WebView handle it
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                // Return false to let WebView handle all URLs internally
                                // This ensures proper history tracking
                                return false
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                // ✅ Update navigation state when history changes
                                updateNavigationState(view)
                            }
                        }

                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadingProgress = newProgress / 100f
                                isLoading = newProgress < 100
                                // ✅ Update navigation state during loading
                                if (newProgress > 30) {
                                    updateNavigationState(view)
                                }
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                if (!title.isNullOrBlank()) {
                                    pageTitle = title
                                }
                            }

                            // ✅ Enable geolocation permission
                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: android.webkit.GeolocationPermissions.Callback?
                            ) {
                                callback?.invoke(origin, true, false)
                            }

                            // ✅ Handle console messages (for debugging)
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                // Suppress mixed content warnings to reduce log spam
                                return true
                            }

                            // ✅ Request permissions for camera and audio
                            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                                super.onPermissionRequest(request)
                                permissionCallback = request
                                // Show permission dialog
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.CAMERA,
                                        android.Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            }
                        }

                        loadUrl(url)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // ✅ Update navigation state whenever the view updates
                    updateNavigationState(view)
                }
            )

            // Error State
            if (hasError) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(extraColors.background),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                webView?.reload()
                                hasError = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6EB3A6)
                            ),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(retryText)
                        }
                    }
                }
            }
        }
    }
}
