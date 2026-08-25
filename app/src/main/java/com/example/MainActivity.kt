package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LiveBrowserScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.PersonaScreen
import com.example.ui.screens.TaskConfigScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryPurpleDark
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AutomatorViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: AutomatorViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CpaAutomatorApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun CpaAutomatorApp(viewModel: AutomatorViewModel) {
  val activeTab by viewModel.activeTab.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color(0xFFFEF7FF),
    bottomBar = {
      CpaBottomNavigation(
        activeTab = activeTab,
        onTabSelected = { viewModel.setActiveTab(it) }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (activeTab) {
        0 -> DashboardScreen(viewModel = viewModel)
        1 -> LiveBrowserScreen(viewModel = viewModel)
        2 -> PersonaScreen(viewModel = viewModel)
        3 -> TaskConfigScreen(viewModel = viewModel)
        4 -> LogsScreen(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun CpaBottomNavigation(
  activeTab: Int,
  onTabSelected: (Int) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(SurfaceLight)
      .navigationBarsPadding()
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(SurfaceBorder)
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 4.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavTabItem(
        icon = Icons.Outlined.Dashboard,
        label = "Dashboard",
        isSelected = activeTab == 0,
        onClick = { onTabSelected(0) },
        testTag = "nav_dashboard"
      )
      NavTabItem(
        icon = Icons.Outlined.TravelExplore,
        label = "Browser",
        isSelected = activeTab == 1,
        onClick = { onTabSelected(1) },
        testTag = "nav_browser"
      )
      NavTabItem(
        icon = Icons.Outlined.Person,
        label = "Proxy & ID",
        isSelected = activeTab == 2,
        onClick = { onTabSelected(2) },
        testTag = "nav_identity"
      )
      NavTabItem(
        icon = Icons.Outlined.Settings,
        label = "Tasks & UTM",
        isSelected = activeTab == 3,
        onClick = { onTabSelected(3) },
        testTag = "nav_tasks"
      )
      NavTabItem(
        icon = Icons.Outlined.Terminal,
        label = "Logs",
        isSelected = activeTab == 4,
        onClick = { onTabSelected(4) },
        testTag = "nav_logs"
      )
    }
  }
}

@Composable
private fun NavTabItem(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    if (isSelected) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(100.dp))
          .background(PrimaryPurpleLight)
          .padding(horizontal = 16.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = PrimaryPurpleDark,
          modifier = Modifier.size(20.dp)
        )
      }
    } else {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = TextSecondary.copy(alpha = 0.7f),
        modifier = Modifier.size(22.dp)
      )
    }

    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) PrimaryPurpleDark else TextSecondary
    )
  }
}

