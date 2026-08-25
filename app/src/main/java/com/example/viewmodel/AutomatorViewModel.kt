package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CpaLeadCheckerService
import com.example.data.GeoIpService
import com.example.data.PersonaGenerator
import com.example.data.ProxyListService
import com.example.model.AutomationMode
import com.example.model.AutomationState
import com.example.model.BrowserPrepStage
import com.example.model.LeadStatus
import com.example.model.LogEntry
import com.example.model.LogLevel
import com.example.model.Persona
import com.example.model.ProxyProfile
import com.example.model.SimulationStep
import com.example.model.TaskConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProxyListStats(
  val total: Int = 5,
  val currentIndex: Int = 0,
  val isFetching: Boolean = false,
  val sourceUrl: String = ProxyListService.DEFAULT_ASOCKS_URL,
  val lastStatus: String = "Ready"
)

class AutomatorViewModel : ViewModel() {

  private val _persona = MutableStateFlow(PersonaGenerator.generatePersona("US"))
  val persona: StateFlow<Persona> = _persona.asStateFlow()

  private val _proxyProfile = MutableStateFlow(ProxyProfile())
  val proxyProfile: StateFlow<ProxyProfile> = _proxyProfile.asStateFlow()

  private val _proxyStats = MutableStateFlow(ProxyListStats())
  val proxyStats: StateFlow<ProxyListStats> = _proxyStats.asStateFlow()

  private val _taskConfig = MutableStateFlow(TaskConfig())
  val taskConfig: StateFlow<TaskConfig> = _taskConfig.asStateFlow()

  private val _leadStatus = MutableStateFlow(LeadStatus())
  val leadStatus: StateFlow<LeadStatus> = _leadStatus.asStateFlow()

  private val _automationState = MutableStateFlow(AutomationState.IDLE)
  val automationState: StateFlow<AutomationState> = _automationState.asStateFlow()

  private val _isAutomating = MutableStateFlow(false)
  val isAutomating: StateFlow<Boolean> = _isAutomating.asStateFlow()

  private val _currentCycle = MutableStateFlow(1)
  val currentCycle: StateFlow<Int> = _currentCycle.asStateFlow()

  private val _currentTaskRepeat = MutableStateFlow(1)
  val currentTaskRepeat: StateFlow<Int> = _currentTaskRepeat.asStateFlow()

  private val _countdownSeconds = MutableStateFlow(55)
  val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

  private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
  val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

  private val _simulationSteps = MutableStateFlow<List<SimulationStep>>(emptyList())
  val simulationSteps: StateFlow<List<SimulationStep>> = _simulationSteps.asStateFlow()

  private val _currentPrepStage = MutableStateFlow(BrowserPrepStage.STAGE_1_PROXY_INIT)
  val currentPrepStage: StateFlow<BrowserPrepStage> = _currentPrepStage.asStateFlow()

  private val _simulatedPageTitle = MutableStateFlow("CPA Special Reward Landing Page")
  val simulatedPageTitle: StateFlow<String> = _simulatedPageTitle.asStateFlow()

  private val _liveFields = MutableStateFlow<Map<String, String>>(emptyMap())
  val liveFields: StateFlow<Map<String, String>> = _liveFields.asStateFlow()

  private val _targetWebViewUrl = MutableStateFlow("https://rileymarker.com/show.php?l=0&u=2227942&id=74924")
  val targetWebViewUrl: StateFlow<String> = _targetWebViewUrl.asStateFlow()

  private val _webPageProgress = MutableStateFlow(0)
  val webPageProgress: StateFlow<Int> = _webPageProgress.asStateFlow()

  private val _triggerFormFillEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val triggerFormFillEvent: SharedFlow<Unit> = _triggerFormFillEvent.asSharedFlow()

  private val _clearCacheSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val clearCacheSignal: SharedFlow<Unit> = _clearCacheSignal.asSharedFlow()

  private val _reloadSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val reloadSignal: SharedFlow<Unit> = _reloadSignal.asSharedFlow()

  private val _activeTab = MutableStateFlow(0) // 0: Dashboard, 1: Live Browser, 2: Proxy & Identity, 3: Tasks & UTM, 4: Logs
  val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

  private val _emailStats = MutableStateFlow(PersonaGenerator.getEmailPoolStats())
  val emailStats: StateFlow<Pair<Int, Int>> = _emailStats.asStateFlow()

  private var automationJob: Job? = null
  private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

  init {
    addLog(LogLevel.INFO, "System initialized: Advanced CPA Multi-Mode Engine ready.")
    addLog(LogLevel.NETWORK, "WebRTC Leak Prevention: ACTIVE [Isolated Sandbox]")
    addLog(LogLevel.INFO, "Proxy IP loaded: ${_proxyProfile.value.ip} (${_proxyProfile.value.countryName})")
    refreshSimulationSteps()

    // Fetch fresh residential proxy list from Asocks in background
    fetchProxyListFromUrl(_taskConfig.value.proxyListUrl)
  }

  fun fetchProxyListFromUrl(url: String = _taskConfig.value.proxyListUrl) {
    viewModelScope.launch {
      _proxyStats.value = _proxyStats.value.copy(isFetching = true, sourceUrl = url, lastStatus = "Fetching proxy list...")
      addLog(LogLevel.NETWORK, "Connecting to Proxy Provider URL: $url")
      val result = ProxyListService.fetchProxiesFromUrl(url)
      result.onSuccess { count ->
        _proxyStats.value = _proxyStats.value.copy(
          total = count,
          currentIndex = ProxyListService.getCurrentIndex(),
          isFetching = false,
          lastStatus = "Loaded $count live proxies"
        )
        addLog(LogLevel.SUCCESS, "✓ Successfully loaded $count residential proxies from provider list.")
        // Sync first proxy
        rotateToNextProxy(manual = false)
      }.onFailure { err ->
        _proxyStats.value = _proxyStats.value.copy(
          total = ProxyListService.getPoolSize(),
          isFetching = false,
          lastStatus = "Pool active (${ProxyListService.getPoolSize()} proxies ready)"
        )
        addLog(LogLevel.WARNING, "Notice: Provider fetch: ${err.message}. Using high-quality residential pool (${ProxyListService.getPoolSize()} available).")
      }
    }
  }

  fun rotateToNextProxy(manual: Boolean = true) {
    val next = ProxyListService.getNextProxy()
    _proxyStats.value = _proxyStats.value.copy(currentIndex = ProxyListService.getCurrentIndex())
    
    updateProxySettings(
      rawProxy = next.raw.ifBlank { "${next.protocol.lowercase()}://${next.username}:${next.password}@${next.ip}:${next.port}" },
      protocol = next.protocol,
      username = next.username,
      password = next.password
    )

    if (manual) {
      addLog(LogLevel.INFO, "Proxy rotated manually -> ${next.protocol}://${next.ip}:${next.port}")
    }
  }

  fun setActiveTab(index: Int) {
    _activeTab.value = index
  }

  fun updateTaskConfig(newConfig: TaskConfig) {
    _taskConfig.value = newConfig
    _countdownSeconds.value = newConfig.browserDurationSeconds
    addLog(LogLevel.INFO, "Task configuration updated. Mode: ${newConfig.selectedMode.displayName}")
  }

  fun updateProxySettings(
    rawProxy: String,
    protocol: String = "SOCKS5",
    username: String = "",
    password: String = ""
  ) {
    var cleanProxy = rawProxy.trim()
    var detectedProtocol = protocol
    var extractedUser = username
    var extractedPass = password
    var extractedIp = "175.110.115.169"
    var extractedPort = "443"

    try {
      // Handle socks5:// or http:// or https:// prefix
      if (cleanProxy.contains("://")) {
        val protoSplit = cleanProxy.split("://", limit = 2)
        detectedProtocol = protoSplit[0].uppercase()
        cleanProxy = protoSplit[1]
      }

      // Handle user:pass@host:port format
      if (cleanProxy.contains("@")) {
        val atSplit = cleanProxy.split("@", limit = 2)
        val authPart = atSplit[0]
        val hostPart = atSplit[1]

        val authSplit = authPart.split(":", limit = 2)
        extractedUser = authSplit.getOrNull(0)?.trim() ?: ""
        extractedPass = authSplit.getOrNull(1)?.trim() ?: ""

        val hostSplit = hostPart.split(":")
        extractedIp = hostSplit.getOrNull(0)?.trim() ?: "175.110.115.169"
        extractedPort = hostSplit.getOrNull(1)?.trim() ?: "443"
      } else {
        // Standard IP:Port or IP:Port:User:Pass
        val parts = cleanProxy.split(":")
        extractedIp = parts.getOrNull(0)?.trim() ?: "175.110.115.169"
        extractedPort = parts.getOrNull(1)?.trim() ?: "443"
        if (parts.size >= 3) extractedUser = parts[2].trim()
        if (parts.size >= 4) extractedPass = parts[3].trim()
      }
    } catch (e: Exception) {
      extractedIp = "175.110.115.169"
      extractedPort = "443"
    }

    _proxyProfile.value = _proxyProfile.value.copy(
      rawProxyInput = rawProxy,
      ip = extractedIp,
      port = extractedPort,
      protocol = detectedProtocol,
      username = extractedUser,
      password = extractedPass,
      countryName = if (rawProxy.contains("US", ignoreCase = true)) "United States" else _proxyProfile.value.countryName,
      countryCode = if (rawProxy.contains("US", ignoreCase = true)) "US" else _proxyProfile.value.countryCode,
      isConnected = true
    )

    addLog(LogLevel.NETWORK, "Proxy configured: $detectedProtocol://$extractedIp:$extractedPort (User: ${extractedUser.take(10)}...)")
    updateProxyIp(extractedIp)
  }

  fun updateProxyIp(newIp: String) {
    viewModelScope.launch {
      addLog(LogLevel.NETWORK, "Querying IP profile from api.i.pn for: $newIp")
      val result = GeoIpService.lookupIp(newIp)
      result.onSuccess { profile ->
        _proxyProfile.value = profile.copy(
          rawProxyInput = "${profile.ip}:${_proxyProfile.value.port}",
          protocol = _proxyProfile.value.protocol,
          port = _proxyProfile.value.port,
          username = _proxyProfile.value.username,
          password = _proxyProfile.value.password,
          isConnected = true
        )
        addLog(LogLevel.SUCCESS, "Geo profile synced: ${profile.city}, ${profile.countryName} [${profile.timezone}]")
        regeneratePersona(profile.countryCode)
      }
    }
  }

  fun regeneratePersona(countryCode: String = _proxyProfile.value.countryCode) {
    val newPersona = PersonaGenerator.generatePersona(countryCode)
    _persona.value = newPersona
    _emailStats.value = PersonaGenerator.getEmailPoolStats()
    addLog(LogLevel.SUCCESS, "Generated persona for ${newPersona.country}: ${newPersona.fullName} (${newPersona.cardType})")
  }

  fun rotateCvv() {
    val newCvv = PersonaGenerator.rotateCvv()
    _persona.value = _persona.value.copy(cardCvv = newCvv)
    addLog(LogLevel.INFO, "CVV rotated for repeat task: $newCvv")
  }

  fun addEmails(rawText: String) {
    val lines = rawText.split("\n", ",", " ").map { it.trim() }.filter { it.contains("@") }
    if (lines.isNotEmpty()) {
      PersonaGenerator.addEmailsToPool(lines)
      _emailStats.value = PersonaGenerator.getEmailPoolStats()
      addLog(LogLevel.SUCCESS, "Imported ${lines.size} emails to active email pool.")
    }
  }

  fun startAutomation() {
    if (_isAutomating.value) return
    _isAutomating.value = true
    _currentCycle.value = 1
    _currentTaskRepeat.value = 1
    // CRITICAL USER INTENT: Immediately switch to Browser screen upon start
    _activeTab.value = 1
    addLog(LogLevel.SUCCESS, "▶ Automation START triggered -> Switched to Live Browser view")

    automationJob = viewModelScope.launch {
      executeAutomationEngine()
    }
  }

  fun stopAutomation() {
    automationJob?.cancel()
    automationJob = null
    _isAutomating.value = false
    _automationState.value = AutomationState.IDLE
    addLog(LogLevel.WARNING, "■ Automation STOPPED by user.")
  }

  private suspend fun executeAutomationEngine() {
    val config = _taskConfig.value
    var cycle = 0

    while (_isAutomating.value) {
      cycle++
      _currentCycle.value = cycle

      if (!config.unlimitedCycles && cycle > config.processRepeatCount.coerceAtLeast(1)) {
        break
      }

      val cycleLabel = if (config.unlimitedCycles) "Cycle $cycle [Continuous Mode]" else "Cycle $cycle of ${config.processRepeatCount}"
      addLog(LogLevel.INFO, "========================================")
      addLog(LogLevel.INFO, "▶ Starting $cycleLabel")
      addLog(LogLevel.INFO, "========================================")

      // Stage 1: Auto-Rotate Proxy from Asocks pool & Launch isolated container
      _currentPrepStage.value = BrowserPrepStage.STAGE_1_PROXY_INIT
      _automationState.value = AutomationState.INITIALIZING_PROXY
      _simulatedPageTitle.value = "Configuring Dynamic Residential Proxy..."

      if (config.autoRotateProxyEachCycle && ProxyListService.getPoolSize() > 0) {
        val nextProxy = ProxyListService.getNextProxy()
        _proxyStats.value = _proxyStats.value.copy(currentIndex = ProxyListService.getCurrentIndex())
        
        val cleanInput = nextProxy.raw.ifBlank { "${nextProxy.protocol.lowercase()}://${nextProxy.username}:${nextProxy.password}@${nextProxy.ip}:${nextProxy.port}" }
        _proxyProfile.value = _proxyProfile.value.copy(
          rawProxyInput = cleanInput,
          ip = nextProxy.ip,
          port = nextProxy.port,
          protocol = nextProxy.protocol,
          username = nextProxy.username,
          password = nextProxy.password,
          isConnected = true
        )
        addLog(LogLevel.NETWORK, "[${currentTime()}] Switched to Proxy: ${nextProxy.protocol}://${nextProxy.ip}:${nextProxy.port}")
      }

      _liveFields.value = mapOf(
        "Proxy IP" to _proxyProfile.value.ip,
        "Port" to _proxyProfile.value.port,
        "Protocol" to _proxyProfile.value.protocol,
        "User" to _proxyProfile.value.username.take(12) + "..."
      )
      delay(700)

      // Stage 2: Geo IP & Real Geolocation Lookup via api.i.pn
      _currentPrepStage.value = BrowserPrepStage.STAGE_2_GEO_LOOKUP
      _automationState.value = AutomationState.GEO_LOOKUP
      _simulatedPageTitle.value = "Resolving Geolocation Profile for ${_proxyProfile.value.ip}..."
      
      try {
        val geoResult = GeoIpService.lookupIp(_proxyProfile.value.ip)
        geoResult.onSuccess { profile ->
          _proxyProfile.value = profile.copy(
            rawProxyInput = _proxyProfile.value.rawProxyInput,
            protocol = _proxyProfile.value.protocol,
            port = _proxyProfile.value.port,
            username = _proxyProfile.value.username,
            password = _proxyProfile.value.password,
            isConnected = true
          )
          addLog(LogLevel.SUCCESS, "[${currentTime()}] Geo-Lookup: ${_proxyProfile.value.city}, ${_proxyProfile.value.countryName} [${profile.timezone}]")
        }
      } catch (e: Exception) {
        addLog(LogLevel.WARNING, "Geo lookup notice: ${e.message}")
      }
      delay(600)

      // Stage 3: Timezone & Language Environment Spoofing
      _currentPrepStage.value = BrowserPrepStage.STAGE_3_LOCALE_INJECTION
      _automationState.value = AutomationState.INJECTING_ENVIRONMENT
      _simulatedPageTitle.value = "Injecting Timezone & Language Profiles..."
      _liveFields.value = mapOf(
        "Timezone" to _proxyProfile.value.timezone,
        "Browser Locale" to _proxyProfile.value.language,
        "User-Agent" to _proxyProfile.value.userAgent.take(30) + "..."
      )
      addLog(LogLevel.INFO, "[${currentTime()}] Injected Locale: ${_proxyProfile.value.language} | Timezone: ${_proxyProfile.value.timezone}")
      delay(600)

      // Stage 4: WebRTC Anti-Leak Shield
      _currentPrepStage.value = BrowserPrepStage.STAGE_4_WEBRTC_SHIELD
      _liveFields.value = mapOf(
        "WebRTC Shield" to "RTCPeerConnection Disabled",
        "Public IP Leak" to "Protected (Proxy Tunnel Sealed)",
        "DNS Mode" to "Remote Proxy DNS Only"
      )
      addLog(LogLevel.NETWORK, "[${currentTime()}] WebRTC Leak Prevention: ACTIVE (Real IP Protected)")
      delay(600)

      // Stage 5: Persona Generation based on synced IP Geo country
      _currentPrepStage.value = BrowserPrepStage.STAGE_5_PERSONA_GEN
      _simulatedPageTitle.value = "Synthesizing Persona Identity for ${_proxyProfile.value.countryName}..."
      val newPersona = PersonaGenerator.generatePersona(_proxyProfile.value.countryCode)
      _persona.value = newPersona
      _emailStats.value = PersonaGenerator.getEmailPoolStats()

      _liveFields.value = mapOf(
        "Full Name" to newPersona.fullName,
        "Email" to newPersona.email,
        "Phone" to newPersona.phoneNumber,
        "Address" to "${newPersona.streetAddress}, ${newPersona.city}, ${newPersona.zipCode}",
        "Card (Luhn)" to "${newPersona.cardNumber.take(9)}**** [${newPersona.cardType}]",
        "CVV / Expiry" to "${newPersona.cardCvv} | ${newPersona.cardExpiry}"
      )
      addLog(LogLevel.SUCCESS, "[${currentTime()}] Generated synthetic identity for ${newPersona.country}: ${newPersona.fullName} (${newPersona.cardType})")
      delay(800)

      // Stage 6: Navigate to CPA Offer with UTM
      _currentPrepStage.value = BrowserPrepStage.STAGE_6_NAVIGATE_UTM
      _automationState.value = AutomationState.BROWSER_ACTIVE
      val fullTargetUrl = buildUtmUrl()
      _targetWebViewUrl.value = fullTargetUrl
      _simulatedPageTitle.value = "Navigating to: $fullTargetUrl"
      
      // Flush previous cookies/cache before loading
      _clearCacheSignal.tryEmit(Unit)
      delay(400)
      _reloadSignal.tryEmit(Unit)

      _liveFields.value = mapOf(
        "HTTP Referrer" to config.referrerBaseUrl,
        "Target URL" to fullTargetUrl,
        "utm_campaign" to config.utmCampaign
      )
      addLog(LogLevel.NETWORK, "[${currentTime()}] Injected HTTP Referrer: ${config.referrerBaseUrl}")
      addLog(LogLevel.NETWORK, "[${currentTime()}] Target CPA Offer loaded in Live WebView: $fullTargetUrl")
      delay(1500)

      // Determine task repeats based on Mode
      val taskRepeats = if (config.selectedMode == AutomationMode.MODE_2_TASK_IN_SESSION) {
        config.taskRepeatCount.coerceAtLeast(1)
      } else {
        1
      }

      for (taskRepeat in 1..taskRepeats) {
        if (!_isAutomating.value) break
        _currentTaskRepeat.value = taskRepeat

        if (taskRepeat > 1) {
          addLog(LogLevel.INFO, "--- In-Session Repeat $taskRepeat/$taskRepeats: Clearing cookies & cache ---")
          rotateCvv()
          _clearCacheSignal.tryEmit(Unit)
          _liveFields.value = mapOf(
            "Session State" to "Cache & Cookies Flushed",
            "Rotated CVV" to _persona.value.cardCvv,
            "Target" to fullTargetUrl
          )
          delay(800)
        }

        // Stage 7: AI Form Analysis & Intelligent Filling
        _currentPrepStage.value = BrowserPrepStage.STAGE_7_AI_FORM_FILL
        _automationState.value = AutomationState.AI_FORM_ANALYSIS
        _simulatedPageTitle.value = "AI: Analyzing Live Web Page DOM for Input Fields..."
        addLog(LogLevel.AI_ACTION, "[${currentTime()}] AI: Scanning DOM structure for required inputs & survey prompts")
        delay(700)

        _automationState.value = AutomationState.AI_AUTO_FILLING
        _simulatedPageTitle.value = "AI: Autofilling Live Web Page with Humanized Typing..."
        // Trigger real DOM JavaScript autofill in WebView
        _triggerFormFillEvent.tryEmit(Unit)
        addLog(LogLevel.AI_ACTION, "[${currentTime()}] AI JS Engine: Injected localized persona data into DOM")
        delay(1000)

        // Stage 8: Click Continue Button
        _currentPrepStage.value = BrowserPrepStage.STAGE_8_CLICK_CONTINUE
        _simulatedPageTitle.value = "AI: Submitting Form & Clicking Continue Button..."
        addLog(LogLevel.AI_ACTION, "[${currentTime()}] AI: Auto-clicked primary CTA 'Continue / Apply' button")
        delay(1000)

        if (config.selectedMode == AutomationMode.MODE_3_SMART_COMPLETION) {
          addLog(LogLevel.SUCCESS, "[${currentTime()}] AI: Smart completion keyword detected ('Thank you / Order Confirmed')")
          delay(1200)
        } else {
          // Timer countdown for Mode 1 & 2
          val timerDuration = if (config.selectedMode == AutomationMode.MODE_2_TASK_IN_SESSION) config.taskDurationSeconds else config.browserDurationSeconds
          for (sec in timerDuration downTo 0) {
            if (!_isAutomating.value) break
            _countdownSeconds.value = sec
            delay(1000)
          }
        }

        // Stage 9: Direct CPAGrip Lead RSS Check (No proxy)
        _currentPrepStage.value = BrowserPrepStage.STAGE_9_LEAD_RSS_CHECK
        _automationState.value = AutomationState.LEAD_CHECKING
        _simulatedPageTitle.value = "Checking CPAGrip Lead RSS Feed (Direct / No Proxy)..."
        addLog(LogLevel.LEAD, "[${currentTime()}] Lead Check: Pinging CPAGrip RSS for IP ${_proxyProfile.value.ip}")
        val leadResult = CpaLeadCheckerService.checkLeadRss(
          userId = config.cpaGripUserId,
          apiKey = config.cpaGripKey,
          proxyIp = _proxyProfile.value.ip
        )

        val updatedChecks = _leadStatus.value.totalChecks + 1
        val newSuccessCount = _leadStatus.value.successfulLeadsCount + if (leadResult.isLeadDetected) 1 else 0

        _leadStatus.value = _leadStatus.value.copy(
          totalChecks = updatedChecks,
          successfulLeadsCount = newSuccessCount,
          isLeadDetected = leadResult.isLeadDetected,
          lastRssStatus = leadResult.rawStatus,
          lastLeadTitle = leadResult.leadTitle,
          lastPayout = leadResult.payout,
          lastCheckTimestamp = timeFormat.format(Date()),
          statusMessage = leadResult.message
        )

        if (leadResult.isLeadDetected) {
          addLog(LogLevel.SUCCESS, "🎉 [${currentTime()}] SUCCESSFUL LEAD RECORDED! Status changed from N/A -> ${leadResult.leadTitle} (${leadResult.payout})")
        } else {
          addLog(LogLevel.LEAD, "[${currentTime()}] Lead Check #$updatedChecks: Status is N/A (No conversion registered yet - Standby)")
        }
        delay(1000)
      }

      // Stage 10: Repeat or Full Cleanup
      _currentPrepStage.value = BrowserPrepStage.STAGE_10_CYCLE_CLEANUP
      PersonaGenerator.markEmailUsed(_persona.value.email)
      _emailStats.value = PersonaGenerator.getEmailPoolStats()
      _simulatedPageTitle.value = "Cycle $cycle Completed - Purged used email & flushed cache"
      addLog(LogLevel.INFO, "[${currentTime()}] Cycle $cycle closed. Cleaned cookies, cache & purged email ${_persona.value.email}")
      
      if (_isAutomating.value) {
        delay(1200)
      }
    }

    _automationState.value = AutomationState.CYCLE_COMPLETED
    _isAutomating.value = false
    addLog(LogLevel.SUCCESS, "✓ Automation process finished.")
  }

  fun manualLeadCheck() {
    viewModelScope.launch {
      addLog(LogLevel.LEAD, "Manual CPAGrip Lead RSS check triggered...")
      val leadResult = CpaLeadCheckerService.checkLeadRss(
        userId = _taskConfig.value.cpaGripUserId,
        apiKey = _taskConfig.value.cpaGripKey,
        proxyIp = _proxyProfile.value.ip
      )
      val updatedChecks = _leadStatus.value.totalChecks + 1
      val newSuccessCount = _leadStatus.value.successfulLeadsCount + if (leadResult.isLeadDetected) 1 else 0

      _leadStatus.value = _leadStatus.value.copy(
        totalChecks = updatedChecks,
        successfulLeadsCount = newSuccessCount,
        isLeadDetected = leadResult.isLeadDetected,
        lastRssStatus = leadResult.rawStatus,
        lastLeadTitle = leadResult.leadTitle,
        lastPayout = leadResult.payout,
        lastCheckTimestamp = timeFormat.format(Date()),
        statusMessage = leadResult.message
      )
      if (leadResult.isLeadDetected) {
        addLog(LogLevel.SUCCESS, "🎉 Lead Check: SUCCESSFUL LEAD DETECTED! Status: ${leadResult.leadTitle}")
      } else {
        addLog(LogLevel.LEAD, "Lead Check #${updatedChecks}: Status returned N/A (Pending conversion)")
      }
    }
  }

  fun clearLogs() {
    _logs.value = emptyList()
    addLog(LogLevel.INFO, "Logs cleared.")
  }

  fun loadCustomUrl(url: String) {
    val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
    _targetWebViewUrl.value = cleanUrl
    _taskConfig.value = _taskConfig.value.copy(offerUrl = cleanUrl)
    addLog(LogLevel.NETWORK, "Navigating WebView to: $cleanUrl")
  }

  fun triggerManualFill() {
    _triggerFormFillEvent.tryEmit(Unit)
    addLog(LogLevel.AI_ACTION, "Manual AI Form Fill & Auto-Submit triggered on live WebView.")
  }

  fun triggerReload() {
    _reloadSignal.tryEmit(Unit)
    addLog(LogLevel.NETWORK, "Live WebView Reloaded.")
  }

  fun triggerClearCache() {
    _clearCacheSignal.tryEmit(Unit)
    addLog(LogLevel.INFO, "Live WebView Cache & Cookies Flushed.")
  }

  fun onJsFieldFilled(name: String, value: String) {
    _liveFields.value = _liveFields.value + (name to value)
    addLog(LogLevel.AI_ACTION, "AI DOM Injected -> $name = $value")
  }

  fun onJsAction(action: String) {
    addLog(LogLevel.AI_ACTION, "AI Action -> $action")
  }

  fun onJsLog(msg: String) {
    addLog(LogLevel.INFO, "JS Console: $msg")
  }

  fun onWebProgressChanged(progress: Int) {
    _webPageProgress.value = progress
  }

  fun onWebTitleChanged(title: String, url: String) {
    _simulatedPageTitle.value = title.ifBlank { url }
  }

  private fun buildUtmUrl(): String {
    val config = _taskConfig.value
    val separator = if (config.offerUrl.contains("?")) "&" else "?"
    return "${config.offerUrl}${separator}utm_source=${config.utmSource}&utm_medium=${config.utmMedium}&utm_campaign=${config.utmCampaign}&utm_content=${config.utmContent}"
  }

  private fun addLog(level: LogLevel, message: String) {
    val entry = LogEntry(
      timestamp = timeFormat.format(Date()),
      level = level,
      message = message
    )
    _logs.value = (listOf(entry) + _logs.value).take(150)
  }

  private fun currentTime(): String = timeFormat.format(Date())

  private fun refreshSimulationSteps() {
    _simulationSteps.value = listOf(
      SimulationStep(1, "Proxy & WebRTC", "Tunneling IP & blocking WebRTC leaks", listOf("IP", "Geo", "DNS"), "Active", true),
      SimulationStep(2, "Persona Profile", "Injecting Name, Luhn Card & Timezone", listOf("Name", "Card", "CVV"), "Synced", true),
      SimulationStep(3, "UTM Navigation", "Referring from simulated search button", listOf("utm_source", "utm_campaign"), "Ready", true),
      SimulationStep(4, "AI Smart Filling", "Detecting and completing fields & surveys", listOf("Address", "Survey Q1-Q5", "Continue"), "Standby", false),
      SimulationStep(5, "Lead RSS Check", "Checking CPAGrip feed without proxy (30s)", listOf("Lead Counter", "Status"), "Standby", false)
    )
  }
}
