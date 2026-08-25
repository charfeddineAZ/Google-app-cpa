package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import com.example.model.AutomationMode
import com.example.model.BrowserPrepStage
import com.example.model.Persona
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleDark
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AutomatorViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CpaJavaScriptBridge(
  private val onField: (String, String) -> Unit,
  private val onAction: (String) -> Unit,
  private val onLog: (String) -> Unit
) {
  @JavascriptInterface
  fun onFieldFilled(name: String, value: String) {
    onField(name, value)
  }

  @JavascriptInterface
  fun onActionTriggered(action: String) {
    onAction(action)
  }

  @JavascriptInterface
  fun log(msg: String) {
    onLog(msg)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveBrowserScreen(
  viewModel: AutomatorViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val isAutomating by viewModel.isAutomating.collectAsState()
  val prepStage by viewModel.currentPrepStage.collectAsState()
  val proxyProfile by viewModel.proxyProfile.collectAsState()
  val persona by viewModel.persona.collectAsState()
  val taskConfig by viewModel.taskConfig.collectAsState()
  val leadStatus by viewModel.leadStatus.collectAsState()
  val pageTitle by viewModel.simulatedPageTitle.collectAsState()
  val liveFields by viewModel.liveFields.collectAsState()
  val currentCycle by viewModel.currentCycle.collectAsState()
  val currentRepeat by viewModel.currentTaskRepeat.collectAsState()
  val countdown by viewModel.countdownSeconds.collectAsState()
  val targetUrl by viewModel.targetWebViewUrl.collectAsState()
  val webProgress by viewModel.webPageProgress.collectAsState()

  // Sheet States for Modular Windows
  var showPersonaSheet by remember { mutableStateOf(false) }
  var showDomFieldsSheet by remember { mutableStateOf(false) }
  var showLeadCheckerSheet by remember { mutableStateOf(false) }
  var showEngineSheet by remember { mutableStateOf(false) }

  // Fullscreen toggle state (hides toolbars so webview gets 100% of the screen)
  var isImmersiveFullscreen by remember { mutableStateOf(false) }

  // Continuous auto-loop state (watches DOM mutations, new pages, fields, and auto-clicks next step)
  var isAutoLoopActive by remember { mutableStateOf(true) }

  var addressInput by remember(targetUrl) { mutableStateOf(targetUrl) }
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var isPageLoading by remember { mutableStateOf(false) }
  var lastLoadedUrl by remember { mutableStateOf("") }
  var webError by remember { mutableStateOf<String?>(null) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  // Listen to navigation events from ViewModel
  LaunchedEffect(targetUrl, webViewRef) {
    webViewRef?.let { wv ->
      if (targetUrl.isNotBlank() && targetUrl != lastLoadedUrl) {
        lastLoadedUrl = targetUrl
        val headers = mutableMapOf<String, String>()
        if (taskConfig.referrerBaseUrl.isNotBlank()) {
          headers["Referer"] = taskConfig.referrerBaseUrl
        }
        wv.loadUrl(targetUrl, headers)
      }
    }
  }

  // Continuous loop runner: when isAutomating or isAutoLoopActive is true, periodically re-evaluates script
  LaunchedEffect(isAutomating, isAutoLoopActive, persona, webViewRef) {
    if (isAutomating || isAutoLoopActive) {
      while (true) {
        webViewRef?.let { wv ->
          val jsCode = buildAutomationInjectionScript(persona, continuousLoop = true)
          wv.evaluateJavascript(jsCode, null)
        }
        kotlinx.coroutines.delay(2200)
      }
    }
  }

  // Listen for AI Form Fill manual triggers from ViewModel
  LaunchedEffect(webViewRef) {
    viewModel.triggerFormFillEvent.collectLatest {
      webViewRef?.let { wv ->
        val jsCode = buildAutomationInjectionScript(persona, continuousLoop = true)
        wv.evaluateJavascript(jsCode) { result ->
          viewModel.onJsLog("Form Fill JS Evaluated: $result")
        }
      }
    }
  }

  // Listen for reload signals
  LaunchedEffect(webViewRef) {
    viewModel.reloadSignal.collectLatest {
      webViewRef?.reload()
    }
  }

  // Listen for clear cache signals
  LaunchedEffect(webViewRef) {
    viewModel.clearCacheSignal.collectLatest {
      webViewRef?.let { wv ->
        wv.clearCache(true)
        wv.clearHistory()
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
      }
    }
  }

  // ==========================================
  // FULL SCREEN ROOT CONTAINER
  // ==========================================
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF1E1B2E))
  ) {
    // 1. TRUE FULL-SCREEN ANDROID WEBVIEW (occupies 100% of screen)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .testTag("full_screen_webview_container")
    ) {
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              databaseEnabled = true
              loadWithOverviewMode = true
              useWideViewPort = true
              builtInZoomControls = true
              displayZoomControls = false
              setSupportZoom(true)
              cacheMode = WebSettings.LOAD_DEFAULT
              userAgentString = proxyProfile.userAgent
              mediaPlaybackRequiresUserGesture = false
              mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
              allowFileAccess = false
              allowContentAccess = true
              setGeolocationEnabled(true)
            }

            // Add JavaScript Bridge
            addJavascriptInterface(
              CpaJavaScriptBridge(
                onField = { field, value ->
                  viewModel.onJsFieldFilled(field, value)
                },
                onAction = { action ->
                  viewModel.onJsAction(action)
                },
                onLog = { msg ->
                  viewModel.onJsLog(msg)
                }
              ),
              "CpaBridge"
            )

            webChromeClient = object : WebChromeClient() {
              override fun onProgressChanged(view: WebView?, newProgress: Int) {
                viewModel.onWebProgressChanged(newProgress)
                isPageLoading = newProgress < 100
                if (newProgress in 15..95) {
                  val earlyScript = buildAntiLeakScript()
                  view?.evaluateJavascript(earlyScript, null)
                }
              }

              override fun onReceivedTitle(view: WebView?, title: String?) {
                if (!title.isNullOrBlank()) {
                  viewModel.onWebTitleChanged(title, view?.url ?: "")
                }
              }

              override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                consoleMessage?.let {
                  if (it.messageLevel() == android.webkit.ConsoleMessage.MessageLevel.ERROR) {
                    viewModel.onJsLog("JS Error [${it.lineNumber()}]: ${it.message()}")
                  } else {
                    viewModel.onJsLog("JS: ${it.message()}")
                  }
                }
                return true
              }
            }

            webViewClient = object : WebViewClient() {
              override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isPageLoading = true
                webError = null
                url?.let {
                  addressInput = it
                  viewModel.onJsLog("Navigating to: $it")
                }
                view?.evaluateJavascript(buildAntiLeakScript(), null)
              }

              override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                val requestUrl = request?.url?.toString()?.lowercase() ?: ""
                if (requestUrl.contains("stun.anura.io") || requestUrl.contains("webrtc-check")) {
                  return android.webkit.WebResourceResponse("text/plain", "UTF-8", java.io.ByteArrayInputStream("".toByteArray()))
                }
                return super.shouldInterceptRequest(view, request)
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isPageLoading = false
                url?.let { addressInput = it }
                view?.evaluateJavascript(buildAntiLeakScript(), null)

                if (viewModel.isAutomating.value) {
                  val autoFillJs = buildAutomationInjectionScript(persona)
                  view?.evaluateJavascript(autoFillJs, null)
                }
              }

              override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                val failingUrl = request?.url?.toString() ?: ""
                if (failingUrl.contains("stun.anura.io") || failingUrl.contains("webrtc") || failingUrl.contains("stun:")) {
                  return
                }
                if (request?.isForMainFrame == true) {
                  webError = "Page Load Error: ${error?.description ?: "Failed to connect"}"
                  viewModel.onJsLog("WebView MainFrame Error: ${error?.description} on $failingUrl")
                }
              }

              override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
              }
            }

            val headers = mutableMapOf<String, String>()
            if (taskConfig.referrerBaseUrl.isNotBlank()) {
              headers["Referer"] = taskConfig.referrerBaseUrl
            }
            loadUrl(targetUrl, headers)
            webViewRef = this
          }
        },
        update = { wv ->
          wv.settings.userAgentString = proxyProfile.userAgent
        },
        modifier = Modifier.fillMaxSize()
      )

      // Connection error overlay
      if (webError != null) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.96f))
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = StatusRed, modifier = Modifier.size(44.dp))
            Text("Connection Notice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text(webError ?: "", fontSize = 12.sp, color = TextSecondary)
            Button(
              onClick = {
                webError = null
                webViewRef?.reload()
              },
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Retry Loading", fontSize = 13.sp)
            }
          }
        }
      }
    }

    // 2. TOP TOOLBAR & COMPACT ADDRESS CONTROLLER (Can collapse in immersive mode)
    AnimatedVisibility(
      visible = !isImmersiveFullscreen,
      enter = slideInVertically { -it } + fadeIn(),
      exit = slideOutVertically { -it } + fadeOut(),
      modifier = Modifier.align(Alignment.TopCenter)
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E1B2E).copy(alpha = 0.96f),
        shadowElevation = 8.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Row A: Back, Forward, URL TextField, Reload, Offer Shortcut, Fullscreen Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = { if (webViewRef?.canGoBack() == true) webViewRef?.goBack() },
              modifier = Modifier.size(30.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp), tint = Color.White)
            }

            IconButton(
              onClick = { if (webViewRef?.canGoForward() == true) webViewRef?.goForward() },
              modifier = Modifier.size(30.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward", modifier = Modifier.size(16.dp), tint = Color.White)
            }

            // Interactive Address Bar
            Box(
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF2E2A44))
                .border(1.dp, Color(0xFF4A4468), RoundedCornerShape(18.dp))
                .padding(horizontal = 8.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(12.dp))
                OutlinedTextField(
                  value = addressInput,
                  onValueChange = { addressInput = it },
                  placeholder = { Text("Enter URL (https://...)", fontSize = 11.sp, color = Color.Gray) },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                  ),
                  keyboardActions = KeyboardActions(
                    onGo = {
                      viewModel.loadCustomUrl(addressInput)
                    }
                  ),
                  modifier = Modifier.weight(1f),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color.White
                  ),
                  textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                  )
                )
              }
            }

            // Reload Button
            IconButton(
              onClick = { viewModel.triggerReload() },
              modifier = Modifier.size(30.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(16.dp), tint = Color.White)
            }

            // Quick Target CPA Offer Shortcut
            IconButton(
              onClick = {
                viewModel.loadCustomUrl("https://rileymarker.com/show.php?l=0&u=2227942&id=74924")
              },
              modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(PrimaryPurple)
            ) {
              Icon(Icons.Outlined.Link, contentDescription = "Target Offer", modifier = Modifier.size(16.dp), tint = Color.White)
            }

            // Enter Fullscreen Button
            IconButton(
              onClick = { isImmersiveFullscreen = true },
              modifier = Modifier.size(30.dp)
            ) {
              Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", modifier = Modifier.size(18.dp), tint = StatusYellow)
            }
          }

          // Page loading bar
          if (isPageLoading) {
            LinearProgressIndicator(
              progress = { (webProgress.coerceIn(5, 100)) / 100f },
              modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp)),
              color = PrimaryPurpleLight,
              trackColor = Color(0xFF2E2A44)
            )
          }

          // Row B: MODULAR WINDOW OPENER BUTTONS (Persona, DOM Fields, Lead RSS, Engine)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // 1. Open Persona Window Button
            Button(
              onClick = { showPersonaSheet = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3355)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFBB86FC))
              Spacer(modifier = Modifier.width(4.dp))
              Text("👤 Persona ID", fontSize = 11.sp, color = Color.White)
            }

            // 2. Open DOM Injected Fields Window Button
            Button(
              onClick = { showDomFieldsSheet = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3355)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Icon(Icons.Outlined.Assignment, contentDescription = null, modifier = Modifier.size(13.dp), tint = StatusYellow)
              Spacer(modifier = Modifier.width(4.dp))
              Text("📋 DOM (${liveFields.size})", fontSize = 11.sp, color = Color.White)
            }

            // 3. Open Lead Checker Window Button
            Button(
              onClick = { showLeadCheckerSheet = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (leadStatus.detectedLeadsCount > 0) StatusGreen else Color(0xFF3B3355)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Icon(Icons.Outlined.TrackChanges, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (leadStatus.detectedLeadsCount > 0) Color.White else Color(0xFFFF9800))
              Spacer(modifier = Modifier.width(4.dp))
              Text("🎯 Leads (${leadStatus.detectedLeadsCount})", fontSize = 11.sp, color = Color.White)
            }

            // 4. Open Automation Engine Window Button
            Button(
              onClick = { showEngineSheet = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isAutomating) PrimaryPurple else Color(0xFF3B3355)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isAutomating) StatusGreen else Color.Gray)
                  .then(if (isAutomating) Modifier.scale(pulseScale) else Modifier)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isAutomating) "⚙️ Running (${countdown}s)" else "⚙️ Engine", fontSize = 11.sp, color = Color.White)
            }

            // 5. Clear Cookies / Cache Button
            OutlinedButton(
              onClick = {
                viewModel.triggerClearCache()
                Toast.makeText(context, "Browser cache & cookies flushed!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Clear Cache", fontSize = 10.sp)
            }
          }
        }
      }
    }

    // 3. FLOATING MINI-DOCK WHEN IN IMMERSIVE FULLSCREEN MODE
    AnimatedVisibility(
      visible = isImmersiveFullscreen,
      enter = fadeIn() + slideInVertically { it },
      exit = fadeOut() + slideOutVertically { it },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(14.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Quick Persona Floating Button
        FloatingActionButton(
          onClick = { showPersonaSheet = true },
          containerColor = Color(0xFF3B3355),
          contentColor = Color.White,
          modifier = Modifier.size(46.dp),
          elevation = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
          Icon(Icons.Outlined.Person, contentDescription = "Persona", modifier = Modifier.size(20.dp))
        }

        // Quick Engine Floating Button
        FloatingActionButton(
          onClick = { showEngineSheet = true },
          containerColor = PrimaryPurple,
          contentColor = Color.White,
          modifier = Modifier.size(46.dp),
          elevation = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
          Icon(Icons.Default.Settings, contentDescription = "Engine", modifier = Modifier.size(20.dp))
        }

        // Exit Fullscreen Floating Button
        FloatingActionButton(
          onClick = { isImmersiveFullscreen = false },
          containerColor = Color(0xFF2E2A44),
          contentColor = Color.White,
          modifier = Modifier.size(46.dp),
          elevation = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
          Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", modifier = Modifier.size(20.dp))
        }
      }
    }

    // ====================================================================
    // MODULAR WINDOW 1: PERSONA & IDENTITY DETAILS (Modal Bottom Sheet)
    // ====================================================================
    if (showPersonaSheet) {
      ModalBottomSheet(
        onDismissRequest = { showPersonaSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFFEF7FF),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(PrimaryPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
              }
              Column {
                Text("Persona Identity & Credentials", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Auto-generated for live form completion", fontSize = 11.sp, color = TextSecondary)
              }
            }

            IconButton(onClick = { showPersonaSheet = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          HorizontalDivider(color = SurfaceBorder)

          // Persona Detail Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              PersonaDetailRow("Full Name", persona.fullName, Icons.Outlined.Person)
              PersonaDetailRow("Title & Gender", "${persona.title} • ${persona.gender} (${persona.genderArabic})", Icons.Outlined.Person)
              PersonaDetailRow("Split Birth Date", "D: ${persona.birthDayPadded} • M: ${persona.birthMonthName} (${persona.birthMonth}) • Y: ${persona.birthYear} (Age: ${persona.age})", Icons.Outlined.Assignment)
              PersonaDetailRow("Email", persona.email, Icons.Outlined.Email, copyable = true) {
                clipboardManager.setText(AnnotatedString(persona.email))
                Toast.makeText(context, "Email copied!", Toast.LENGTH_SHORT).show()
              }
              PersonaDetailRow("Phone", "${persona.phoneAreaCode} • ${persona.phoneNumber}", Icons.Outlined.Public)
              PersonaDetailRow("Street Address", "${persona.streetAddress}, ${persona.streetAddress2}", Icons.Outlined.LocationOn)
              PersonaDetailRow("City & State & Zip", "${persona.city}, ${persona.state} (${persona.zipCode})", Icons.Outlined.LocationOn)
              PersonaDetailRow("Country & Code", "${persona.country} (${persona.countryCode})", Icons.Outlined.Public)
              PersonaDetailRow("Payment Card", "${persona.cardNumber} [${persona.cardExpiry}] • CVV: ${persona.cardCvv}", Icons.Outlined.CreditCard, copyable = true) {
                clipboardManager.setText(AnnotatedString(persona.cardNumber))
                Toast.makeText(context, "Card number copied!", Toast.LENGTH_SHORT).show()
              }
              PersonaDetailRow("Password", persona.password, Icons.Default.Lock)
            }
          }

          // Quick Action inside Persona Window
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                viewModel.regeneratePersona(proxyProfile.countryCode)
                Toast.makeText(context, "New Persona Generated!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Generate New ID", fontSize = 12.sp)
            }

            Button(
              onClick = {
                viewModel.triggerManualFill()
                showPersonaSheet = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Fill In Browser", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // ====================================================================
    // MODULAR WINDOW 2: DOM INJECTED FIELDS INSPECTOR (Modal Bottom Sheet)
    // ====================================================================
    if (showDomFieldsSheet) {
      ModalBottomSheet(
        onDismissRequest = { showDomFieldsSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFFEF7FF),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .fillMaxHeight(0.75f),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(StatusYellow.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Outlined.Assignment, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
              }
              Column {
                Text("DOM Form Fields & Injections", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${liveFields.size} Fields detected and processed", fontSize = 11.sp, color = TextSecondary)
              }
            }

            IconButton(onClick = { showDomFieldsSheet = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          HorizontalDivider(color = SurfaceBorder)

          if (liveFields.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight)
                .padding(20.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(36.dp))
                Text("No Fields Captured Yet", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Click '⚡ Trigger AI Scan & Fill' below to scan the active page DOM.", fontSize = 12.sp, color = TextSecondary)
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(liveFields.toList()) { (label, value) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurpleDark)
                    Text(text = value, fontSize = 11.sp, color = TextPrimary)
                  }
                  Icon(Icons.Filled.CheckCircle, contentDescription = "Filled", tint = StatusGreen, modifier = Modifier.size(16.dp))
                }
              }
            }
          }

          Button(
            onClick = {
              viewModel.triggerManualFill()
            },
            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("⚡ Trigger AI Scan & Fill Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // ====================================================================
    // MODULAR WINDOW 3: CPAGRIP LEAD RSS VERIFIER (Modal Bottom Sheet)
    // ====================================================================
    if (showLeadCheckerSheet) {
      ModalBottomSheet(
        onDismissRequest = { showLeadCheckerSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFFEF7FF),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Outlined.TrackChanges, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
              }
              Column {
                Text("CPAGrip Lead Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Live RSS Feed query for IP ${proxyProfile.ip}", fontSize = 11.sp, color = TextSecondary)
              }
            }

            IconButton(onClick = { showLeadCheckerSheet = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          HorizontalDivider(color = SurfaceBorder)

          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (leadStatus.detectedLeadsCount > 0) StatusGreen.copy(alpha = 0.1f) else SurfaceLight
            ),
            border = CardDefaults.outlinedCardBorder().copy(
              width = 1.dp,
              brush = androidx.compose.ui.graphics.SolidColor(if (leadStatus.detectedLeadsCount > 0) StatusGreen else SurfaceBorder)
            )
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Total Confirmed Leads", fontSize = 11.sp, color = TextSecondary)
                  Text("${leadStatus.detectedLeadsCount} Leads", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (leadStatus.detectedLeadsCount > 0) StatusGreen else TextPrimary)
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (leadStatus.detectedLeadsCount > 0) StatusGreen else Color(0xFFEEEEEE))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = if (leadStatus.detectedLeadsCount > 0) "CONVERTED" else "PENDING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (leadStatus.detectedLeadsCount > 0) Color.White else Color.Gray
                  )
                }
              }

              Text("Feed Status: ${leadStatus.statusMessage}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PrimaryPurpleDark)
              Text("User ID: ${taskConfig.cpaGripUserId} • Time: 1day", fontSize = 11.sp, color = TextSecondary)
              Text("Verified Target IP: ${proxyProfile.ip}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
              Text("Last Checked: ${leadStatus.lastCheckTimestamp}", fontSize = 10.sp, color = TextSecondary)
            }
          }

          Button(
            onClick = {
              viewModel.manualLeadCheck()
              Toast.makeText(context, "Checking CPAGrip RSS Feed...", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Query RSS Feed Now", fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // ====================================================================
    // MODULAR WINDOW 4: AUTOMATION ENGINE & STAGE CONTROLLER (Modal Sheet)
    // ====================================================================
    if (showEngineSheet) {
      ModalBottomSheet(
        onDismissRequest = { showEngineSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFFEF7FF),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(PrimaryPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
              }
              Column {
                Text("Automation Engine Controller", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                  text = if (isAutomating) "STATUS: RUNNING (${countdown}s)" else "STATUS: STANDBY",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isAutomating) StatusGreen else TextSecondary
                )
              }
            }

            IconButton(onClick = { showEngineSheet = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          HorizontalDivider(color = SurfaceBorder)

          // Engine Details Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Execution Mode:", fontSize = 12.sp, color = TextSecondary)
                Text(taskConfig.selectedMode.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cycle Progress:", fontSize = 12.sp, color = TextSecondary)
                Text("Cycle $currentCycle of ${taskConfig.processRepeatCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Task Repeat in Session:", fontSize = 12.sp, color = TextSecondary)
                Text("Repeat $currentRepeat of ${if (taskConfig.selectedMode == AutomationMode.MODE_2_TASK_IN_SESSION) taskConfig.taskRepeatCount else 1}", fontSize = 12.sp, color = TextPrimary)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Current Stage:", fontSize = 12.sp, color = TextSecondary)
                Text(prepStage.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
              }
              Text("Stage Info: ${prepStage.desc}", fontSize = 11.sp, color = TextSecondary)
            }
          }

          // Start / Stop Automation Action
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (!isAutomating) {
              Button(
                onClick = {
                  viewModel.startAutomation()
                  showEngineSheet = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Automation Engine", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            } else {
              Button(
                onClick = {
                  viewModel.stopAutomation()
                  showEngineSheet = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stop Engine", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun PersonaDetailRow(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  copyable: Boolean = false,
  onCopy: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color.White)
      .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(14.dp))
      Column {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
      }
    }

    if (copyable && onCopy != null) {
      IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryPurple, modifier = Modifier.size(14.dp))
      }
    }
  }
}

@Composable
private fun BadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(100.dp))
      .background(color.copy(alpha = 0.15f))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
      Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
  }
}

/**
 * Constructs Anti-Leak script to mock and intercept WebRTC STUN requests (e.g. stun.anura.io)
 * to prevent IP leak detection and chromium socket errors.
 */
private fun buildAntiLeakScript(): String {
  return """
    (function() {
      try {
        if (window.__cpaAntiLeakActive) return;
        window.__cpaAntiLeakActive = true;

        // Mock RTCPeerConnection to prevent STUN server socket attempts
        function MockRTCPeerConnection(config) {
          this.localDescription = null;
          this.remoteDescription = null;
          this.signalingState = 'stable';
          this.iceConnectionState = 'connected';
          this.iceGatheringState = 'complete';
          this.onicecandidate = null;
          this.ontrack = null;
          this.onnegotiationneeded = null;
        }

        MockRTCPeerConnection.prototype.createOffer = function() {
          return Promise.resolve({ type: 'offer', sdp: 'v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n' });
        };
        MockRTCPeerConnection.prototype.createAnswer = function() {
          return Promise.resolve({ type: 'answer', sdp: 'v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n' });
        };
        MockRTCPeerConnection.prototype.setLocalDescription = function() { return Promise.resolve(); };
        MockRTCPeerConnection.prototype.setRemoteDescription = function() { return Promise.resolve(); };
        MockRTCPeerConnection.prototype.addIceCandidate = function() { return Promise.resolve(); };
        MockRTCPeerConnection.prototype.close = function() {};
        MockRTCPeerConnection.prototype.getStats = function() { return Promise.resolve(new Map()); };

        window.RTCPeerConnection = MockRTCPeerConnection;
        window.webkitRTCPeerConnection = MockRTCPeerConnection;
        window.mozRTCPeerConnection = MockRTCPeerConnection;

        if (navigator.mediaDevices) {
          navigator.mediaDevices.getUserMedia = function() {
            return Promise.reject(new Error("Media capture disabled by privacy policy"));
          };
          navigator.mediaDevices.enumerateDevices = function() {
            return Promise.resolve([]);
          };
        }

        if (window.CpaBridge) {
          window.CpaBridge.log("WebRTC & STUN shield initialized cleanly.");
        }
      } catch(e) {}
    })();
  """.trimIndent()
}

/**
 * Constructs robust JavaScript to analyze the live DOM, fill matching inputs, and click continue/submit.
 * Comprehensively handles split Birth Date fields (Day, Month, Year as inputs or selects),
 * unified DOB, gender, addresses, passwords, survey ratings, payment cards, and CTA navigation buttons.
 */
private fun buildAutomationInjectionScript(persona: Persona, continuousLoop: Boolean = true): String {
  val fName = persona.firstName.replace("'", "\\'").replace("\"", "\\\"")
  val lName = persona.lastName.replace("'", "\\'").replace("\"", "\\\"")
  val fullName = persona.fullName.replace("'", "\\'").replace("\"", "\\\"")
  val email = persona.email.replace("'", "\\'").replace("\"", "\\\"")
  val phone = persona.phoneNumber.replace("'", "\\'").replace("\"", "\\\"")
  val phoneArea = persona.phoneAreaCode.replace("'", "\\'").replace("\"", "\\\"")
  val street = persona.streetAddress.replace("'", "\\'").replace("\"", "\\\"")
  val street2 = persona.streetAddress2.replace("'", "\\'").replace("\"", "\\\"")
  val city = persona.city.replace("'", "\\'").replace("\"", "\\\"")
  val state = persona.state.replace("'", "\\'").replace("\"", "\\\"")
  val zip = persona.zipCode.replace("'", "\\'").replace("\"", "\\\"")
  val country = persona.country.replace("'", "\\'").replace("\"", "\\\"")
  val countryCode = persona.countryCode.replace("'", "\\'").replace("\"", "\\\"")
  val bDay = persona.birthDay
  val bDayPad = persona.birthDayPadded
  val bMonth = persona.birthMonth
  val bMonthNum = persona.birthMonthNum
  val bMonthName = persona.birthMonthName
  val bMonthShort = persona.birthMonthShort
  val bYear = persona.birthYear
  val bYearShort = persona.birthYearShort
  val bDate = persona.birthDate
  val cardNum = persona.cardNumber
  val cardExp = persona.cardExpiry
  val cardMonth = persona.cardExpMonth
  val cardYear = persona.cardExpYear
  val cardYearFull = persona.cardExpYearFull
  val cardCvv = persona.cardCvv
  val pwd = persona.password.replace("'", "\\'").replace("\"", "\\\"")
  val occupation = persona.occupation.replace("'", "\\'").replace("\"", "\\\"")
  val income = persona.incomeRange.replace("'", "\\'").replace("\"", "\\\"")
  val education = persona.educationLevel.replace("'", "\\'").replace("\"", "\\\"")
  val gender = persona.gender

  return """
    (function() {
      try {
        window.__cpaRunScanAndFill = function(isAutoTriggered) {
          try {
            var filledCount = 0;
            var elements = document.querySelectorAll('input, select, textarea');

            // 1. Rock-solid helper to set value even with React / Angular / Vue synthetic events
            function setNativeValue(element, value) {
              if (!element) return;
              try {
                element.focus();
                var proto = window.HTMLInputElement ? window.HTMLInputElement.prototype : Object.getPrototypeOf(element);
                var tag = (element.tagName || '').toLowerCase();
                if (tag === 'textarea' && window.HTMLTextAreaElement) {
                  proto = window.HTMLTextAreaElement.prototype;
                } else if (tag === 'select' && window.HTMLSelectElement) {
                  proto = window.HTMLSelectElement.prototype;
                }
                var descriptor = Object.getOwnPropertyDescriptor(proto, 'value') || Object.getOwnPropertyDescriptor(Object.getPrototypeOf(element), 'value');
                if (descriptor && descriptor.set) {
                  descriptor.set.call(element, value);
                } else {
                  element.value = value;
                }
              } catch(e) {
                try { element.value = value; } catch(err) {}
              }

              // Dispatch comprehensive synthetic events
              try {
                element.dispatchEvent(new Event('input', { bubbles: true, cancelable: true }));
                element.dispatchEvent(new Event('change', { bubbles: true, cancelable: true }));
                element.dispatchEvent(new Event('blur', { bubbles: true }));
              } catch(e) {}
            }

            // 2. Helper to match and select dropdown options
            function matchSelectOption(selectEl, candidates) {
              if (!selectEl || !selectEl.options || selectEl.options.length === 0) return false;
              var opts = selectEl.options;
              for (var i = 0; i < opts.length; i++) {
                var opt = opts[i];
                var val = (opt.value || '').trim().toLowerCase();
                var txt = (opt.text || opt.innerText || '').trim().toLowerCase();
                for (var c = 0; c < candidates.length; c++) {
                  var cand = (candidates[c] || '').toString().trim().toLowerCase();
                  if (cand === '') continue;
                  if (val === cand || txt === cand || (val.length > 0 && val === cand) || (txt.length > 0 && txt.indexOf(cand) !== -1)) {
                    selectEl.selectedIndex = i;
                    setNativeValue(selectEl, opt.value);
                    return true;
                  }
                }
              }
              // If none matched but candidates has entries and select has options, select first non-disabled option
              if (opts.length > 1 && selectEl.selectedIndex <= 0) {
                selectEl.selectedIndex = 1;
                setNativeValue(selectEl, opts[1].value);
                return true;
              }
              return false;
            }

            // Iterate over all DOM elements
            for (var i = 0; i < elements.length; i++) {
              try {
                var el = elements[i];
                var tag = (el.tagName || '').toLowerCase();
                var type = (el.type || '').toLowerCase();
                var name = (el.name || '').toLowerCase();
                var id = (el.id || '').toLowerCase();
                var placeholder = (el.placeholder || '').toLowerCase();
                var aria = (el.getAttribute('aria-label') || '').toLowerCase();
                var autocomplete = (el.getAttribute('autocomplete') || '').toLowerCase();
                var testId = (el.getAttribute('data-testid') || '').toLowerCase();

                // Surrounding label text
                var parentText = '';
                try {
                  if (el.labels && el.labels.length > 0) {
                    parentText = (el.labels[0].innerText || '').toLowerCase();
                  } else if (el.parentElement) {
                    parentText = (el.parentElement.innerText || '').toLowerCase().slice(0, 120);
                  }
                } catch(e) {}

                var label = (name + " " + id + " " + placeholder + " " + aria + " " + autocomplete + " " + testId + " " + parentText).trim();

                if (type === 'hidden' || type === 'submit' || type === 'button') continue;

                // ==========================================
                // 1. DATE OF BIRTH: SPLIT DAY / MONTH / YEAR
                // ==========================================
                // A. Split Day
                if (
                  name.indexOf('dob_d') !== -1 || name.indexOf('dob-d') !== -1 || name.indexOf('bday_d') !== -1 ||
                  name.indexOf('birth_d') !== -1 || name.indexOf('birthday_d') !== -1 || name.indexOf('birthdate_d') !== -1 ||
                  id.indexOf('dob_d') !== -1 || id.indexOf('dob-d') !== -1 || id.indexOf('bday_d') !== -1 ||
                  placeholder === 'dd' || placeholder === 'day' || placeholder === 'dia' || placeholder === 'jour' ||
                  (label.indexOf('day') !== -1 && (label.indexOf('birth') !== -1 || label.indexOf('dob') !== -1 || label.indexOf('bday') !== -1 || label.indexOf('date') !== -1))
                ) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$bDay', '$bDayPad']);
                  } else {
                    setNativeValue(el, '$bDayPad');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Birth Day (Split)', '$bDayPad');
                  continue;
                }

                // B. Split Month
                if (
                  name.indexOf('dob_m') !== -1 || name.indexOf('dob-m') !== -1 || name.indexOf('bday_m') !== -1 ||
                  name.indexOf('birth_m') !== -1 || name.indexOf('birthday_m') !== -1 || name.indexOf('birthdate_m') !== -1 ||
                  id.indexOf('dob_m') !== -1 || id.indexOf('dob-m') !== -1 || id.indexOf('bday_m') !== -1 ||
                  placeholder === 'mm' || placeholder === 'month' || placeholder === 'mes' || placeholder === 'mois' ||
                  (label.indexOf('month') !== -1 && (label.indexOf('birth') !== -1 || label.indexOf('dob') !== -1 || label.indexOf('bday') !== -1 || label.indexOf('date') !== -1))
                ) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$bMonthNum', '$bMonth', '$bMonthName', '$bMonthShort']);
                  } else {
                    setNativeValue(el, (el.maxLength === 2) ? '$bMonth' : '$bMonthNum');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Birth Month (Split)', '$bMonthName');
                  continue;
                }

                // C. Split Year
                if (
                  name.indexOf('dob_y') !== -1 || name.indexOf('dob-y') !== -1 || name.indexOf('bday_y') !== -1 ||
                  name.indexOf('birth_y') !== -1 || name.indexOf('birthday_y') !== -1 || name.indexOf('birthdate_y') !== -1 ||
                  id.indexOf('dob_y') !== -1 || id.indexOf('dob-y') !== -1 || id.indexOf('bday_y') !== -1 ||
                  placeholder === 'yyyy' || placeholder === 'yy' || placeholder === 'year' || placeholder === 'ano' || placeholder === 'annee' ||
                  (label.indexOf('year') !== -1 && (label.indexOf('birth') !== -1 || label.indexOf('dob') !== -1 || label.indexOf('bday') !== -1 || label.indexOf('date') !== -1))
                ) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$bYear', '$bYearShort']);
                  } else {
                    setNativeValue(el, (el.maxLength === 2 || placeholder === 'yy') ? '$bYearShort' : '$bYear');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Birth Year (Split)', '$bYear');
                  continue;
                }

                // D. Unified Date of Birth (Single Field / type="date")
                if (
                  type === 'date' || label.indexOf('dob') !== -1 || label.indexOf('birthdate') !== -1 ||
                  label.indexOf('bday') !== -1 || label.indexOf('birthday') !== -1 || label.indexOf('birth_date') !== -1 ||
                  label.indexOf('date_of_birth') !== -1 || label.indexOf('naissance') !== -1 || label.indexOf('ميلاد') !== -1
                ) {
                  var dateVal = '$bYear-$bMonth-$bDayPad';
                  if (type !== 'date') {
                    if (placeholder.indexOf('mm/dd/yyyy') !== -1 || placeholder.indexOf('mm-dd-yyyy') !== -1) {
                      dateVal = '$bMonth/$bDayPad/$bYear';
                    } else if (placeholder.indexOf('dd/mm/yyyy') !== -1 || placeholder.indexOf('dd-mm-yyyy') !== -1) {
                      dateVal = '$bDayPad/$bMonth/$bYear';
                    } else {
                      dateVal = '$bDate';
                    }
                  }
                  setNativeValue(el, dateVal);
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Date of Birth (Unified)', dateVal);
                  continue;
                }

                // E. Age Field / Age Range Dropdown
                if (label.indexOf('age') !== -1 && label.indexOf('page') === -1 && label.indexOf('language') === -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['${persona.age}', '25-34', '25-40', '30-39', '30-49', '21-35', '18-35', 'Over 21', '25-54']);
                  } else {
                    setNativeValue(el, '${persona.age}');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Age', '${persona.age}');
                  continue;
                }

                // ==========================================
                // 2. NAME IDENTIFIERS
                // ==========================================
                // First Name
                if (
                  label.indexOf('first') !== -1 || label.indexOf('fname') !== -1 || label.indexOf('given') !== -1 ||
                  label.indexOf('forename') !== -1 || label.indexOf('prenom') !== -1 || label.indexOf('vorname') !== -1 ||
                  label.indexOf('الاول') !== -1
                ) {
                  setNativeValue(el, '$fName');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('First Name', '$fName');
                  continue;
                }

                // Last Name
                if (
                  label.indexOf('last') !== -1 || label.indexOf('lname') !== -1 || label.indexOf('surname') !== -1 ||
                  label.indexOf('family') !== -1 || label.indexOf('nom') !== -1 || label.indexOf('nachname') !== -1 ||
                  label.indexOf('apellido') !== -1 || label.indexOf('العائلة') !== -1 || label.indexOf('اللقب') !== -1
                ) {
                  setNativeValue(el, '$lName');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Last Name', '$lName');
                  continue;
                }

                // Full Name
                if (
                  label.indexOf('fullname') !== -1 || label.indexOf('full_name') !== -1 || label.indexOf('your_name') !== -1 ||
                  (label.indexOf('name') !== -1 && label.indexOf('card') === -1 && label.indexOf('user') === -1 && label.indexOf('user_name') === -1 && label.indexOf('first') === -1 && label.indexOf('last') === -1)
                ) {
                  setNativeValue(el, '$fullName');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Full Name', '$fullName');
                  continue;
                }

                // Title / Salutation
                if (label.indexOf('title') !== -1 || label.indexOf('salutation') !== -1 || label.indexOf('prefix') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['${persona.title}', 'Mr.', 'Mr', 'Sir']);
                  } else {
                    setNativeValue(el, '${persona.title}');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Title', '${persona.title}');
                  continue;
                }

                // ==========================================
                // 3. GENDER & SEX
                // ==========================================
                if (label.indexOf('gender') !== -1 || label.indexOf('sex') !== -1 || label.indexOf('sexe') !== -1 || label.indexOf('geschlecht') !== -1 || label.indexOf('جنس') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$gender', 'Male', 'M', 'Homme', 'ذكر', 'Hombre', 'Mann']);
                  } else if (type === 'radio') {
                    var radioVal = (el.value || '').toLowerCase();
                    if (radioVal === 'male' || radioVal === 'm' || radioVal === '1' || radioVal.indexOf('man') !== -1) {
                      el.checked = true;
                      el.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Gender', '$gender');
                  continue;
                }

                // ==========================================
                // 4. CONTACT: EMAIL & PHONE (SPLIT & FULL)
                // ==========================================
                // Email
                if (type === 'email' || label.indexOf('email') !== -1 || label.indexOf('mail') !== -1 || label.indexOf('courriel') !== -1 || label.indexOf('correo') !== -1 || label.indexOf('بريد') !== -1) {
                  setNativeValue(el, '$email');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Email', '$email');
                  continue;
                }

                // Phone Area Code
                if (label.indexOf('area_code') !== -1 || label.indexOf('areacode') !== -1 || label.indexOf('phone_area') !== -1) {
                  setNativeValue(el, '$phoneArea');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Phone Area Code', '$phoneArea');
                  continue;
                }

                // Phone / Mobile
                if (type === 'tel' || label.indexOf('phone') !== -1 || label.indexOf('tel') !== -1 || label.indexOf('mobile') !== -1 || label.indexOf('cell') !== -1 || label.indexOf('هاتف') !== -1 || label.indexOf('جوال') !== -1) {
                  setNativeValue(el, '$phone');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Phone Number', '$phone');
                  continue;
                }

                // ==========================================
                // 5. ADDRESS & LOCATION (SPLIT & FULL)
                // ==========================================
                // Address Line 2
                if (label.indexOf('address2') !== -1 || label.indexOf('street2') !== -1 || label.indexOf('apt') !== -1 || label.indexOf('suite') !== -1 || label.indexOf('unit') !== -1) {
                  setNativeValue(el, '$street2');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Address Line 2', '$street2');
                  continue;
                }

                // Street Address Line 1
                if (label.indexOf('address') !== -1 || label.indexOf('street') !== -1 || label.indexOf('adresse') !== -1 || label.indexOf('strasse') !== -1 || label.indexOf('direccion') !== -1 || label.indexOf('عنوان') !== -1) {
                  setNativeValue(el, '$street');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Street Address', '$street');
                  continue;
                }

                // City
                if (label.indexOf('city') !== -1 || label.indexOf('town') !== -1 || label.indexOf('ville') !== -1 || label.indexOf('stadt') !== -1 || label.indexOf('ciudad') !== -1 || label.indexOf('مدينة') !== -1) {
                  setNativeValue(el, '$city');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('City', '$city');
                  continue;
                }

                // State / Region / Province
                if (label.indexOf('state') !== -1 || label.indexOf('province') !== -1 || label.indexOf('region') !== -1 || label.indexOf('county') !== -1 || label.indexOf('محافظة') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$state', '$city', 'CA', 'California', 'NY', 'New York', 'TX', 'Texas', 'London', 'Cairo']);
                  } else {
                    setNativeValue(el, '$state');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('State / Region', '$state');
                  continue;
                }

                // Zip / Postal Code
                if (label.indexOf('zip') !== -1 || label.indexOf('postal') !== -1 || label.indexOf('postcode') !== -1 || label.indexOf('post_code') !== -1 || label.indexOf('plz') !== -1 || label.indexOf('البريدي') !== -1) {
                  setNativeValue(el, '$zip');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Zip Code', '$zip');
                  continue;
                }

                // Country
                if (label.indexOf('country') !== -1 || label.indexOf('nation') !== -1 || label.indexOf('pays') !== -1 || label.indexOf('land') !== -1 || label.indexOf('دولة') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$country', '$countryCode', 'United States', 'US', 'USA', 'Egypt', 'EG', 'United Kingdom', 'GB', 'UK']);
                  } else {
                    setNativeValue(el, '$country');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Country', '$country');
                  continue;
                }

                // ==========================================
                // 6. PASSWORDS & ACCOUNTS
                // ==========================================
                if (type === 'password' || label.indexOf('password') !== -1 || label.indexOf('pass') !== -1 || label.indexOf('pwd') !== -1 || label.indexOf('مرور') !== -1) {
                  setNativeValue(el, '$pwd');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Password', '••••••••••••');
                  continue;
                }

                // ==========================================
                // 7. PRODUCT REVIEW & CPA SURVEY QUESTIONS
                // ==========================================
                if (label.indexOf('employment') !== -1 || label.indexOf('job') !== -1 || label.indexOf('work') !== -1 || label.indexOf('occupation') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['Employed', 'Full-time', 'Part-time', 'Self-employed', 'Employed full-time', 'Professional']);
                  } else {
                    setNativeValue(el, '$occupation');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Occupation / Employment', '$occupation');
                  continue;
                }

                if (label.indexOf('income') !== -1 || label.indexOf('salary') !== -1 || label.indexOf('earning') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$income', '$50,000 - $74,999', '$50,000 - $75,000', '$40,000 - $60,000', '$50,000+', '$75,000 - $99,999']);
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Income Range', '$income');
                  continue;
                }

                if (label.indexOf('education') !== -1 || label.indexOf('degree') !== -1 || label.indexOf('academic') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$education', "Bachelor's", "College Graduate", "University", "High School Graduate"]);
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Education', '$education');
                  continue;
                }

                // ==========================================
                // 8. PAYMENT & CREDIT CARD (SPLIT & FULL)
                // ==========================================
                if (label.indexOf('exp_month') !== -1 || label.indexOf('exp_m') !== -1 || label.indexOf('expiration_month') !== -1 || label.indexOf('expiry_month') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$cardMonth', '04', '4', 'April', 'Apr']);
                  } else {
                    setNativeValue(el, '$cardMonth');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Card Exp Month (Split)', '$cardMonth');
                  continue;
                }

                if (label.indexOf('exp_year') !== -1 || label.indexOf('exp_y') !== -1 || label.indexOf('expiration_year') !== -1 || label.indexOf('expiry_year') !== -1) {
                  if (tag === 'select') {
                    matchSelectOption(el, ['$cardYearFull', '$cardYear', '2028', '28']);
                  } else {
                    setNativeValue(el, (el.maxLength === 2) ? '$cardYear' : '$cardYearFull');
                  }
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Card Exp Year (Split)', '$cardYear');
                  continue;
                }

                if (label.indexOf('exp') !== -1 || label.indexOf('expiry') !== -1 || label.indexOf('expiration') !== -1 || label.indexOf('cc_exp') !== -1) {
                  setNativeValue(el, '$cardExp');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Card Expiry', '$cardExp');
                  continue;
                }

                if (label.indexOf('card') !== -1 || label.indexOf('credit') !== -1 || label.indexOf('ccnum') !== -1 || label.indexOf('pan') !== -1) {
                  setNativeValue(el, '$cardNum');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Card Number', '${cardNum.take(8)}****');
                  continue;
                }

                if (label.indexOf('cvv') !== -1 || label.indexOf('cvc') !== -1 || label.indexOf('security') !== -1 || label.indexOf('cid') !== -1) {
                  setNativeValue(el, '$cardCvv');
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('CVV', '$cardCvv');
                  continue;
                }

                // ==========================================
                // 9. CHECKBOXES & RADIOS (CONSENT & POLICIES)
                // ==========================================
                if (type === 'checkbox' && !el.checked) {
                  el.checked = true;
                  el.dispatchEvent(new Event('change', { bubbles: true }));
                  filledCount++;
                  if (window.CpaBridge) window.CpaBridge.onFieldFilled('Checkbox Terms', 'Checked');
                  continue;
                }

                if (type === 'radio' && !el.checked) {
                  var rVal = (el.value || '').toLowerCase();
                  if (rVal === 'yes' || rVal === '1' || rVal === 'true' || rVal === '5' || rVal === 'agree' || rVal.indexOf('yes') !== -1 || rVal.indexOf('agree') !== -1 || rVal.indexOf('male') !== -1) {
                    el.checked = true;
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                    filledCount++;
                    if (window.CpaBridge) window.CpaBridge.onFieldFilled('Radio Survey', el.value || 'Selected');
                  }
                }
              } catch(fieldErr) {
                // Ignore individual field errors so rest of form continues seamlessly
              }
            }

            // ==========================================
            // 10. MULTI-LANGUAGE SMART BUTTON CLICKER LOOP
            // ==========================================
            setTimeout(function() {
              try {
                var buttons = document.querySelectorAll('button, input[type="submit"], input[type="button"], a.btn, a[class*="btn"], a[class*="button"], [role="button"], div.submit-btn');
                var clicked = false;
                var submitKeywords = [
                  'continue', 'submit', 'next', 'get started', 'start', 'apply', 'apply now', 'join', 'join now',
                  'confirm', 'claim', 'claim now', 'register', 'sign up', 'proceed', 'verify', 'done', 'finish', 'step',
                  'متابعة', 'تقديم', 'تسجيل', 'بدء', 'إرسال', 'تأكيد', 'التالي', 'اشترك',
                  'continuer', 'suivant', 'soumettre', 'postuler', 'commencer',
                  'weiter', 'absenden', 'registrieren', 'fortfahren',
                  'continuar', 'siguiente', 'enviar', 'solicitar'
                ];

                for (var b = 0; b < buttons.length; b++) {
                  var btn = buttons[b];
                  var text = (btn.innerText || btn.value || btn.getAttribute('aria-label') || '').toLowerCase().trim();
                  for (var k = 0; k < submitKeywords.length; k++) {
                    if (text.indexOf(submitKeywords[k]) !== -1) {
                      try {
                        btn.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        btn.focus();
                        btn.click();
                        btn.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                      } catch(e) {
                        btn.click();
                      }
                      clicked = true;
                      if (window.CpaBridge) {
                        window.CpaBridge.onActionTriggered('Smart Click: ' + text);
                      }
                      break;
                    }
                  }
                  if (clicked) break;
                }

                // Fallback: If no matched CTA found, click the first visible button or submit form
                if (!clicked && filledCount > 0) {
                  if (buttons.length > 0) {
                    try {
                      buttons[0].click();
                      if (window.CpaBridge) window.CpaBridge.onActionTriggered('Clicked First Button');
                    } catch(e) {}
                  } else {
                    var forms = document.querySelectorAll('form');
                    if (forms.length > 0) {
                      try {
                        forms[0].submit();
                        if (window.CpaBridge) window.CpaBridge.onActionTriggered('Triggered Form Submit');
                      } catch(e) {}
                    }
                  }
                }
              } catch(btnErr) {}
            }, 600);

            return "Processed " + filledCount + " fields.";
          } catch(e) {
            if (window.CpaBridge) window.CpaBridge.log("Scan error: " + e.message);
            return "Error: " + e.message;
          }
        };

        // Run immediate pass
        var res = window.__cpaRunScanAndFill(false);

        // ==========================================
        // 11. CONTINUOUS MUTATION OBSERVER & INTERVAL LOOP
        // ==========================================
        ${if (continuousLoop) """
        if (!window.__cpaObserverInstalled) {
          window.__cpaObserverInstalled = true;

          // A. Mutation Observer for Dynamic Single Page Apps / React / Vue step transitions
          var debounceTimer = null;
          var observer = new MutationObserver(function(mutations) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(function() {
              window.__cpaRunScanAndFill(true);
            }, 500);
          });
          observer.observe(document.documentElement || document.body, { childList: true, subtree: true, attributes: false });

          // B. Recurring Interval Scanner (every 1.5 seconds)
          setInterval(function() {
            window.__cpaRunScanAndFill(true);
          }, 1500);

          // C. Route & History Watcher
          window.addEventListener('popstate', function() { window.__cpaRunScanAndFill(true); });
          window.addEventListener('hashchange', function() { window.__cpaRunScanAndFill(true); });

          if (window.CpaBridge) {
            window.CpaBridge.log("Continuous AI Auto-Fill & Smart Click Loop is ACTIVE.");
          }
        }
        """ else ""}

        return res;
      } catch(err) {
        if (window.CpaBridge) window.CpaBridge.log("Error in AI script: " + err.message);
        return "Error: " + err.message;
      }
    })();
  """.trimIndent()
}
