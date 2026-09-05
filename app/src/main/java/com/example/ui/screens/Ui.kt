package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QaStatus
import com.example.model.QualityCheck
import com.example.ui.theme.Border
import com.example.ui.theme.Cream
import com.example.ui.theme.Faded
import com.example.ui.theme.InkCard
import com.example.ui.theme.InkElevated
import com.example.ui.theme.Lime
import com.example.ui.theme.LimeDark
import com.example.ui.theme.Muted
import com.example.ui.theme.Orange
import com.example.ui.theme.Purple
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.Red

@Composable
fun PageColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Column(
    modifier = modifier.padding(horizontal = 18.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    content = { content() }
  )
}

@Composable
fun Eyebrow(text: String, color: Color = Lime) {
  Text(text.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
}

@Composable
fun ScreenHeader(
  eyebrow: String,
  title: String,
  subtitle: String,
  trailing: (@Composable () -> Unit)? = null
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
      Eyebrow(eyebrow)
      Text(title, color = Cream, fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.7).sp)
      Text(subtitle, color = Muted, fontSize = 13.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
    trailing?.invoke()
  }
}

@Composable
fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = InkCard),
    border = BorderStroke(1.dp, Border),
    content = { content() }
  )
}

@Composable
fun AccentPill(text: String, color: Color = Lime, background: Color = LimeDark) {
  Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(background).padding(horizontal = 10.dp, vertical = 5.dp)) {
    Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
fun SectionTitle(title: String, meta: String? = null) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
    Text(title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    meta?.let { Text(it, color = Faded, fontSize = 11.sp) }
  }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit, enabled: Boolean = true) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.height(48.dp),
    shape = RoundedCornerShape(14.dp),
    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink, disabledContainerColor = InkElevated, disabledContentColor = Faded)
  ) { Text(text, fontWeight = FontWeight.Black, fontSize = 13.sp) }
}

@Composable
fun OutlineButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.height(46.dp),
    shape = RoundedCornerShape(13.dp),
    border = BorderStroke(1.dp, Border),
    colors = ButtonDefaults.outlinedButtonColors(contentColor = Cream)
  ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
}

@Composable
fun StatChip(value: String, label: String, color: Color = Purple) {
  Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(InkElevated).padding(horizontal = 13.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
    Text(label, color = Muted, fontSize = 10.sp)
  }
}

@Composable
fun QualityRow(check: QualityCheck) {
  val color = when (check.status) {
    QaStatus.PASS -> Lime
    QaStatus.WARNING -> Orange
    QaStatus.BLOCKED -> Red
  }
  val icon = when (check.status) {
    QaStatus.PASS -> Icons.Outlined.CheckCircle
    QaStatus.WARNING -> Icons.Outlined.Info
    QaStatus.BLOCKED -> Icons.Outlined.ErrorOutline
  }
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(check.name, color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Text(check.detail, color = Muted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
    Text("${check.score}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
  }
}

@Composable
fun DividerSpace() {
  Spacer(modifier = Modifier.height(2.dp))
}
