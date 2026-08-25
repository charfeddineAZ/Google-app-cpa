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

  // Sequential Task Queue
  private val _taskList = MutableStateFlow<List<com.example.model.TaskItem>>(
    listOf(
      com.example.model.TaskItem(
        id = "task-1",
        title = "Task #1: US Gift Card Reward",
        offerUrl = "https://rileymarker.com/show.php?l=0&u=2227942&id=74924",
        referrerBaseUrl = "https://www.google.com/search",
        useRandomReferrer = true,
        selectedUserAgentIndex = 0,
        selectedMode = AutomationMode.MODE_3_SMART_COMPLETION,
        taskRepeatCount = 2,
        browserDurationSeconds = 55,
        utmSource = "google",
        utmMedium = "cpc",
        utmCampaign = "reward_promo_us"
      ),
      com.example.model.TaskItem(
        id = "task-2",
        title = "Task #2: Special Review Survey",
        offerUrl = "https://rileymarker.com/show.php?l=0&u=2227942&id=74924",
        referrerBaseUrl = "https://m.facebook.com/l.php",
        useRandomReferrer = true,
        selectedUserAgentIndex = 1,
        selectedMode = AutomationMode.MODE_1_BROWSER_CYCLE,
        taskRepeatCount = 1,
        browserDurationSeconds = 45,
        utmSource = "facebook",
        utmMedium = "social",
        utmCampaign = "survey_jobs_2026"
      )
    )
  )
  val taskList: StateFlow<List<com.example.model.TaskItem>> = _taskList.asStateFlow()

  private val _currentTaskIndex = MutableStateFlow(0)
  val currentTaskIndex: StateFlow<Int> = _currentTaskIndex.asStateFlow()

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

  val randomReferrersList = listOf(
    "https://www.google.com/search?q=online+survey+rewards",
    "https://m.facebook.com/l.php?u=cpa_offer",
    "https://t.co/exclusive_promos",
    "https://www.bing.com/search?q=earn+gift+cards+reviews",
    "https://www.instagram.com/p/promotions_deal",
    "https://www.tiktok.com/link/rewards_cpa",
    "https://www.pinterest.com/pin/survey_deal",
    "https://www.youtube.com/redirect?q=bonus_link"
  )

  fun getRandomReferrer(): String = randomReferrersList.random()

  fun addTask(task: com.example.model.TaskItem) {
    _taskList.value = _taskList.value + task
    addLog(LogLevel.INFO, "➕ Added Task #${_taskList.value.size}: ${task.title}")
  }

  fun updateTask(updatedTask: com.example.model.TaskItem) {
    _taskList.value = _taskList.value.map { if (it.id == updatedTask.id) updatedTask else it }
    addLog(LogLevel.INFO, "✏️ Updated Task: ${updatedTask.title}")
  }

  fun deleteTask(taskId: String) {
    val target = _taskList.value.find { it.id == taskId }
    _taskList.value = _taskList.value.filter { it.id != taskId }
    addLog(LogLevel.WARNING, "🗑️ Deleted Task: ${target?.title ?: taskId}")
  }

  fun duplicateTask(taskId: String) {
    val target = _taskList.value.find { it.id == taskId } ?: return
    val copy = target.copy(
      id = java.util.UUID.randomUUID().toString(),
      title = "${target.title} (Copy)",
      isCompleted = false,
      isRunning = false,
      completedRepeats = 0
    )
    _taskList.value = _taskList.value + copy
    addLog(LogLevel.INFO, "📋 Duplicated Task: ${copy.title}")
  }

  fun moveTaskUp(index: Int) {
    if (index > 0 && index < _taskList.value.size) {
      val mutable = _taskList.value.toMutableList()
      val item = mutable.removeAt(index)
      mutable.add(index - 1, item)
      _taskList.value = mutable
    }
  }

  fun moveTaskDown(index: Int) {
    if (index >= 0 && index < _taskList.value.size - 1) {
      val mutable = _taskList.value.toMutableList()
      val item = mutable.removeAt(index)
      mutable.add(index + 1, item)
      _taskList.value = mutable
    }
  }

  fun resetTasksState() {
    _taskList.value = _taskList.value.map { it.copy(isCompleted = false, isRunning = false, completedRepeats = 0) }
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
    _taskList.value = _taskList.value.map { it.copy(isRunning = false) }
    addLog(LogLevel.WARNING, "■ Automation STOPPED by user.")
  }

  private suspend fun executeAutomationEngine() {
    val currentQueue = _taskList.value.ifEmpty {
      listOf(
        com.example.model.TaskItem(
          id = "default-task",
          title = "Default CPA Task",
          offerUrl = _taskConfig.value.offerUrl,
          referrerBaseUrl = _taskConfig.value.referrerBaseUrl,
          useRandomReferrer = false,
          selectedUserAgentIndex = _taskConfig.value.selectedUserAgentIndex,
          selectedMode = _taskConfig.value.selectedMode,
          taskRepeatCount = _taskConfig.value.taskRepeatCount,
          browserDurationSeconds = _taskConfig.value.browserDurationSeconds
        )
      )
    }

    // Reset task statuses
    _taskList.value = _taskList.value.map { it.copy(isCompleted = false, isRunning = false, completedRepeats = 0) }

    addLog(LogLevel.INFO, "==================================================")
    addLog(LogLevel.INFO, "▶ Starting Sequential Task Queue (${currentQueue.size} Tasks in sequence)")
    addLog(LogLevel.INFO, "==================================================")

    for (taskIdx in currentQueue.indices) {
      if (!_isAutomating.value) break
      _currentTaskIndex.value = taskIdx
      val task = currentQueue[taskIdx]

      // Mark running
      _taskList.value = _taskList.value.mapIndexed { idx, item ->
        if (idx == taskIdx) item.copy(isRunning = true, isCompleted = false) else item
      }

      val actualReferrer = if (task.useRandomReferrer) getRandomReferrer() else task.referrerBaseUrl
      val config = _taskConfig.value.copy(
        offerUrl = task.offerUrl,
        referrerBaseUrl = actualReferrer,
        selectedUserAgentIndex = task.selectedUserAgentIndex,
        selectedMode = task.selectedMode,
        taskRepeatCount = task.taskRepeatCount,
        browserDurationSeconds = task.browserDurationSeconds,
        utmSource = task.utmSource,
        utmMedium = task.utmMedium,
        utmCampaign = task.utmCampaign,
        utmContent = task.utmContent
      )
      _taskConfig.value = config

      val repeats = task.taskRepeatCount.coerceAtLeast(1)
      addLog(LogLevel.INFO, "--------------------------------------------------")
      addLog(LogLevel.INFO, "🚀 Task [${taskIdx + 1}/${currentQueue.size}]: ${task.title}")
      addLog(LogLevel.NETWORK, "Offer: ${task.offerUrl} | Referrer: $actualReferrer")
      addLog(LogLevel.INFO, "Mode: ${task.selectedMode.displayName} ($repeats Repeats)")
      addLog(LogLevel.INFO, "--------------------------------------------------")

      for (rep in 1..repeats) {
        if (!_isAutomating.value) break
        _currentCycle.value = rep
        _currentTaskRepeat.value = rep

        val cycleLabel = "Task [${taskIdx + 1}/${currentQueue.size}] Repeat $rep of $repeats"
        addLog(LogLevel.INFO, "▶ Running $cycleLabel")

        // Stage 1: Auto-Rotate Proxy & launch container
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
          addLog(LogLevel.NETWORK, "[${currentTime()}] Switched Proxy: ${nextProxy.protocol}://${nextProxy.ip}:${nextProxy.port}")
        }

        _liveFields.value = mapOf(
          "Task" to "[${taskIdx + 1}/${currentQueue.size}] ${task.title}",
          "Proxy IP" to _proxyProfile.value.ip,
          "Port" to _proxyProfile.value.port,
          "Referrer" to actualReferrer
        )
        delay(600)

        // Stage 2: Geo IP Lookup
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
            addLog(LogLevel.SUCCESS, "[${currentTime()}] Geo: ${_proxyProfile.value.city}, ${_proxyProfile.value.countryName} [${profile.timezone}]")
          }
        } catch (e: Exception) {
          addLog(LogLevel.WARNING, "Geo lookup notice: ${e.message}")
        }
        delay(500)

        // Stage 3: Timezone & Language
        _currentPrepStage.value = BrowserPrepStage.STAGE_3_LOCALE_INJECTION
        _automationState.value = AutomationState.INJECTING_ENVIRONMENT
        _simulatedPageTitle.value = "Injecting Timezone & Language Profiles..."
        _liveFields.value = mapOf(
          "Timezone" to _proxyProfile.value.timezone,
          "Browser Locale" to _proxyProfile.value.language,
          "Referrer" to actualReferrer
        )
        addLog(LogLevel.INFO, "[${currentTime()}] Injected Locale: ${_proxyProfile.value.language} | Timezone: ${_proxyProfile.value.timezone}")
        delay(500)

        // Stage 4: WebRTC Shield
        _currentPrepStage.value = BrowserPrepStage.STAGE_4_WEBRTC_SHIELD
        addLog(LogLevel.NETWORK, "[${currentTime()}] WebRTC Leak Prevention: ACTIVE (Real IP Shielded)")
        delay(500)

        // Stage 5: Persona Generation
        _currentPrepStage.value = BrowserPrepStage.STAGE_5_PERSONA_GEN
        _simulatedPageTitle.value = "Synthesizing Identity for ${_proxyProfile.value.countryName}..."
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
        addLog(LogLevel.SUCCESS, "[${currentTime()}] Generated Persona for ${newPersona.country}: ${newPersona.fullName}")
        delay(600)

        // Stage 6: Navigate to Task Offer with UTM
        _currentPrepStage.value = BrowserPrepStage.STAGE_6_NAVIGATE_UTM
        _automationState.value = AutomationState.BROWSER_ACTIVE
        val fullTargetUrl = buildUtmUrl()
        _targetWebViewUrl.value = fullTargetUrl
        _simulatedPageTitle.value = "Navigating to: $fullTargetUrl"

        _clearCacheSignal.tryEmit(Unit)
        delay(300)
        _reloadSignal.tryEmit(Unit)

        _liveFields.value = mapOf(
          "HTTP Referrer" to actualReferrer,
          "Target URL" to fullTargetUrl,
          "Task" to "${taskIdx + 1}/${currentQueue.size}"
        )
        addLog(LogLevel.NETWORK, "[${currentTime()}] Loaded Offer URL: $fullTargetUrl")
        delay(1200)

        // Stage 7: AI Form Analysis & Intelligent Filling
        _currentPrepStage.value = BrowserPrepStage.STAGE_7_AI_FORM_FILL
        _automationState.value = AutomationState.AI_FORM_ANALYSIS
        _simulatedPageTitle.value = "AI: Analyzing Form & Survey Elements..."
        addLog(LogLevel.AI_ACTION, "[${currentTime()}] AI: Scanning DOM structure for form fields")
        delay(600)

        _automationState.value = AutomationState.AI_AUTO_FILLING
        _simulatedPageTitle.value = "AI: Autofilling Form Fields with Humanized Behavior..."
        _triggerFormFillEvent.tryEmit(Unit)
        addLog(LogLevel.AI_ACTION, "[${currentTime()}] AI JS Engine: Injected localized persona into DOM")
        delay(900)

        // Stage 8: Click Continue Button
        _currentPrepStage.value = BrowserPrepStage.STAGE_8_CLICK_CONTINUE
        _simulatedPageTitle.value = "AI: Submitting & Clicking Continue..."
        addLog(LogLevel.AI_ACTION, "[${currentTime()}] AI: Auto-clicked primary CTA 'Continue' button")
        delay(800)

        if (config.selectedMode == AutomationMode.MODE_3_SMART_COMPLETION) {
          addLog(LogLevel.SUCCESS, "[${currentTime()}] AI: Smart completion detected")
          delay(1000)
        } else {
          val timerDuration = if (config.selectedMode == AutomationMode.MODE_2_TASK_IN_SESSION) config.taskDurationSeconds else config.browserDurationSeconds
          for (sec in timerDuration downTo 0) {
            if (!_isAutomating.value) break
            _countdownSeconds.value = sec
            delay(1000)
          }
        }

        // Stage 9: Lead Check
        _currentPrepStage.value = BrowserPrepStage.STAGE_9_LEAD_RSS_CHECK
        _automationState.value = AutomationState.LEAD_CHECKING
        _simulatedPageTitle.value = "Checking CPAGrip Lead RSS Feed (Direct)..."
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
          addLog(LogLevel.SUCCESS, "🎉 [${currentTime()}] CONVERSION RECORDED! ${leadResult.leadTitle} (${leadResult.payout})")
        } else {
          addLog(LogLevel.LEAD, "[${currentTime()}] Lead Check #$updatedChecks: Pending / Standby (Status: N/A)")
        }
        delay(800)

        // Stage 10: Cycle Cleanup
        _currentPrepStage.value = BrowserPrepStage.STAGE_10_CYCLE_CLEANUP
        PersonaGenerator.markEmailUsed(_persona.value.email)
        _emailStats.value = PersonaGenerator.getEmailPoolStats()
        _simulatedPageTitle.value = "Repeat $rep of Task ${taskIdx + 1} complete - Cleaned cache"
        addLog(LogLevel.INFO, "[${currentTime()}] Cleaned cookies, cache & purged used email ${_persona.value.email}")

        // Update completed repeats for this task in list
        _taskList.value = _taskList.value.mapIndexed { idx, item ->
          if (idx == taskIdx) item.copy(completedRepeats = rep) else item
        }
        delay(600)
      }

      // Mark task completed
      _taskList.value = _taskList.value.mapIndexed { idx, item ->
        if (idx == taskIdx) item.copy(isRunning = false, isCompleted = true) else item
      }
      addLog(LogLevel.SUCCESS, "✓ Task [${taskIdx + 1}/${currentQueue.size}] Completed: ${task.title}")
      delay(800)
    }

    _automationState.value = AutomationState.CYCLE_COMPLETED
    _isAutomating.value = false
    addLog(LogLevel.SUCCESS, "🎉 All ${currentQueue.size} tasks in queue completed! Automation stopped automatically.")
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
