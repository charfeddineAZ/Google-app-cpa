package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DictionaryEntry
import com.example.ui.theme.Border
import com.example.ui.theme.Cream
import com.example.ui.theme.Faded
import com.example.ui.theme.Ink
import com.example.ui.theme.InkCard
import com.example.ui.theme.Lime
import com.example.ui.theme.Muted
import com.example.ui.theme.Purple
import com.example.ui.theme.PurpleSoft
import com.example.viewmodel.RapStudioViewModel

@Composable
fun DictionaryScreen(viewModel: RapStudioViewModel, modifier: Modifier = Modifier) {
  val entries by viewModel.dictionary.collectAsState()
  var query by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("الكل") }
  val categories = listOf("الكل") + entries.map { it.category }.distinct()
  val visible = entries.filter { entry ->
    (category == "الكل" || entry.category == category) &&
      (query.isBlank() || entry.word.contains(query, true) || entry.meaning.contains(query, true))
  }

  LazyColumn(
    modifier = modifier.padding(horizontal = 18.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      ScreenHeader(
        eyebrow = "المصدر الأساسي",
        title = "القاموس التونسي.",
        subtitle = "كلمات، معاني وأمثلة قصيرة باش النص يخرج طبيعي موش ترجمة آلية.",
        trailing = { AccentPill("${entries.size} كلمة", Lime, com.example.ui.theme.LimeDark) }
      )
    }
    item {
      SurfaceCard {
        Row(modifier = Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Outlined.Book, contentDescription = null, tint = Lime)
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("المصدر مربوط بالبرومبة", color = Cream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("كل كلمة تختارها تمشي مباشرة لمرحلة التوليد والمراجعة.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
          }
        }
      }
    }
    item {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Muted) },
        placeholder = { Text("فتّش: برشا، حومة، نكمّل…") },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Purple,
          unfocusedBorderColor = Border,
          focusedTextColor = Cream,
          unfocusedTextColor = Cream,
          focusedContainerColor = InkCard,
          unfocusedContainerColor = InkCard,
          cursorColor = Lime
        )
      )
    }
    item {
      Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        categories.forEach { item ->
          FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) }, colors = FilterChipDefaults.filterChipColors(containerColor = Ink, labelColor = Muted, selectedContainerColor = PurpleSoft, selectedLabelColor = Cream))
        }
      }
    }
    items(visible, key = { it.word }) { entry -> DictionaryEntryCard(entry) }
    item { Spacer(modifier = Modifier.height(18.dp)) }
  }
}

@Composable
private fun DictionaryEntryCard(entry: DictionaryEntry) {
  SurfaceCard {
    Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(entry.word, color = Lime, fontSize = 21.sp, fontWeight = FontWeight.Black)
        AccentPill(entry.category, Purple, PurpleSoft)
      }
      Text(entry.meaning, color = Cream, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      Text("«${entry.example}»", color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
      if (entry.variants.isNotEmpty()) {
        Text("تتقال زادة: ${entry.variants.joinToString("، ")}", color = Faded, fontSize = 10.sp)
      }
    }
  }
}
