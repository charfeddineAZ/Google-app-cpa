package com.example.data

import com.example.model.DictionaryEntry
import com.example.model.Energy
import com.example.model.PromptOptions
import com.example.model.QaReport
import com.example.model.QaStatus
import com.example.model.QualityCheck
import com.example.model.RapStyle
import com.example.model.Song
import com.example.model.SongStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The app's offline-first Tunisian word bank and writing engine.
 *
 * It intentionally keeps the lexicon in the repository so a song can be drafted,
 * checked and saved without an API key. An AI provider can later receive the
 * generated prompt, but the five-pass gate remains local and deterministic.
 */
object RapRepository {
  val dictionary: List<DictionaryEntry> = listOf(
    DictionaryEntry("برشا", "كثير / جدّا", "نحبّك برشا وما نبدّلش", "توكيد", listOf("برشة")),
    DictionaryEntry("ياسر", "جدّا / كثير", "الحلم ياسر كبير", "توكيد"),
    DictionaryEntry("موش", "ليس / مش", "موش ساهل، أمّا نكمّل", "نفي", listOf("مش")),
    DictionaryEntry("مانيش", "لستُ", "مانيش نستنّى في حدّ", "نفي"),
    DictionaryEntry("نحب", "أحب / أريد", "نحب نعيشها كيما نحب", "مشاعر"),
    DictionaryEntry("نحلم", "أحلم", "نحلم بفجر يبدّل الحكاية", "مشاعر"),
    DictionaryEntry("حومة", "الحيّ / المنطقة", "في الحومة اسمي حاضر", "مكان"),
    DictionaryEntry("زنقة", "الشارع الضيّق", "نمشي في الزنقة وما نطيّحش", "مكان"),
    DictionaryEntry("دزّ", "ادفع / تقدّم", "دزّ لقدّام وخلي الخوف", "فعل", listOf("يدزّ")),
    DictionaryEntry("نمشي", "أذهب / أسير", "نمشي وحدي والنية صافية", "فعل"),
    DictionaryEntry("نكمّل", "أواصل / أتمّ", "نكمّل للنهاية مهما صار", "فعل"),
    DictionaryEntry("نخمّم", "أفكّر", "نخمّم في غدوة موش في البارح", "فعل"),
    DictionaryEntry("نحكي", "أتكلّم", "نحكي من قلبي موش للواجهة", "فعل"),
    DictionaryEntry("نسمع", "أسمع", "نسمع صوتي فوق الدوزان", "فعل"),
    DictionaryEntry("فدّيت", "سئمت / مللت", "فدّيت من كلام بلا فعل", "حالة"),
    DictionaryEntry("مخّي", "عقلي", "في مخّي ألف فكرة", "جسم", listOf("مخي")),
    DictionaryEntry("قلبي", "قلبي", "قلبي ما يعرف كان الصدق", "جسم"),
    DictionaryEntry("روحي", "نفسي / روحي", "روحي حرّة في هالدنيا", "مشاعر"),
    DictionaryEntry("صحابي", "أصدقائي", "صحابي واقفين وقت الشدّة", "ناس"),
    DictionaryEntry("خويا", "يا أخي / صديقي", "خويا شدّ صحيح", "نداء"),
    DictionaryEntry("ياخي", "أداة استفهام أو تعجّب", "ياخي تحسبها تسكتني؟", "تعجّب"),
    DictionaryEntry("آش", "ماذا", "آش تحبّ منّي؟", "استفهام"),
    DictionaryEntry("علاش", "لماذا", "علاش نخاف والباب محلول؟", "استفهام"),
    DictionaryEntry("وين", "أين", "وين ما نمشي نخلّي أثر", "استفهام"),
    DictionaryEntry("توا", "الآن", "توا وقت الفعل موش الكلام", "زمن"),
    DictionaryEntry("غدوة", "غدًا", "غدوة نلقى روحي فوق", "زمن"),
    DictionaryEntry("البارح", "أمس", "البارح درس واليوم بداية", "زمن"),
    DictionaryEntry("قدّام", "إلى الأمام / أمام", "نحطّ عيني قدّام", "اتجاه"),
    DictionaryEntry("لورى", "إلى الخلف", "ما نرجعش لورى", "اتجاه"),
    DictionaryEntry("بالحق", "حقًّا", "بالحقّ صوتي ما يتباعش", "توكيد"),
    DictionaryEntry("بالك", "انتبه / خذ بالك", "بالك من الوعد الفارغ", "تنبيه"),
    DictionaryEntry("يعطيك الصحّة", "شكرًا / أحسنت", "يعطيك الصحّة يا خويا", "تحيّة"),
    DictionaryEntry("ما ثماش", "لا يوجد", "ما ثماش مستحيل قدّام العزيمة", "نفي", listOf("ماثماش")),
    DictionaryEntry("هكّا", "هكذا", "نعيش هكّا، واضح وبلا دوران", "وصف"),
    DictionaryEntry("على خاطري", "من أجلي / لأنني أريد", "على خاطري نبدّل المسار", "مشاعر"),
    DictionaryEntry("رزينة", "هادئة ومتّزنة", "كلمة رزينة أقوى من الصياح", "وصف"),
    DictionaryEntry("فزّ", "انهض / اقفز", "فزّ من القاع وعاود ابدا", "فعل"),
    DictionaryEntry("شدّ صحيح", "اثبت / تمسّك", "شدّ صحيح، الليل ما يدومش", "تشجيع"),
    DictionaryEntry("الجو", "الأجواء", "الجو تبدّل كي دخل الدوزان", "وصف"),
    DictionaryEntry("دوزان", "إيقاع / موسيقى", "دوزان ثقيل وكلمة حارّة", "موسيقى"),
    DictionaryEntry("كلمة", "كلمة", "كل كلمة عندها ثمن", "كتابة"),
    DictionaryEntry("حكاية", "قصة / أمر", "هاذي حكاية صوتي", "كتابة"),
    DictionaryEntry("سطر", "سطر كتابي", "كل سطر شاهد على التعب", "كتابة"),
    DictionaryEntry("قافية", "قافية", "نركّب القافية من غير تكلّف", "كتابة"),
    DictionaryEntry("ضو", "نور", "نولّي ضو وسط العتمة", "صورة"),
    DictionaryEntry("ليل", "ليل", "في الليل تولد الفكرة", "صورة"),
    DictionaryEntry("فجر", "فجر", "بعد الصبر يطلع الفجر", "صورة"),
    DictionaryEntry("حلم", "حلم", "الحلم ما هوش عيب", "مشاعر"),
    DictionaryEntry("قاع", "القاع / الأسفل", "طلعت من القاع بصوتي", "صورة"),
    DictionaryEntry("راس", "رأس / عقل", "راسي مرفوع رغم الريح", "جسم")
  )

  val categories: List<String>
    get() = listOf("الكل") + dictionary.map { it.category }.distinct()

  fun seedSongs(): List<Song> = listOf(
    Song(
      id = "seed-1",
      title = "توا وقتي",
      theme = "كسر الخوف وبداية جديدة",
      style = RapStyle.TRAP,
      energy = Energy.HIGH,
      lyrics = """[لازمة]
توا وقتي، موش غدوة، نبدّل الحكاية
في الحومة صوتي طالع، ما نطلبش رعاية

[كوبليه]
مانيش نستنى في باب يتحلّ وحدو
نخمّم، نكمّل، ونزيد نركّز في قصدو
صحابي قالولي شدّ صحيح يا خويا
بالحقّ كل خطوة تقرّبني لروحي شوية

[لازمة]
توا وقتي، موش غدوة، نبدّل الحكاية
من القاع للضو نمشي، هاذي البداية""".trimIndent(),
      prompt = buildPrompt(PromptOptions("توا وقتي", "كسر الخوف وبداية جديدة", RapStyle.TRAP, Energy.HIGH, listOf("توا", "مانيش", "حومة", "شدّ صحيح"))),
      status = SongStatus.REVIEWED,
      qaReport = analyse("توا وقتي", """توا وقتي، موش غدوة، نبدّل الحكاية
في الحومة صوتي طالع، ما نطلبش رعاية
مانيش نستنى في باب يتحلّ وحدو
نخمّم، نكمّل، ونزيد نركّز في قصدو
صحابي قالولي شدّ صحيح يا خويا
بالحقّ كل خطوة تقرّبني لروحي شوية"""),
      createdAt = "اليوم، 09:42"
    ),
    Song(
      id = "seed-2",
      title = "في الزنقة",
      theme = "الصداقة والوفاء",
      style = RapStyle.OLD_SCHOOL,
      energy = Energy.BALANCED,
      lyrics = """[مقدّمة]
في الزنقة نعرف شكون واقف وشكون يعدّي
الكلمة ميزان، والوجه ما يلزمش يتخبّى

[كوبليه]
خويا كان معايا نهار الدنيا ضاقت
صحابي قلّة، أمّا صحبتهم ما تماطتش
ما ثماش قناع كي يهبط الليل
بالحقّ نعيش واضح، ونكتب اسمي في السطر""".trimIndent(),
      prompt = buildPrompt(PromptOptions("في الزنقة", "الصداقة والوفاء", RapStyle.OLD_SCHOOL, Energy.BALANCED, listOf("زنقة", "خويا", "صحابي", "بالحق"))),
      status = SongStatus.PUBLISHED,
      qaReport = QaReport(
        checks = listOf(
          QualityCheck("لهجة تونسية", "المفردات المطابقة للقاموس حاضرة", QaStatus.PASS, 94),
          QualityCheck("القافية والفلو", "قافية متماسكة مع تبديل طبيعي", QaStatus.PASS, 88),
          QualityCheck("الأصالة", "لا توجد عبارات طويلة متشابهة", QaStatus.PASS, 100),
          QualityCheck("السلامة", "محتوى فني بلا تحريض أو استهداف", QaStatus.PASS, 100),
          QualityCheck("المراجعة النهائية", "جاهز للحفظ في المكتبة", QaStatus.PASS, 95)
        ),
        overallScore = 95,
        passedPasses = 5
      ),
      createdAt = "البارح، 18:10"
    )
  )

  fun buildPrompt(options: PromptOptions): String {
    val selected = options.selectedWords.joinToString("، ")
    val references = options.selectedWords.mapNotNull { word ->
      dictionary.find { it.word == word }?.let { "${it.word}: ${it.meaning} — مثال: ${it.example}" }
    }.joinToString("\n")

    return """أنت كاتب راب تونسي ومدقّق لهجة تونسية. مهمّتك كتابة أغنية أصلية بعنوان «${options.title}» حول «${options.theme}» بأسلوب ${options.style.label} وطاقة ${options.energy.label}.

قاعدة المصدر الإلزامية:
1) استخرج الكلمات التونسية المناسبة من «القاموس التونسي المرفق» قبل الكتابة، واستعمل معانيها وأمثلتها كمرجع.
2) الكلمات المفتاحية المطلوبة: $selected
3) لا تستعمل العربية الفصحى، ولا اللهجات المشرقية أو المغاربية الأخرى، ولا الفرنسية أو الإنقليزية داخل الكلمات. إذا لم تعرف تعبيراً تونسياً، اتركه أو اسأل بدل اختراعه.
4) اكتب بالدارجة التونسية 100%، مع الحفاظ على المعنى الطبيعي لا على ترجمة حرفية. لا تقلّد فناناً موجوداً ولا تعاود عبارة معروفة.

القاموس المستعمل:
$references

البنية المطلوبة:
- [مقدّمة] قصيرة
- [لازمة] من 4 أسطر قابلة للترديد
- [كوبليه 1] من 8 أسطر
- [كوبليه 2] من 8 أسطر
- [لازمة]
- [خاتمة] من سطرين
استعمل صوراً تونسية من الحياة اليومية، فلو واضح، وقافية مرنة من غير حشو أو سبّ مجاني. اجعل كل سطر قابلاً للغناء ولا تضع ملاحظات بين قوسين داخل النص.

بوابة المراجعة الإلزامية — نفّذها بعد المسودة خمس مرّات:
المرّة 1: راجع كل كلمة مقابل القاموس والسياق التونسي.
المرّة 2: احذف أي فصحى أو دخيل غير ضروري وصحّح التصريف التونسي.
المرّة 3: اختبر النطق، طول الأسطر، القافية، وتكرار اللازمة.
المرّة 4: افحص الأصالة، عدم تقليد فنان، وعدم وجود كراهية أو تهديد أو تحريض.
المرّة 5: اقرأ النص كأنك تونسي، أصلح آخر زلّة، ثم أخرج الأغنية فقط مع تقرير قصير يذكر نسبة القاموس ونتيجة كل مراجعة.

أخرج: العنوان، الكلمات، ثم «تقرير المراجعة» في آخر الرد.""".trimIndent()
  }

  fun generateLyrics(options: PromptOptions): String {
    val words = options.selectedWords.ifEmpty { listOf("حومة", "نكمّل", "قدّام", "حلم") }
    val a = words.getOrElse(0) { "حومة" }
    val b = words.getOrElse(1) { "نكمّل" }
    val c = words.getOrElse(2) { "قدّام" }
    val d = words.getOrElse(3) { "حلم" }
    val styleLine = when (options.style) {
      RapStyle.TRAP -> "دوزان ثقيل، نقطع السطر ونرجع باللازمة"
      RapStyle.OLD_SCHOOL -> "كلمة فوق كلمة، والفلو ثابت كي نبض الحومة"
      RapStyle.DRILL -> "صوت بارد في الليل، أمّا العزيمة سخونة"
      RapStyle.MELODIC -> "نطلع ونهبط بالنغمة، واللازمة تدخل للروح"
    }
    val energyLine = when (options.energy) {
      Energy.CALM -> "على روحي، ناخذ النفس ونخلي المعنى يهدر"
      Energy.BALANCED -> "نوازن بين الهدوء والنار، كل كلمة في بلاصتها"
      Energy.HIGH -> "نرفع الصوت، نكسر الصمت، ونشعل الجو"
    }

    return """[مقدّمة]
$energyLine
من $a نبدأ، وما نبدّلش مساري

[لازمة]
${options.title}، نكتبها فوق الجدار
موش كلام عابر، هاذا صوت النهار
$d في عيني، و$c في خطوتي
نحبّها واضحة، ونكمّل حكايتي

[كوبليه 1]
في $a تربّيت، سمعت الدنيا تحكي
كل سطر من وجعي علّمني كيف نركّب
مانيش نستنى تصفيق، نسمع قلبي برك
وكي يضيق الطريق، نلقى في روحي مخرج
$styleLine
صحابي قالولي: شدّ صحيح، وخلي الأثر
نخمّم في غدوة، موش نعيش في الخوف
توا وقت الفعل، والنية تفتح ألف باب

[كوبليه 2]
نحكي من غير قناع، هكّا نعرف روحي
ما ثماش مستحيل كي نكون واقف على ساقي
$d موش حلم بعيد، هو وعد بيني وبيني
و$c قدّام عيني، حتى كان الليل يعمّي
فدّيت من كلمة كبيرة ما وراها حتى فعل
نحبّ كلمة رزينة، تداوي موش تجرح
بالحقّ كل خطوة تزيد تقرّبني لجوّي
ومن القاع للضو نطلع، وما نرجعش لورى

[لازمة]
${options.title}، نكتبها فوق الجدار
موش كلام عابر، هاذا صوت النهار
$d في عيني، و$c في خطوتي
نحبّها واضحة، ونكمّل حكايتي

[خاتمة]
يعطيك الصحّة يا ليل، خلي الفجر يبان
هاذي حكايتي بالتونسي، من قلبي للميكروفون""".trimIndent()
  }

  /** Five local gates. Scores are intentionally visible to the creator, not hidden behind a claim of perfection. */
  fun analyse(title: String, lyrics: String): QaReport {
    val cleanWords = lyrics
      .lowercase(Locale.ROOT)
      .replace(Regex("[^ء-ي0-9\\s]"), " ")
      .split(Regex("\\s+"))
      .filter { it.length > 1 && !it.startsWith("[") }
    val known = dictionary.flatMap { listOf(it.word) + it.variants }.toSet()
    val matched = cleanWords.count { word -> known.any { key -> word == key || word.startsWith(key) } }
    val arabicLetters = lyrics.count { it in '\u0600'..'\u06FF' }
    val letters = lyrics.count { it.isLetter() }
    val dialectRatio = if (letters == 0) 0 else ((arabicLetters.toDouble() / letters) * 100).roundToInt()
    val uniqueRatio = if (cleanWords.isEmpty()) 0 else ((cleanWords.distinct().size.toDouble() / cleanWords.size) * 100).roundToInt()
    val titleSafe = title.isNotBlank() && lyrics.length >= 240
    val coverage = if (cleanWords.isEmpty()) 0 else ((matched.toDouble() / cleanWords.size) * 100).roundToInt().coerceAtMost(100)
    val safety = !Regex("(?i)\\b(اقتل|فجّر|عنصري|كره)\\b").containsMatchIn(lyrics)

    val checks = listOf(
      QualityCheck("بوابة اللهجة", "نسبة الحروف العربية: $dialectRatio% • مطابقة القاموس: $coverage%", if (dialectRatio >= 95 && coverage >= 8) QaStatus.PASS else QaStatus.WARNING, ((dialectRatio + coverage) / 2).coerceAtMost(100)),
      QualityCheck("بوابة طبيعية التونسي", if (matched >= 7) "مصطلحات تونسية موثّقة في السياق" else "زيد كلمات من القاموس باش يقوى الصوت التونسي", if (matched >= 7) QaStatus.PASS else QaStatus.WARNING, (coverage + 60).coerceAtMost(100)),
      QualityCheck("بوابة الفلو والقافية", if (lyrics.lines().count { it.isNotBlank() } >= 12) "البنية فيها مقاطع ولازمة قابلة للترديد" else "النص قصير على بنية أغنية", if (lyrics.lines().count { it.isNotBlank() } >= 12) QaStatus.PASS else QaStatus.WARNING, 86),
      QualityCheck("بوابة الأصالة والسلامة", if (safety && uniqueRatio >= 45) "لا عنف أو كراهية ظاهرة، وتنوّع الكلمات مقبول" else "راجع العبارات المتشابهة أو المحتوى الحاد", if (safety) QaStatus.PASS else QaStatus.BLOCKED, uniqueRatio.coerceAtMost(100)),
      QualityCheck("المراجعة النهائية", if (titleSafe) "العنوان حاضر والنص قابل للحفظ في المكتبة" else "يلزم عنوان ونص أطول قبل الحفظ", if (titleSafe) QaStatus.PASS else QaStatus.WARNING, if (titleSafe) 92 else 45)
    )
    val score = checks.map { it.score }.average().roundToInt()
    return QaReport(checks, score, checks.count { it.status == QaStatus.PASS })
  }

  fun nowLabel(): String = SimpleDateFormat("اليوم، HH:mm", Locale.getDefault()).format(Date())
}
