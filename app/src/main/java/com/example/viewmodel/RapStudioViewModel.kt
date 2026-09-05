package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.RapRepository
import com.example.data.SongStore
import com.example.model.AppTab
import com.example.model.Energy
import com.example.model.PromptOptions
import com.example.model.RapStyle
import com.example.model.Song
import com.example.model.SongStatus
import com.example.model.StudioState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RapStudioViewModel(private val songStore: SongStore = SongStore()) : ViewModel() {
  private val _activeTab = MutableStateFlow(AppTab.STUDIO)
  val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

  private val _studio = MutableStateFlow(StudioState())
  val studio: StateFlow<StudioState> = _studio.asStateFlow()

  private val _songs = MutableStateFlow(songStore.load(RapRepository.seedSongs()))
  val songs: StateFlow<List<Song>> = _songs.asStateFlow()

  private val _dictionary = MutableStateFlow(RapRepository.dictionary)
  val dictionary = _dictionary.asStateFlow()

  init {
    refreshPrompt()
  }

  fun selectTab(tab: AppTab) {
    _activeTab.value = tab
  }

  fun updateTitle(title: String) = updateOptions { copy(title = title) }
  fun updateTheme(theme: String) = updateOptions { copy(theme = theme) }
  fun setStyle(style: RapStyle) = updateOptions { copy(style = style) }
  fun setEnergy(energy: Energy) = updateOptions { copy(energy = energy) }

  fun toggleWord(word: String) {
    updateOptions {
      val words = selectedWords.toMutableList()
      if (word in words) words.remove(word) else if (words.size < 8) words.add(word)
      copy(selectedWords = words)
    }
  }

  private fun updateOptions(change: PromptOptions.() -> PromptOptions) {
    val next = _studio.value.options.change()
    _studio.value = _studio.value.copy(options = next)
    refreshPrompt()
  }

  private fun refreshPrompt() {
    val options = _studio.value.options
    _studio.value = _studio.value.copy(generatedPrompt = RapRepository.buildPrompt(options))
  }

  fun generateSong() {
    if (_studio.value.isGenerating) return
    viewModelScope.launch {
      val options = _studio.value.options
      _studio.value = _studio.value.copy(isGenerating = true, lastAction = "نستخرج الكلمات من القاموس ونراجع النص…")
      delay(280)
      val lyrics = RapRepository.generateLyrics(options)
      val report = RapRepository.analyse(options.title, lyrics)
      _studio.value = _studio.value.copy(
        generatedLyrics = lyrics,
        report = report,
        isGenerating = false,
        lastAction = "تعدّت الأغنية ${report.passedPasses}/${report.totalPasses} من بوابات المراجعة"
      )
    }
  }

  fun saveGeneratedSong() {
    val state = _studio.value
    if (state.generatedLyrics.isBlank()) return
    val options = state.options
    val newSong = Song(
      id = state.editingSongId ?: "song-${UUID.randomUUID()}",
      title = options.title.ifBlank { "أغنية بلا عنوان" },
      theme = options.theme,
      style = options.style,
      energy = options.energy,
      lyrics = state.generatedLyrics,
      prompt = state.generatedPrompt,
      status = if (state.report.isReady) SongStatus.REVIEWED else SongStatus.DRAFT,
      qaReport = state.report,
      createdAt = RapRepository.nowLabel()
    )
    _songs.value = if (state.editingSongId == null) {
      listOf(newSong) + _songs.value
    } else {
      _songs.value.map { if (it.id == state.editingSongId) newSong else it }
    }
    songStore.save(_songs.value)
    _studio.value = state.copy(editingSongId = newSong.id, lastAction = "تحفظت في المكتبة — تنجم ترجع تعدّلها وقت تحب")
    _activeTab.value = AppTab.LIBRARY
  }

  fun toggleFavorite(id: String) {
    _songs.value = _songs.value.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
    songStore.save(_songs.value)
  }

  fun deleteSong(id: String) {
    _songs.value = _songs.value.filterNot { it.id == id }
    songStore.save(_songs.value)
  }

  fun openSong(song: Song) {
    val options = PromptOptions(song.title, song.theme, song.style, song.energy)
    _studio.value = StudioState(
      editingSongId = song.id,
      options = options,
      generatedLyrics = song.lyrics,
      generatedPrompt = song.prompt,
      report = song.qaReport,
      lastAction = "تفتحت من المكتبة"
    )
    _activeTab.value = AppTab.STUDIO
  }

  fun newSong() {
    _studio.value = StudioState(options = PromptOptions())
    refreshPrompt()
    _activeTab.value = AppTab.STUDIO
  }

  companion object {
    fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RapStudioViewModel::class.java)) {
          return RapStudioViewModel(SongStore(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
      }
    }
  }
}
