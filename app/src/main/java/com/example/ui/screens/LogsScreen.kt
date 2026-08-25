package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogLevel
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.ConsolePurpleAccent
import com.example.ui.theme.ConsoleText
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AutomatorViewModel

@Composable
fun LogsScreen(
  viewModel: AutomatorViewModel,
  modifier: Modifier = Modifier
) {
  val logs by viewModel.logs.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf<LogLevel?>(null) }

  val filteredLogs = remember(logs, searchQuery, selectedFilter) {
    logs.filter { log ->
      val matchesFilter = selectedFilter == null || log.level == selectedFilter
      val matchesSearch = searchQuery.isBlank() || log.message.contains(searchQuery, ignoreCase = true)
      matchesFilter && matchesSearch
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFEF7FF))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "System Console & Errors",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "${logs.size} total system & execution events recorded",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }

      OutlinedButton(
        onClick = { viewModel.clearLogs() },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("clear_logs_btn")
      ) {
        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Clear", fontSize = 11.sp)
      }
    }

    // Search Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text("Filter console messages...") },
      leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryPurple,
        unfocusedBorderColor = OutlineColor
      )
    )

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          label = "All (${logs.size})",
          isSelected = selectedFilter == null,
          onClick = { selectedFilter = null }
        )
      }
      items(LogLevel.values()) { level ->
        val count = logs.count { it.level == level }
        FilterChip(
          label = "${level.name} ($count)",
          isSelected = selectedFilter == level,
          onClick = { selectedFilter = level }
        )
      }
    }

    // Dark Terminal Window
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("full_logs_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ConsoleBackground)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LIVE CONSOLE STREAM",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = ConsolePurpleAccent
          )
          Text(
            text = "Encoding: UTF-8 • Monospace",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray
          )
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ConsoleBorder)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredLogs.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No log records match the current filter criteria.",
              color = Color.Gray,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(filteredLogs, key = { it.id }) { log ->
              val textColor = when (log.level) {
                LogLevel.ERROR -> StatusRed
                LogLevel.SUCCESS -> StatusGreen
                LogLevel.NETWORK -> StatusBlue
                LogLevel.AI_ACTION -> ConsolePurpleAccent
                LogLevel.WARNING -> StatusYellow
                LogLevel.LEAD -> Color(0xFFFFD54F)
                LogLevel.INFO -> ConsoleText
              }

              Text(
                text = "[${log.timestamp}] [${log.level.name}] ${log.message}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                lineHeight = 15.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(100.dp))
      .background(if (isSelected) PrimaryPurple else SurfaceLight)
      .border(1.dp, if (isSelected) PrimaryPurple else OutlineColor, RoundedCornerShape(100.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) Color.White else TextPrimary
    )
  }
}
