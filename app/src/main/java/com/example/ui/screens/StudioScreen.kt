package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RapRepository
import com.example.model.Energy
import com.example.model.RapStyle
import com.example.ui.theme.Border
import com.example.ui.theme.Cream
import com.example.ui.theme.Faded
import com.example.ui.theme.Ink
import com.example.ui.theme.Lime
import com.example.ui.theme.Muted
import com.example.ui.theme.Orange
import com.example.ui.theme.Purple
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.Red
import com.example.viewmodel.RapStudioViewModel

@Composable
fun StudioScreen(viewModel: RapStudioViewModel, modifier: Modifier = Modifier) {
  val studio by viewModel.studio.collectAsState()
  val clipboard = LocalClipboardManager.current
  val selected = studio.options.selectedWords

  Column(
    modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    ScreenHeader(
      eyebrow = "استوديو الراب التونسي",
      title = "من الفكرة للماستر.",
      subtitle = "اكتب، استخرج من القاموس، وبعدها عدّي النص على خمس بوابات مراجعة قبل ما تحفظو.",
      trailing = { AccentPill("محلّي • بلا API", Purple, PurpleSoft) }
    )

    SurfaceCard {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Eyebrow("01 / الفكرة")
            Text("ابني البذرة متاع الأغنية", color = Cream, fontWeight = FontWeight.Bold, fontSize = 18.sp)
          }
          Text("قاموس تونسي مربوط", color = Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
          value = studio.options.title,
          onValueChange = viewModel::updateTitle,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          label = { Text("عنوان الأغنية") },
          placeholder = { Text("مثال: توا وقتي") },
          shape = RoundedCornerShape(13.dp),
          colors = fieldColors()
        )
        OutlinedTextField(
          value = studio.options.theme,
          onValueChange = viewModel::updateTheme,
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          maxLines = 3,
          label = { Text("الموضوع / الحكاية") },
          placeholder = { Text("شنوّة تحب تحكي؟") },
          shape = RoundedCornerShape(13.dp),
          colors = fieldColors()
        )
      }
    }

    SurfaceCard {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        SectionTitle("الصوت والإيقاع", "اختار الجو")
        Text("الستايل", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          RapStyle.values().forEach { style ->
            FilterChip(
              selected = studio.options.style == style,
              onClick = { viewModel.setStyle(style) },
              label = { Text(style.label) },
              colors = chipColors()
            )
          }
        }
        Text("الطاقة", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Energy.values().forEach { energy ->
            FilterChip(
              modifier = Modifier.weight(1f),
              selected = studio.options.energy == energy,
              onClick = { viewModel.setEnergy(energy) },
              label = { Text(energy.label, modifier = Modifier.fillMaxWidth()) },
              colors = chipColors()
            )
          }
        }
      }
    }

    SurfaceCard {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Eyebrow("02 / المصدر")
            Text("كلمات من القاموس", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp)
          }
          Text("${selected.size}/8 مختارة", color = if (selected.size >= 4) Lime else Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("اختار كلمات تحبّ الذكاء الاصطناعي يبني عليها. الباقي يتستخرج آلياً من نفس القاموس.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
          RapRepository.dictionary.take(20).forEach { entry ->
            FilterChip(
              selected = entry.word in selected,
              onClick = { viewModel.toggleWord(entry.word) },
              label = { Text(entry.word) },
              colors = chipColors()
            )
          }
        }
      }
    }

    PrimaryButton(
      text = if (studio.isGenerating) "نراجع ونصنع…" else "ولّد أغنية تونسية",
      modifier = Modifier.fillMaxWidth(),
      enabled = !studio.isGenerating,
      onClick = viewModel::generateSong
    )
    Text(studio.lastAction, modifier = Modifier.fillMaxWidth(), color = Muted, fontSize = 11.sp)
    if (studio.isGenerating) {
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Lime, trackColor = PurpleSoft)
    }

    if (studio.generatedLyrics.isNotBlank()) {
      SurfaceCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Eyebrow("03 / المسودة")
              Text(studio.options.title.ifBlank { "أغنية جديدة" }, color = Cream, fontWeight = FontWeight.Black, fontSize = 21.sp)
            }
            IconButton(onClick = { clipboard.setText(AnnotatedString(studio.generatedLyrics)) }) {
              Icon(Icons.Outlined.ContentCopy, contentDescription = "نسخ", tint = Muted)
            }
          }
          Text(studio.generatedLyrics, color = Cream, fontSize = 14.sp, lineHeight = 25.sp)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton("احفظ في المكتبة", Modifier.weight(1f), viewModel::saveGeneratedSong)
            OutlineButton("انسخ الكلمات", Modifier.weight(1f)) { clipboard.setText(AnnotatedString(studio.generatedLyrics)) }
          }
        }
      }

      SurfaceCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
              Eyebrow("04 / المصفاة")
              Text("مراجعة على خمس مرّات", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            AccentPill("${studio.report.overallScore}%", if (studio.report.isReady) Lime else Orange, if (studio.report.isReady) com.example.ui.theme.LimeDark else Color(0xFF3A2817))
          }
          Text("النتيجة ما هيش وعد بالكمال: هي بوابة واضحة تورّيك وين يلزمك تصلّح قبل النشر.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
          studio.report.checks.forEach { QualityRow(it) }
        }
      }
    }
    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
  focusedBorderColor = Purple,
  unfocusedBorderColor = Border,
  focusedLabelColor = Purple,
  unfocusedLabelColor = Muted,
  cursorColor = Lime,
  focusedTextColor = Cream,
  unfocusedTextColor = Cream,
  focusedContainerColor = Ink,
  unfocusedContainerColor = Ink
)

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
  containerColor = Ink,
  labelColor = Muted,
  selectedContainerColor = PurpleSoft,
  selectedLabelColor = Cream,
  selectedLeadingIconColor = Lime
)
