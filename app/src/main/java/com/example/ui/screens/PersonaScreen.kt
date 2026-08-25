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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.PersonaGenerator
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleDark
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AutomatorViewModel

@Composable
fun PersonaScreen(
  viewModel: AutomatorViewModel,
  modifier: Modifier = Modifier
) {
  val persona by viewModel.persona.collectAsState()
  val proxyProfile by viewModel.proxyProfile.collectAsState()
  val emailStats by viewModel.emailStats.collectAsState()

  var showEmailDialog by remember { mutableStateOf(false) }
  var emailInputText by remember { mutableStateOf("") }
  var ipInputText by remember { mutableStateOf(proxyProfile.ip) }

  val availableCountries = remember { PersonaGenerator.getAvailableCountries() }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFEF7FF))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Title
    item {
      Column {
        Text(
          text = "Synthetic Identity & Anti-Leak",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Country-specific personas, Luhn cards, and WebRTC shield",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    // Country Selector Row
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "SELECT COUNTRY PROFILE",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = TextSecondary
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(availableCountries) { (code, name) ->
            val isSelected = persona.countryCode == code
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (isSelected) PrimaryPurple else SurfaceLight)
                .border(
                  1.dp,
                  if (isSelected) PrimaryPurple else OutlineColor,
                  RoundedCornerShape(100.dp)
                )
                .clickable { viewModel.regeneratePersona(code) }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("country_pill_$code")
            ) {
              Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextPrimary
              )
            }
          }
        }
      }
    }

    // Full Persona Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(PrimaryPurple),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Column {
                Text(text = persona.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Born: ${persona.birthDate} • ${persona.country}", fontSize = 11.sp, color = TextSecondary)
              }
            }

            IconButton(
              onClick = { viewModel.regeneratePersona(persona.countryCode) },
              modifier = Modifier.testTag("regenerate_persona_btn")
            ) {
              Icon(Icons.Outlined.Refresh, contentDescription = "Regenerate", tint = PrimaryPurple)
            }
          }

          // Details List
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(Color.White)
              .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            DetailRow(icon = Icons.Outlined.Email, label = "Active Email", value = persona.email)
            DetailRow(icon = Icons.Outlined.Person, label = "Split Birth Date", value = "D: ${persona.birthDayPadded} • M: ${persona.birthMonthName} (${persona.birthMonth}) • Y: ${persona.birthYear} (Age: ${persona.age})")
            DetailRow(icon = Icons.Outlined.Person, label = "Gender & Title", value = "${persona.gender} (${persona.genderArabic}) • ${persona.title}")
            DetailRow(icon = Icons.Outlined.LocationOn, label = "Street & Unit", value = "${persona.streetAddress}, ${persona.streetAddress2}")
            DetailRow(icon = Icons.Outlined.LocationOn, label = "City & State", value = "${persona.city}, ${persona.state} (${persona.zipCode})")
            DetailRow(icon = Icons.Outlined.Language, label = "Timezone & Lang", value = "${persona.timezone} • ${persona.language.take(12)}...")
            DetailRow(icon = Icons.Outlined.CreditCard, label = "Payment Card", value = "${persona.cardNumber} [${persona.cardExpiry}]")
          }

          // CVV Rotation Box
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(text = "Rotated CVV/CVN:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
              Text(
                text = persona.cardCvv,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = PrimaryPurpleDark
              )
            }

            OutlinedButton(
              onClick = { viewModel.rotateCvv() },
              shape = RoundedCornerShape(100.dp),
              modifier = Modifier.testTag("rotate_cvv_btn")
            ) {
              Text("Rotate CVV", fontSize = 11.sp)
            }
          }
        }
      }
    }

    // Dedicated Proxy Configuration & Asocks Pool Manager Card
    item {
      val proxyStats by viewModel.proxyStats.collectAsState()
      val taskConfig by viewModel.taskConfig.collectAsState()

      var proxyInput by remember(proxyProfile.rawProxyInput) { mutableStateOf(proxyProfile.rawProxyInput) }
      var selectedProtocol by remember(proxyProfile.protocol) { mutableStateOf(proxyProfile.protocol) }
      var proxyUser by remember(proxyProfile.username) { mutableStateOf(proxyProfile.username) }
      var proxyPass by remember(proxyProfile.password) { mutableStateOf(proxyProfile.password) }
      var proxyListUrlInput by remember(taskConfig.proxyListUrl) { mutableStateOf(taskConfig.proxyListUrl) }

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Card Title
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "DYNAMIC RESIDENTIAL PROXY POOL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = PrimaryPurpleDark
              )
              Text(
                text = "Asocks Residential Rotation Link",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (proxyProfile.isConnected) StatusGreen.copy(alpha = 0.15f) else StatusYellow.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (proxyProfile.isConnected) "TUNNEL ACTIVE" else "STANDBY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (proxyProfile.isConnected) StatusGreen else StatusYellow
              )
            }
          }

          // Asocks / Custom Proxy List URL
          OutlinedTextField(
            value = proxyListUrlInput,
            onValueChange = { proxyListUrlInput = it },
            label = { Text("Proxy List URL (Asocks / Residential)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PrimaryPurple,
              unfocusedBorderColor = OutlineColor
            )
          )

          // Fetch & Rotate Action Buttons Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.fetchProxyListFromUrl(proxyListUrlInput)
                viewModel.updateTaskConfig(taskConfig.copy(proxyListUrl = proxyListUrlInput))
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
              modifier = Modifier.weight(1.3f).testTag("fetch_proxies_btn")
            ) {
              Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Fetch Proxies", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { viewModel.rotateToNextProxy(manual = true) },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("rotate_proxy_btn")
            ) {
              Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryPurple)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Next Proxy", fontSize = 11.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
            }
          }

          // Pool status info box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(PrimaryPurpleLight.copy(alpha = 0.5f))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Pool Status: ${proxyStats.lastStatus}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
              )
              Text(
                text = "Index #${proxyStats.currentIndex + 1}/${proxyStats.total}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = PrimaryPurpleDark
              )
            }
          }

          // Protocol Selector (SOCKS5, HTTP, HTTPS)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("SOCKS5", "HTTP", "HTTPS").forEach { proto ->
              val isSelected = selectedProtocol == proto
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) PrimaryPurple else Color.White)
                  .border(1.dp, if (isSelected) PrimaryPurple else OutlineColor, RoundedCornerShape(10.dp))
                  .clickable { selectedProtocol = proto }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = proto,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else TextPrimary
                )
              }
            }
          }

          // Raw Proxy Input (IP:Port or IP:Port:User:Pass)
          OutlinedTextField(
            value = proxyInput,
            onValueChange = { proxyInput = it },
            label = { Text("Active Proxy Credentials (IP:Port or IP:Port:User:Pass)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PrimaryPurple,
              unfocusedBorderColor = OutlineColor
            )
          )

          // Username & Password optional row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = proxyUser,
              onValueChange = { proxyUser = it },
              label = { Text("Username") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = OutlineColor
              )
            )
            OutlinedTextField(
              value = proxyPass,
              onValueChange = { proxyPass = it },
              label = { Text("Password") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = OutlineColor
              )
            )
          }

          Button(
            onClick = {
              viewModel.updateProxySettings(
                rawProxy = proxyInput,
                protocol = selectedProtocol,
                username = proxyUser,
                password = proxyPass
              )
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            modifier = Modifier.fillMaxWidth().testTag("apply_proxy_btn")
          ) {
            Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Apply Active Proxy & Sync Geo Identity", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Anti-Leak Shield Tags
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            ShieldTag(text = "WebRTC Leak: Blocked", color = StatusGreen)
            ShieldTag(text = "DNS Leak: Sealed", color = StatusGreen)
            ShieldTag(text = "Timezone Spoofed", color = StatusBlue)
          }
        }
      }
    }

    // Email Pool Manager Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "EMAIL POOL (FROM .TXT POOL)",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = TextSecondary
            )

            Text(
              text = "${emailStats.first} Active • ${emailStats.second} Used (Purged)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryPurpleDark
            )
          }

          Text(
            text = "Emails are preserved during in-session task repeats and automatically purged when the browser closes.",
            fontSize = 11.sp,
            color = TextSecondary
          )

          OutlinedTextField(
            value = emailInputText,
            onValueChange = { emailInputText = it },
            label = { Text("Paste new emails (line or comma separated)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PrimaryPurple,
              unfocusedBorderColor = OutlineColor
            )
          )

          Button(
            onClick = {
              if (emailInputText.isNotBlank()) {
                viewModel.addEmails(emailInputText)
                emailInputText = ""
              }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Import Emails to Pool")
          }
        }
      }
    }
  }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
      Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
    Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
  }
}

@Composable
private fun ShieldTag(text: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.15f))
      .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
  }
}
