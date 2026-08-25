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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.AutomationMode
import com.example.model.TaskConfig
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleDark
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AutomatorViewModel

@Composable
fun TaskConfigScreen(
  viewModel: AutomatorViewModel,
  modifier: Modifier = Modifier
) {
  val currentConfig by viewModel.taskConfig.collectAsState()

  var offerUrl by remember(currentConfig) { mutableStateOf(currentConfig.offerUrl) }
  var referrerBaseUrl by remember(currentConfig) { mutableStateOf(currentConfig.referrerBaseUrl) }
  var utmSource by remember(currentConfig) { mutableStateOf(currentConfig.utmSource) }
  var utmMedium by remember(currentConfig) { mutableStateOf(currentConfig.utmMedium) }
  var utmCampaign by remember(currentConfig) { mutableStateOf(currentConfig.utmCampaign) }
  var utmContent by remember(currentConfig) { mutableStateOf(currentConfig.utmContent) }

  var selectedMode by remember(currentConfig) { mutableStateOf(currentConfig.selectedMode) }
  var browserDuration by remember(currentConfig) { mutableFloatStateOf(currentConfig.browserDurationSeconds.toFloat()) }
  var taskDuration by remember(currentConfig) { mutableFloatStateOf(currentConfig.taskDurationSeconds.toFloat()) }
  var processRepeats by remember(currentConfig) { mutableFloatStateOf(currentConfig.processRepeatCount.toFloat()) }
  var inSessionRepeats by remember(currentConfig) { mutableFloatStateOf(currentConfig.taskRepeatCount.toFloat()) }
  var unlimitedCycles by remember(currentConfig) { mutableStateOf(currentConfig.unlimitedCycles) }
  var autoRotateProxy by remember(currentConfig) { mutableStateOf(currentConfig.autoRotateProxyEachCycle) }
  var proxyListUrl by remember(currentConfig) { mutableStateOf(currentConfig.proxyListUrl) }

  var cpaUserId by remember(currentConfig) { mutableStateOf(currentConfig.cpaGripUserId) }
  var cpaKey by remember(currentConfig) { mutableStateOf(currentConfig.cpaGripKey) }

  val userAgents = listOf(
    "iPhone / Safari iOS 17.4 (Mobile)",
    "Android Chrome 124 (Mobile)",
    "Windows 11 / Chrome 124 (Desktop)",
    "Mac OS X / Safari 17 (Desktop)"
  )
  var selectedUaIndex by remember(currentConfig) { mutableIntStateOf(currentConfig.selectedUserAgentIndex) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFEF7FF))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text(
          text = "Task & UTM Automation Configuration",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Setup target URL, UTM tags, 3 intelligent modes & CPAGrip RSS",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    // 3 Smart Modes Selector Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "3 SMART AUTOMATION MODES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
          )

          AutomationMode.values().forEach { mode ->
            val isSelected = selectedMode == mode
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) PrimaryPurpleLight.copy(alpha = 0.5f) else Color.White)
                .border(
                  1.dp,
                  if (isSelected) PrimaryPurple else OutlineColor,
                  RoundedCornerShape(16.dp)
                )
                .clickable { selectedMode = mode }
                .padding(12.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = { selectedMode = mode },
                  colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
                )
                Column {
                  Text(
                    text = mode.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PrimaryPurpleDark else TextPrimary
                  )
                  Text(
                    text = mode.shortDesc,
                    fontSize = 11.sp,
                    color = TextSecondary
                  )
                }
              }
            }
          }

          // Mode Parameters Sliders
          Spacer(modifier = Modifier.height(4.dp))

          // Browser Duration
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Browser Session Duration:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
              Text("${browserDuration.toInt()}s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
            Slider(
              value = browserDuration,
              onValueChange = { browserDuration = it },
              valueRange = 20f..120f,
              colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
            )
          }

          // In-Session Repeats (Mode 2)
          if (selectedMode == AutomationMode.MODE_2_TASK_IN_SESSION) {
            Column {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("In-Session Task Repeats (Clears Cache/Cookies):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Text("${inSessionRepeats.toInt()}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
              }
              Slider(
                value = inSessionRepeats,
                onValueChange = { inSessionRepeats = it },
                valueRange = 1f..10f,
                colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
              )
            }
          }

          // Unlimited vs Fixed Cycles Selector
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(if (unlimitedCycles) PrimaryPurpleLight.copy(alpha = 0.6f) else Color.White)
              .border(1.dp, if (unlimitedCycles) PrimaryPurple else OutlineColor, RoundedCornerShape(12.dp))
              .clickable { unlimitedCycles = !unlimitedCycles }
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Continuous Unlimited Loop (تكرار مستمر بلا حدود)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (unlimitedCycles) PrimaryPurpleDark else TextPrimary)
              Text("Repeats cycles continuously until stopped by user", fontSize = 10.sp, color = TextSecondary)
            }
            androidx.compose.material3.Switch(
              checked = unlimitedCycles,
              onCheckedChange = { unlimitedCycles = it },
              colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurpleLight)
            )
          }

          // Process Repeats (Cycles) - only if not unlimited
          if (!unlimitedCycles) {
            Column {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Process Repeat Cycles (Full New Session & Persona):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Text("${processRepeats.toInt()}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
              }
              Slider(
                value = processRepeats,
                onValueChange = { processRepeats = it },
                valueRange = 1f..20f,
                colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
              )
            }
          }
        }
      }
    }

    // Offer URL & UTM Builder Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "TARGET CPA URL & UTM REFERRER",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
          )

          OutlinedTextField(
            value = offerUrl,
            onValueChange = { offerUrl = it },
            label = { Text("CPA Offer URL") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
          )

          OutlinedTextField(
            value = referrerBaseUrl,
            onValueChange = { referrerBaseUrl = it },
            label = { Text("Referrer Site URL (e.g. google.eg)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
          )

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = utmSource,
              onValueChange = { utmSource = it },
              label = { Text("utm_source") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
            )
            OutlinedTextField(
              value = utmMedium,
              onValueChange = { utmMedium = it },
              label = { Text("utm_medium") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
            )
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = utmCampaign,
              onValueChange = { utmCampaign = it },
              label = { Text("utm_campaign") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
            )
            OutlinedTextField(
              value = utmContent,
              onValueChange = { utmContent = it },
              label = { Text("utm_content") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
            )
          }

          // Live Generated URL Preview
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF1C1B1F))
              .padding(10.dp)
          ) {
            Column {
              Text("Generated UTM Referral URL:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
              Spacer(modifier = Modifier.height(2.dp))
              val sep = if (offerUrl.contains("?")) "&" else "?"
              Text(
                text = "$offerUrl${sep}utm_source=$utmSource&utm_medium=$utmMedium&utm_campaign=$utmCampaign&utm_content=$utmContent",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE6E1E5)
              )
            }
          }
        }
      }
    }

    // User-Agent Selector Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "BROWSER USER-AGENT SPOOFING",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
          )

          userAgents.forEachIndexed { index, ua ->
            val isSelected = selectedUaIndex == index
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) PrimaryPurpleLight.copy(alpha = 0.5f) else Color.White)
                .clickable { selectedUaIndex = index }
                .padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { selectedUaIndex = index },
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
              )
              Text(text = ua, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = TextPrimary)
            }
          }
        }
      }
    }

    // CPAGrip Lead RSS Feed Settings Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "CPAGRIP LEAD RSS VERIFICATION (DIRECT / NO PROXY)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary
          )

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = cpaUserId,
              onValueChange = { cpaUserId = it },
              label = { Text("User ID") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
            )
            OutlinedTextField(
              value = cpaKey,
              onValueChange = { cpaKey = it },
              label = { Text("API Key") },
              modifier = Modifier.weight(2f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = OutlineColor)
            )
          }

          Text(
            text = "Endpoint: https://www.cpagrip.com/common/lead_check_rss.php?user_id=$cpaUserId&key=$cpaKey&time=1day&check=ip&value=IP",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
          )
        }
      }
    }

    // Save Button
    item {
      Button(
        onClick = {
          val updated = currentConfig.copy(
            offerUrl = offerUrl,
            referrerBaseUrl = referrerBaseUrl,
            utmSource = utmSource,
            utmMedium = utmMedium,
            utmCampaign = utmCampaign,
            utmContent = utmContent,
            selectedMode = selectedMode,
            browserDurationSeconds = browserDuration.toInt(),
            taskDurationSeconds = taskDuration.toInt(),
            processRepeatCount = processRepeats.toInt(),
            taskRepeatCount = inSessionRepeats.toInt(),
            unlimitedCycles = unlimitedCycles,
            autoRotateProxyEachCycle = autoRotateProxy,
            proxyListUrl = proxyListUrl,
            cpaGripUserId = cpaUserId,
            cpaGripKey = cpaKey,
            selectedUserAgentIndex = selectedUaIndex
          )
          viewModel.updateTaskConfig(updated)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_config_btn"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
      ) {
        Icon(Icons.Outlined.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save & Apply Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
