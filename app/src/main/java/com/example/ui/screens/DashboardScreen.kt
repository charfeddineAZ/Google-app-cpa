package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutomationState
import com.example.model.LogLevel
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.ConsolePurpleAccent
import com.example.ui.theme.ConsoleText
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleDark
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AutomatorViewModel

@Composable
fun DashboardScreen(
  viewModel: AutomatorViewModel,
  modifier: Modifier = Modifier
) {
  val persona by viewModel.persona.collectAsState()
  val proxyProfile by viewModel.proxyProfile.collectAsState()
  val taskConfig by viewModel.taskConfig.collectAsState()
  val leadStatus by viewModel.leadStatus.collectAsState()
  val logs by viewModel.logs.collectAsState()
  val isAutomating by viewModel.isAutomating.collectAsState()
  val automationState by viewModel.automationState.collectAsState()
  val currentCycle by viewModel.currentCycle.collectAsState()
  val currentTaskRepeat by viewModel.currentTaskRepeat.collectAsState()
  val countdown by viewModel.countdownSeconds.collectAsState()

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFEF7FF))
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "CPA Automator",
          fontSize = 24.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary,
          letterSpacing = (-0.5).sp
        )
          Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Proxy: ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
          )
          Text(
            text = "${proxyProfile.countryName} (${proxyProfile.ip})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = StatusBlue
          )
          if (taskConfig.autoRotateProxyEachCycle) {
            Text(
              text = " • [Auto-Rotate ON]",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = StatusGreen
            )
          }
        }
      }

      // Control Action Buttons (Start / Stop)
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Start Button
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isAutomating) PrimaryPurple else PrimaryPurpleLight)
            .clickable(enabled = !isAutomating) { viewModel.startAutomation() }
            .testTag("start_automation_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Start Automation",
            tint = if (isAutomating) Color.White else PrimaryPurpleDark,
            modifier = Modifier.size(26.dp)
          )
        }

        // Stop Button
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(SurfaceLight)
            .border(1.dp, OutlineColor, CircleShape)
            .clickable(enabled = isAutomating) { viewModel.stopAutomation() }
            .testTag("stop_automation_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop Automation",
            tint = if (isAutomating) StatusRed else TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }

    // Active Automation Live Bar (When Running)
    AnimatedVisibility(visible = isAutomating || automationState != AutomationState.IDLE) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SecondaryContainer,
        border = CardDefaults.outlinedCardBorder()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isAutomating) StatusGreen else StatusYellow)
                .alpha(pulseAlpha)
            )
            val cycleText = if (taskConfig.unlimitedCycles) "Cycle $currentCycle [Continuous]" else "Cycle $currentCycle/${taskConfig.processRepeatCount}"
            Text(
              text = "$cycleText • Repeat $currentTaskRepeat • ${automationState.name.replace('_', ' ')}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryPurpleDark
            )
          }

          Text(
            text = "${countdown}s",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = PrimaryPurple
          )
        }
      }
    }

    // Generated Persona Card (Rounded 28dp matching HTML Design)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("persona_card"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceLight),
      border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Persona Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryPurple),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Persona",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }

            Column {
              Text(
                text = "GENERATED PERSONA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
              )
              Text(
                text = persona.fullName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }
          }

          // Synced Chip
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(100.dp))
              .background(Color(0xFFD0BCFF))
              .padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Text(
              text = "SYNCED",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryPurpleDark,
              letterSpacing = 0.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Persona Details Subgrid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Credit Card Box
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(Color.White.copy(alpha = 0.7f))
              .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
              .padding(10.dp)
          ) {
            Column {
              Text(
                text = "Credit Card",
                fontSize = 11.sp,
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = persona.cardNumber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
              )
              Text(
                text = "CVV: ${persona.cardCvv} (Rotated)",
                fontSize = 10.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = TextSecondary
              )
            }
          }

          // Location Box
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(Color.White.copy(alpha = 0.7f))
              .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
              .padding(10.dp)
          ) {
            Column {
              Text(
                text = "Location",
                fontSize = 11.sp,
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${persona.city}, ${persona.streetAddress.take(14)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "${persona.timezone.take(15)} (${persona.country})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
              )
            }
          }
        }
      }
    }

    // Automation Mode & Lead Status Row (Matching HTML cards)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Automation Mode Card
      Card(
        modifier = Modifier
          .weight(1f)
          .height(96.dp)
          .testTag("mode_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "AUTOMATION MODE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f),
            letterSpacing = 0.8.sp
          )
          Text(
            text = taskConfig.selectedMode.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }

      // Lead Status Card
      Card(
        modifier = Modifier
          .weight(1f)
          .height(96.dp)
          .testTag("lead_status_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (leadStatus.isLeadDetected) Color(0xFFE8F5E9) else SecondaryContainer
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "LEAD STATUS",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (leadStatus.isLeadDetected) StatusGreen else TextSecondary,
              letterSpacing = 0.8.sp
            )
            Icon(
              imageVector = Icons.Outlined.Refresh,
              contentDescription = "Check Feed",
              tint = PrimaryPurple,
              modifier = Modifier
                .size(16.dp)
                .clickable { viewModel.manualLeadCheck() }
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Column {
              Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = String.format("%02d", leadStatus.successfulLeadsCount),
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (leadStatus.isLeadDetected) StatusGreen else Color(0xFF1D192B)
                )
                Text(
                  text = "Leads",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = TextSecondary,
                  modifier = Modifier.padding(bottom = 2.dp)
                )
              }
              Text(
                text = if (leadStatus.isLeadDetected) "🎉 Converted" else "Status: N/A",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (leadStatus.isLeadDetected) StatusGreen else StatusYellow
              )
            }

            Text(
              text = "Checks: ${leadStatus.totalChecks}",
              fontSize = 10.sp,
              color = TextSecondary,
              modifier = Modifier.padding(bottom = 2.dp)
            )
          }
        }
      }
    }

    // System Logs Terminal Box (Dark Theme #1C1B1F with rounded 24dp)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("system_logs_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = ConsoleBackground)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp)
      ) {
        // Logs Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SYSTEM LOGS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = ConsolePurpleAccent
          )

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isAutomating) StatusGreen else StatusYellow)
                .alpha(if (isAutomating) pulseAlpha else 1f)
            )
            Text(
              text = if (isAutomating) "LIVE" else "IDLE",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (isAutomating) StatusGreen else StatusYellow
            )
          }
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ConsoleBorder)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Logs Stream
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("logs_stream_list"),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(logs, key = { it.id }) { log ->
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
              text = "[${log.timestamp}] ${log.message}",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = textColor,
              lineHeight = 14.sp
            )
          }
        }
      }
    }
  }
}
