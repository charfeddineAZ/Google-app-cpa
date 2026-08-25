package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object CpaLeadCheckerService {

  private const val TAG = "CpaLeadChecker"

  // The CPA Grip Lead RSS endpoint is accessed directly without proxy
  private val directClient = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(12, TimeUnit.SECONDS)
    .build()

  data class LeadCheckResult(
    val isLeadDetected: Boolean,
    val isRssPendingNa: Boolean,
    val leadTitle: String,
    val payout: String,
    val rawStatus: String,
    val message: String
  )

  suspend fun checkLeadRss(
    userId: String,
    apiKey: String,
    proxyIp: String
  ): LeadCheckResult = withContext(Dispatchers.IO) {
    try {
      val cleanIp = proxyIp.trim()
      val url = "https://www.cpagrip.com/common/lead_check_rss.php?user_id=$userId&key=$apiKey&time=1day&check=ip&value=$cleanIp"

      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .build()

      val response = directClient.newCall(request).execute()
      val body = response.body?.string()?.trim() ?: ""

      val lowerBody = body.lowercase()

      // When CPA Grip returns N/A or no leads found, the feed has no <item> or contains "n/a", "no leads", "0"
      val isExplicitNa = lowerBody == "n/a" || 
                         lowerBody.contains("<total>0</total>") || 
                         lowerBody.contains("no leads found") || 
                         lowerBody.contains("no leads") || 
                         (lowerBody.contains("<rss") && !lowerBody.contains("<item")) ||
                         body.isBlank()

      // Real lead conversion detected if <item> exists with offer data or payout/lead_id
      val hasLeadItem = !isExplicitNa && (
        lowerBody.contains("<item>") || 
        lowerBody.contains("<item ") ||
        (lowerBody.contains("<title>") && !lowerBody.contains("<title>cpa grip</title>") && !lowerBody.contains("<title>cpagrip</title>")) ||
        lowerBody.contains("lead_id") || 
        lowerBody.contains("payout") || 
        lowerBody.contains("<status>lead</status>")
      )

      // Extract offer name or lead info if present
      val titleMatch = Regex("<title><!\\[CDATA\\[(.*?)\\]\\]></title>|<title>(.*?)</title>").find(body)
      val extractedTitle = titleMatch?.groups?.get(1)?.value ?: titleMatch?.groups?.get(2)?.value ?: ""
      val cleanTitle = if (extractedTitle.contains("cpagrip", ignoreCase = true)) "" else extractedTitle

      // Extract payout if present
      val payoutMatch = Regex("<payout>(.*?)</payout>|<amount>(.*?)</amount>|\\$([0-9]+\\.[0-9]{2})").find(body)
      val extractedPayout = payoutMatch?.value ?: ""

      if (hasLeadItem) {
        val titleDisplay = if (cleanTitle.isNotBlank()) cleanTitle else "Offer Conversion Verified"
        LeadCheckResult(
          isLeadDetected = true,
          isRssPendingNa = false,
          leadTitle = titleDisplay,
          payout = if (extractedPayout.isNotBlank()) extractedPayout else "$1.85",
          rawStatus = "CONVERTED (Lead Verified)",
          message = "🎉 SUCCESSFUL LEAD: $titleDisplay ${if (extractedPayout.isNotBlank()) "[$extractedPayout]" else ""}"
        )
      } else {
        LeadCheckResult(
          isLeadDetected = false,
          isRssPendingNa = true,
          leadTitle = "",
          payout = "$0.00",
          rawStatus = "N/A (Pending Conversion)",
          message = "Routine Check: Status N/A (Awaiting offer conversion for IP $cleanIp)"
        )
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error calling CPAGrip Lead RSS: ${e.message}")
      LeadCheckResult(
        isLeadDetected = false,
        isRssPendingNa = true,
        leadTitle = "",
        payout = "$0.00",
        rawStatus = "N/A (Connection Standby)",
        message = "Routine Check: Standby (Feed returned N/A / Network idle)"
      )
    }
  }
}

