package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Energy
import com.example.model.QaReport
import com.example.model.QaStatus
import com.example.model.QualityCheck
import com.example.model.RapStyle
import com.example.model.Song
import com.example.model.SongStatus
import org.json.JSONArray
import org.json.JSONObject

/** Small JSON-backed store. The default constructor keeps previews and unit tests in memory. */
class SongStore private constructor(private val preferences: SharedPreferences?) {
  constructor() : this(null)
  constructor(context: Context) : this(context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE))

  fun load(seed: List<Song>): List<Song> {
    val raw = preferences?.getString(KEY_SONGS, null) ?: return seed
    return try {
      val saved = decode(JSONArray(raw))
      seed + saved.filterNot { stored -> seed.any { it.id == stored.id } }
    } catch (_: Exception) {
      seed
    }
  }

  fun save(songs: List<Song>) {
    val editor = preferences?.edit() ?: return
    val customSongs = songs.filterNot { it.id.startsWith("seed-") }
    editor.putString(KEY_SONGS, JSONArray().apply { customSongs.forEach { put(encode(it)) } }.toString()).apply()
  }

  private fun encode(song: Song): JSONObject = JSONObject().apply {
    put("id", song.id)
    put("title", song.title)
    put("theme", song.theme)
    put("style", song.style.name)
    put("energy", song.energy.name)
    put("lyrics", song.lyrics)
    put("prompt", song.prompt)
    put("status", song.status.name)
    put("createdAt", song.createdAt)
    put("favorite", song.isFavorite)
    put("score", song.qaReport.overallScore)
    put("passed", song.qaReport.passedPasses)
    put("checks", JSONArray().apply {
      song.qaReport.checks.forEach { check ->
        put(JSONObject().apply {
          put("name", check.name)
          put("detail", check.detail)
          put("status", check.status.name)
          put("score", check.score)
        })
      }
    })
  }

  private fun decode(array: JSONArray): List<Song> = buildList {
    for (index in 0 until array.length()) {
      val item = array.getJSONObject(index)
      val checks = buildList {
        val checkArray = item.optJSONArray("checks") ?: JSONArray()
        for (checkIndex in 0 until checkArray.length()) {
          val check = checkArray.getJSONObject(checkIndex)
          add(QualityCheck(check.optString("name"), check.optString("detail"), enumOr(check.optString("status"), QaStatus.PASS), check.optInt("score")))
        }
      }
      add(
        Song(
          id = item.optString("id"),
          title = item.optString("title"),
          theme = item.optString("theme"),
          style = enumOr(item.optString("style"), RapStyle.TRAP),
          energy = enumOr(item.optString("energy"), Energy.BALANCED),
          lyrics = item.optString("lyrics"),
          prompt = item.optString("prompt"),
          status = enumOr(item.optString("status"), SongStatus.DRAFT),
          qaReport = QaReport(checks, item.optInt("score"), item.optInt("passed")),
          createdAt = item.optString("createdAt"),
          isFavorite = item.optBoolean("favorite")
        )
      )
    }
  }

  private inline fun <reified T : Enum<T>> enumOr(value: String, fallback: T): T = try {
    enumValueOf(value)
  } catch (_: IllegalArgumentException) {
    fallback
  }

  private companion object {
    const val FILE_NAME = "tounsi_rap_library"
    const val KEY_SONGS = "songs"
  }
}
