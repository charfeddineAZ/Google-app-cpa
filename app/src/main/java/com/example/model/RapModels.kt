package com.example.model

/** The four workspaces in the studio. */
enum class AppTab(val label: String) {
  STUDIO("الاستوديو"),
  LIBRARY("المكتبة"),
  DICTIONARY("القاموس"),
  PROMPT("البرومبة")
}

enum class SongStatus(val label: String) {
  DRAFT("مسودة"),
  REVIEWED("مراجعة كاملة"),
  PUBLISHED("جاهزة")
}

enum class RapStyle(val label: String, val description: String) {
  TRAP("تراب", "808 ثقيل وفلو متقطّع"),
  OLD_SCHOOL("أولد سكول", "بوم باب وكلمات مركّزة"),
  DRILL("دريل", "إيقاع مظلم وقوافي حادّة"),
  MELODIC("ميلوديك", "راب عاطفي ولازمة سهلة")
}

enum class Energy(val label: String) {
  CALM("رايق"),
  BALANCED("متوازن"),
  HIGH("مولّع")
}

enum class QaStatus { PASS, WARNING, BLOCKED }

data class DictionaryEntry(
  val word: String,
  val meaning: String,
  val example: String,
  val category: String,
  val variants: List<String> = emptyList()
)

data class PromptOptions(
  val title: String = "حومة و حلم",
  val theme: String = "الطموح وتبديل الواقع",
  val style: RapStyle = RapStyle.TRAP,
  val energy: Energy = Energy.BALANCED,
  val selectedWords: List<String> = listOf("حومة", "برشا", "نحب", "قدّام")
)

data class QualityCheck(
  val name: String,
  val detail: String,
  val status: QaStatus,
  val score: Int
)

data class QaReport(
  val checks: List<QualityCheck> = emptyList(),
  val overallScore: Int = 0,
  val passedPasses: Int = 0,
  val totalPasses: Int = 5
) {
  val isReady: Boolean
    get() = overallScore >= 80 && checks.none { it.status == QaStatus.BLOCKED }
}

data class Song(
  val id: String,
  val title: String,
  val theme: String,
  val style: RapStyle,
  val energy: Energy,
  val lyrics: String,
  val prompt: String,
  val status: SongStatus,
  val qaReport: QaReport,
  val createdAt: String,
  val isFavorite: Boolean = false
)

data class StudioState(
  val editingSongId: String? = null,
  val options: PromptOptions = PromptOptions(),
  val generatedLyrics: String = "",
  val generatedPrompt: String = "",
  val report: QaReport = QaReport(),
  val isGenerating: Boolean = false,
  val lastAction: String = "جاهز للكتابة"
)
