package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.model.SongStatus
import com.example.ui.theme.Border
import com.example.ui.theme.Cream
import com.example.ui.theme.Faded
import com.example.ui.theme.Ink
import com.example.ui.theme.InkCard
import com.example.ui.theme.Lime
import com.example.ui.theme.LimeDark
import com.example.ui.theme.Muted
import com.example.ui.theme.Orange
import com.example.ui.theme.Purple
import com.example.ui.theme.PurpleSoft
import com.example.viewmodel.RapStudioViewModel

@Composable
fun LibraryScreen(viewModel: RapStudioViewModel, modifier: Modifier = Modifier) {
  val songs by viewModel.songs.collectAsState()
  var query by remember { mutableStateOf("") }
  var status by remember { mutableStateOf<SongStatus?>(null) }
  val filtered = songs.filter { song ->
    (status == null || song.status == status) &&
      (query.isBlank() || song.title.contains(query, true) || song.theme.contains(query, true))
  }

  LazyColumn(
    modifier = modifier.padding(horizontal = 18.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      ScreenHeader(
        eyebrow = "مكتبة المشاريع",
        title = "كل حكاية في بلاصتها.",
        subtitle = "مسوداتك، النصوص اللي تعدّات المصفاة، والجاهز للنشر — في مخزن واحد.",
        trailing = { AccentPill("${songs.size} أغاني", Purple, PurpleSoft) }
      )
    }
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatChip("${songs.size}", "كل المشاريع", Purple)
        StatChip("${songs.count { it.status == SongStatus.REVIEWED }}", "مراجعة", Lime)
        StatChip("${songs.count { it.isFavorite }}", "مفضّلة", Orange)
      }
    }
    item {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Muted) },
        placeholder = { Text("فتّش بالعنوان ولا بالموضوع") },
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
      Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = status == null,
          onClick = { status = null },
          label = { Text("الكل") },
          colors = libraryChipColors()
        )
        SongStatus.values().forEach { filter ->
          FilterChip(
            selected = status == filter,
            onClick = { status = if (status == filter) null else filter },
            label = { Text(filter.label) },
            colors = libraryChipColors()
          )
        }
      }
    }
    if (filtered.isEmpty()) {
      item {
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(Icons.Outlined.LibraryMusic, contentDescription = null, tint = Faded, modifier = Modifier.size(34.dp))
            Text("ما لقيناش مشروع بالفلتر هذا", color = Cream, fontWeight = FontWeight.Bold)
            Text("جرّب كلمة أخرى ولا ارجع للكل.", color = Muted, fontSize = 12.sp)
          }
        }
      }
    } else {
      items(filtered, key = { it.id }) { song ->
        SongCard(song, onOpen = { viewModel.openSong(song) }, onFavorite = { viewModel.toggleFavorite(song.id) }, onDelete = { viewModel.deleteSong(song.id) })
      }
    }
    item { Spacer(modifier = Modifier.height(18.dp)) }
  }
}

@Composable
private fun SongCard(song: Song, onOpen: () -> Unit, onFavorite: () -> Unit, onDelete: () -> Unit) {
  SurfaceCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(song.title, color = Cream, fontSize = 18.sp, fontWeight = FontWeight.Black)
            val statusColor = if (song.status == SongStatus.REVIEWED || song.status == SongStatus.PUBLISHED) Lime else Orange
            AccentPill(song.status.label, statusColor, if (statusColor == Lime) LimeDark else Color(0xFF3A2817))
          }
          Text(song.theme, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
          Icon(if (song.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "مفضلة", tint = if (song.isFavorite) Orange else Muted)
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        AccentPill(song.style.label, Purple, PurpleSoft)
        AccentPill(song.energy.label, Orange, Color(0xFF3A2817))
        Text(song.createdAt, color = Faded, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
      }
      Text(song.lyrics.replace(Regex("\\[.*?]"), "").trim().take(150) + "…", color = Cream.copy(alpha = 0.82f), fontSize = 12.sp, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PrimaryButton("افتح وعدّل", Modifier.weight(1f), onOpen)
        OutlineButton("حذف", Modifier.width(78.dp), onDelete)
      }
    }
  }
}

@Composable
private fun libraryChipColors() = FilterChipDefaults.filterChipColors(
  containerColor = Ink,
  labelColor = Muted,
  selectedContainerColor = PurpleSoft,
  selectedLabelColor = Cream
)
