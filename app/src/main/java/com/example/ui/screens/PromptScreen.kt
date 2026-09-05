package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.ui.theme.Cream
import com.example.ui.theme.Faded
import com.example.ui.theme.InkCard
import com.example.ui.theme.Lime
import com.example.ui.theme.LimeDark
import com.example.ui.theme.Muted
import com.example.ui.theme.Orange
import com.example.ui.theme.Purple
import com.example.ui.theme.PurpleSoft
import com.example.viewmodel.RapStudioViewModel

@Composable
fun PromptScreen(viewModel: RapStudioViewModel, modifier: Modifier = Modifier) {
  val studio by viewModel.studio.collectAsState()
  val clipboard = LocalClipboardManager.current
  val passes = listOf(
    "01" to "المصدر", 
    "02" to "اللهجة", 
    "03" to "الفلو والقافية", 
    "04" to "الأصالة والسلامة", 
    "05" to "القراءة الأخيرة"
  )

  Column(
    modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(15.dp)
  ) {
    ScreenHeader(
      eyebrow = "المحرك الكامل",
      title = "البرومبة اللي تخدم وحدها.",
      subtitle = "موش نصّ يتنسخ وخلاص: الخيارات، القاموس، والتصفية مربوطين في نفس المسار.",
      trailing = { AccentPill("نسخة حيّة", Lime, LimeDark) }
    )

    SurfaceCard {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Eyebrow("تعريف البرومبة")
            Text("Tounsi-first writing protocol", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp)
          }
          Icon(Icons.Outlined.Lock, contentDescription = null, tint = Lime)
        }
        Text("القواعد ثابتة: استخراج من القاموس، تونسي 100%، ثم خمس مراجعات مرئية. تنجم تبدّل الفكرة من الاستوديو والبرومبة تتحدّث وحدها.", color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AccentPill("100% تونسي", Lime, LimeDark)
          AccentPill("5 مراجعات", Orange, Color(0xFF3A2817))
          AccentPill("أصلي", Purple, PurpleSoft)
        }
      }
    }

    SurfaceCard {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Eyebrow("النص الكامل")
            Text("انسخو لأي موديل تحبّو", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp)
          }
          IconButton(onClick = { clipboard.setText(AnnotatedString(studio.generatedPrompt)) }) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "نسخ البرومبة", tint = Lime)
          }
        }
        SelectionContainer {
          Text(studio.generatedPrompt, color = Cream.copy(alpha = 0.88f), fontSize = 12.sp, lineHeight = 20.sp)
        }
        OutlineButton("انسخ البرومبة كاملة", Modifier.fillMaxWidth()) { clipboard.setText(AnnotatedString(studio.generatedPrompt)) }
      }
    }

    SectionTitle("بوابات ما بعد الإنتاج", "تخدم بعد كل توليد")
    passes.forEach { (number, label) ->
      SurfaceCard {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
          AccentPill(number, Purple, PurpleSoft)
          Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, color = Cream, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
              when (number) {
                "01" -> "نلقاو الكلمات المناسبة في القاموس قبل ما نكتبوا."
                "02" -> "نحذف الفصحى والدخيل ونصلح التصريف التونسي."
                "03" -> "نسمع السطر ونشوف الطول والقافية واللازمة."
                "04" -> "نراجع الأصالة والسلامة بلا تقليد ولا تحريض."
                else -> "قراءة أخيرة بعين مستمع تونسي قبل الحفظ."
              },
              color = Muted, fontSize = 11.sp, lineHeight = 17.sp
            )
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(18.dp))
  }
}
