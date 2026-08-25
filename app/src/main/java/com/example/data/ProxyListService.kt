package com.example.data

import android.util.Log
import com.example.model.ProxyProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object ProxyListService {

  private const val TAG = "ProxyListService"
  const val DEFAULT_ASOCKS_URL =
    "https://asocks-list.org/WL8AfPijnDM9U9mbo4uH8d5FAd1HS2sS.txt?limit=1000&type=res&template_id=4&country=US"

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  private val proxyPool = mutableListOf<ProxyItem>()
  private val currentIndex = AtomicInteger(0)

  data class ProxyItem(
    val ip: String,
    val port: String,
    val protocol: String = "SOCKS5",
    val username: String = "",
    val password: String = "",
    val raw: String = ""
  )

  init {
    // Populate default high quality residential proxies
    loadInitialDefaultPool()
  }

  private fun loadInitialDefaultPool() {
    proxyPool.clear()
    proxyPool.addAll(
      listOf(
        ProxyItem("175.110.115.169", "443", "SOCKS5", "s1izpcpyhj-res-country-US-hold-session-session-6a8c975624ce3", "i3aKgpXR26QvKsWy", "socks5://s1izpcpyhj-res-country-US-hold-session-session-6a8c975624ce3:i3aKgpXR26QvKsWy@175.110.115.169:443"),
        ProxyItem("198.54.133.112", "1080", "SOCKS5", "s1izpcpyhj-res-country-US-session-101", "i3aKgpXR26QvKsWy", "socks5://s1izpcpyhj-res-country-US-session-101:i3aKgpXR26QvKsWy@198.54.133.112:1080"),
        ProxyItem("104.238.191.75", "443", "SOCKS5", "s1izpcpyhj-res-country-US-session-102", "i3aKgpXR26QvKsWy", "socks5://s1izpcpyhj-res-country-US-session-102:i3aKgpXR26QvKsWy@104.238.191.75:443"),
        ProxyItem("142.11.218.188", "1080", "SOCKS5", "s1izpcpyhj-res-country-US-session-103", "i3aKgpXR26QvKsWy", "socks5://s1izpcpyhj-res-country-US-session-103:i3aKgpXR26QvKsWy@142.11.218.188:1080"),
        ProxyItem("192.241.220.145", "443", "SOCKS5", "s1izpcpyhj-res-country-US-session-104", "i3aKgpXR26QvKsWy", "socks5://s1izpcpyhj-res-country-US-session-104:i3aKgpXR26QvKsWy@192.241.220.145:443")
      )
    )
  }

  suspend fun fetchProxiesFromUrl(url: String = DEFAULT_ASOCKS_URL): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val targetUrl = url.trim().ifBlank { DEFAULT_ASOCKS_URL }
      val request = Request.Builder()
        .url(targetUrl)
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .build()

      val response = client.newCall(request).execute()
      val body = response.body?.string()

      if (response.isSuccessful && !body.isNullOrBlank()) {
        val parsed = parseProxyListText(body)
        if (parsed.isNotEmpty()) {
          synchronized(proxyPool) {
            proxyPool.clear()
            proxyPool.addAll(parsed)
            currentIndex.set(0)
          }
          Log.i(TAG, "Successfully fetched and parsed ${parsed.size} proxies from $targetUrl")
          return@withContext Result.success(parsed.size)
        }
      }
      Result.failure(Exception("HTTP ${response.code}: Empty or invalid proxy response"))
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching proxies from $url: ${e.message}", e)
      Result.failure(e)
    }
  }

  fun parseProxyListText(rawText: String): List<ProxyItem> {
    val items = mutableListOf<ProxyItem>()
    val lines = rawText.split("\n", "\r\n")

    for (rawLine in lines) {
      val line = rawLine.trim()
      if (line.isBlank() || line.startsWith("#") || line.startsWith("//")) continue

      val parsed = parseSingleProxyLine(line)
      if (parsed != null) {
        items.add(parsed)
      }
    }
    return items
  }

  fun parseSingleProxyLine(raw: String): ProxyItem? {
    val clean = raw.trim()
    if (clean.isBlank()) return null

    try {
      // 1. Check URI format like socks5://user:pass@ip:port or http://user:pass@ip:port
      if (clean.contains("://")) {
        val proto = clean.substringBefore("://").uppercase()
        val rest = clean.substringAfter("://")
        val (authPart, hostPortPart) = if (rest.contains("@")) {
          val parts = rest.split("@", limit = 2)
          parts[0] to parts[1]
        } else {
          "" to rest
        }

        val (user, pass) = if (authPart.contains(":")) {
          val parts = authPart.split(":", limit = 2)
          parts[0] to parts[1]
        } else {
          authPart to ""
        }

        val (ip, port) = if (hostPortPart.contains(":")) {
          val parts = hostPortPart.split(":", limit = 2)
          parts[0] to parts[1]
        } else {
          hostPortPart to "443"
        }

        return ProxyItem(
          ip = ip.trim(),
          port = port.trim(),
          protocol = if (proto.contains("SOCKS")) "SOCKS5" else "HTTP",
          username = user.trim(),
          password = pass.trim(),
          raw = clean
        )
      }

      // 2. Colon separated format: IP:PORT:USER:PASS or IP:PORT or USER:PASS:IP:PORT
      val parts = clean.split(":")
      when (parts.size) {
        4 -> {
          // Could be ip:port:user:pass or user:pass:ip:port
          val p0IsIp = parts[0].matches(Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}"""))
          val p2IsIp = parts[2].matches(Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}"""))

          return if (p0IsIp) {
            ProxyItem(
              ip = parts[0].trim(),
              port = parts[1].trim(),
              protocol = "SOCKS5",
              username = parts[2].trim(),
              password = parts[3].trim(),
              raw = clean
            )
          } else if (p2IsIp) {
            ProxyItem(
              ip = parts[2].trim(),
              port = parts[3].trim(),
              protocol = "SOCKS5",
              username = parts[0].trim(),
              password = parts[1].trim(),
              raw = clean
            )
          } else {
            ProxyItem(
              ip = parts[0].trim(),
              port = parts[1].trim(),
              protocol = "SOCKS5",
              username = parts[2].trim(),
              password = parts[3].trim(),
              raw = clean
            )
          }
        }
        2 -> {
          return ProxyItem(
            ip = parts[0].trim(),
            port = parts[1].trim(),
            protocol = "SOCKS5",
            username = "",
            password = "",
            raw = clean
          )
        }
        3 -> {
          return ProxyItem(
            ip = parts[0].trim(),
            port = parts[1].trim(),
            protocol = "SOCKS5",
            username = parts[2].trim(),
            password = "",
            raw = clean
          )
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed parsing proxy line: $raw, err: ${e.message}")
    }
    return null
  }

  fun getNextProxy(): ProxyItem {
    synchronized(proxyPool) {
      if (proxyPool.isEmpty()) {
        loadInitialDefaultPool()
      }
      val idx = currentIndex.getAndIncrement() % proxyPool.size
      return proxyPool[idx]
    }
  }

  fun getPoolSize(): Int = synchronized(proxyPool) { proxyPool.size }

  fun getCurrentIndex(): Int = currentIndex.get() % (if (proxyPool.isNotEmpty()) proxyPool.size else 1)

  fun getAllProxies(): List<ProxyItem> = synchronized(proxyPool) { proxyPool.toList() }
}
