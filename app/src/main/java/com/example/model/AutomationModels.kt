package com.example.model

enum class AutomationMode(val displayName: String, val shortDesc: String) {
  MODE_1_BROWSER_CYCLE(
    "Mode 1: Browser Cycle",
    "Timed browser session + full restart per cycle"
  ),
  MODE_2_TASK_IN_SESSION(
    "Mode 2: In-Session Repeat",
    "Clears cache/cookies per repeat without changing persona/proxy"
  ),
  MODE_3_SMART_COMPLETION(
    "Mode 3: Smart Completion",
    "AI detects completion markers & executes repeat cycles"
  )
}

enum class AutomationState {
  IDLE,
  INITIALIZING_PROXY,
  GEO_LOOKUP,
  INJECTING_ENVIRONMENT,
  BROWSER_ACTIVE,
  AI_FORM_ANALYSIS,
  AI_AUTO_FILLING,
  LEAD_CHECKING,
  CYCLE_COMPLETED,
  PAUSED,
  ERROR
}

enum class LogLevel {
  INFO,
  SUCCESS,
  WARNING,
  ERROR,
  AI_ACTION,
  NETWORK,
  LEAD
}

data class LogEntry(
  val id: Long = System.currentTimeMillis() + (0..999).random(),
  val timestamp: String,
  val level: LogLevel,
  val message: String
)

data class Persona(
  val firstName: String,
  val lastName: String,
  val fullName: String = "$firstName $lastName",
  val country: String,
  val countryCode: String,
  val city: String,
  val streetAddress: String,
  val streetAddress2: String = "Apt 4B",
  val state: String = "",
  val zipCode: String,
  val timezone: String,
  val language: String,
  val email: String,
  val phoneNumber: String,
  val phoneAreaCode: String = "",
  val phoneLocalNumber: String = "",
  val birthDate: String, // "15/04/1994"
  val birthDay: String = "15",
  val birthDayPadded: String = "15",
  val birthMonth: String = "04",
  val birthMonthNum: String = "4",
  val birthMonthName: String = "April",
  val birthMonthShort: String = "Apr",
  val birthYear: String = "1994",
  val birthYearShort: String = "94",
  val age: String = "32",
  val gender: String = "Male",
  val genderArabic: String = "ذكر",
  val title: String = "Mr",
  val occupation: String = "Product Reviewer & QA Tester",
  val incomeRange: String = "$50,000 - $75,000",
  val educationLevel: String = "Bachelor's Degree",
  val password: String = "Reviewer2026#Secure",
  val cardNumber: String,
  val cardExpiry: String,
  val cardExpMonth: String = "04",
  val cardExpYear: String = "28",
  val cardExpYearFull: String = "2028",
  val cardCvv: String,
  val cardType: String = "Visa"
)

data class ProxyProfile(
  val ip: String = "175.110.115.169",
  val port: String = "443",
  val protocol: String = "SOCKS5",
  val username: String = "s1izpcpyhj-res-country-US-hold-session-session-6a8c975624ce3",
  val password: String = "i3aKgpXR26QvKsWy",
  val rawProxyInput: String = "socks5://s1izpcpyhj-res-country-US-hold-session-session-6a8c975624ce3:i3aKgpXR26QvKsWy@175.110.115.169:443",
  val countryName: String = "United States",
  val countryCode: String = "US",
  val city: String = "New York",
  val street: String = "742 Evergreen Terrace",
  val timezone: String = "America/New_York (GMT-5)",
  val language: String = "en-US, en;q=0.9",
  val isp: String = "AT&T Residential Proxy",
  val webrtcBlocked: Boolean = true,
  val antiLeakActive: Boolean = true,
  val userAgent: String = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1",
  val isConnected: Boolean = true
)

enum class BrowserPrepStage(val title: String, val desc: String) {
  STAGE_1_PROXY_INIT("1. Opening Proxy & Engine", "Starting secure isolated browser container with proxy tunnel"),
  STAGE_2_GEO_LOOKUP("2. Geo IP & Address Lookup", "Resolving street address, city, timezone & locale via api.i.pn"),
  STAGE_3_LOCALE_INJECTION("3. Timezone & Language Injection", "Spoofing navigator.language, Intl.DateTimeFormat & system clock"),
  STAGE_4_WEBRTC_SHIELD("4. WebRTC Anti-Leak Shield", "Disabling RTCPeerConnection to prevent real local/public IP leaks"),
  STAGE_5_PERSONA_GEN("5. Localized Persona Generation", "Synthesizing identity matching IP country with Luhn-valid card"),
  STAGE_6_NAVIGATE_UTM("6. Navigate to CPA Offer with UTM", "Passing through custom HTTP Referrer with dynamic campaign tags"),
  STAGE_7_AI_FORM_FILL("7. AI Form Analysis & Humanized Fill", "Detecting input elements & typing responses with random delay"),
  STAGE_8_CLICK_CONTINUE("8. Smart Continue Action", "Locating primary CTA button & simulating human touch coordinates"),
  STAGE_9_LEAD_RSS_CHECK("9. CPAGrip Lead RSS Verification", "Direct background check (no proxy) via lead_check_rss.php endpoint"),
  STAGE_10_CYCLE_CLEANUP("10. Repeat / Sanitize & Wipe", "Clearing cache/cookies or cycling persona and closing browser")
}

data class TaskConfig(
  val offerUrl: String = "https://rileymarker.com/show.php?l=0&u=2227942&id=74924",
  val referrerBaseUrl: String = "https://google.com/search",
  val utmSource: String = "google",
  val utmMedium: String = "cpc",
  val utmCampaign: String = "review_jobs_promo",
  val utmContent: String = "cta_apply_now",
  val browserDurationSeconds: Int = 55,
  val taskDurationSeconds: Int = 45,
  val leadCheckIntervalSeconds: Int = 30,
  val processRepeatCount: Int = 3,
  val taskRepeatCount: Int = 2,
  val unlimitedCycles: Boolean = true,
  val autoRotateProxyEachCycle: Boolean = true,
  val proxyListUrl: String = "https://asocks-list.org/WL8AfPijnDM9U9mbo4uH8d5FAd1HS2sS.txt?limit=1000&type=res&template_id=4&country=US",
  val cpaGripUserId: String = "2227942",
  val cpaGripKey: String = "c8c9f000dc666b8efb670b90ccb17aff",
  val checkWithoutProxy: Boolean = true,
  val selectedMode: AutomationMode = AutomationMode.MODE_3_SMART_COMPLETION,
  val selectedUserAgentIndex: Int = 0,
  val humanSimulationEnabled: Boolean = true,
  val humanTypingSpeedMs: Int = 45,
  val humanSmoothScroll: Boolean = true,
  val humanTouchTrajectory: Boolean = true,
  val humanHesitationDelayMs: Int = 320,
  val antiDetectionSpoofing: Boolean = true
)

data class LeadStatus(
  val totalChecks: Int = 0,
  val successfulLeadsCount: Int = 0,
  val isLeadDetected: Boolean = false,
  val lastRssStatus: String = "N/A",
  val lastLeadTitle: String = "",
  val lastPayout: String = "$0.00",
  val lastCheckTimestamp: String = "Just now",
  val statusMessage: String = "Routine Monitor: Standby (Awaiting CPA conversion)"
) {
  val detectedLeadsCount: Int
    get() = successfulLeadsCount
}

data class SimulationStep(
  val stepNumber: Int,
  val title: String,
  val description: String,
  val fieldsFilled: List<String>,
  val status: String,
  val isCompleted: Boolean
)
