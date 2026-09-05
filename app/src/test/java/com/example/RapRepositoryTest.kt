package com.example

import com.example.data.RapRepository
import com.example.model.PromptOptions
import org.junit.Assert.assertTrue
import org.junit.Test

class RapRepositoryTest {
  @Test
  fun prompt_contains_tunisian_source_and_five_passes() {
    val prompt = RapRepository.buildPrompt(PromptOptions())
    assertTrue(prompt.contains("القاموس التونسي"))
    assertTrue(prompt.contains("المرّة 5"))
    assertTrue(prompt.contains("100%"))
  }

  @Test
  fun generated_song_is_long_enough_for_the_quality_gate() {
    val options = PromptOptions(selectedWords = listOf("حومة", "برشا", "قدّام", "حلم"))
    val lyrics = RapRepository.generateLyrics(options)
    val report = RapRepository.analyse(options.title, lyrics)
    assertTrue(lyrics.length > 240)
    assertTrue(report.checks.size == 5)
    assertTrue(report.overallScore > 0)
  }
}
