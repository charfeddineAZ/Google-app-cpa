package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.AppTab
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PromptScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.Ink
import com.example.ui.theme.InkElevated
import com.example.ui.theme.Lime
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Muted
import com.example.viewmodel.RapStudioViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: RapStudioViewModel by viewModels { RapStudioViewModel.factory(applicationContext) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          TounsiRapApp(viewModel)
        }
      }
    }
  }
}

/** Root composable for the Tounsi Rap Studio. */
@Composable
fun TounsiRapApp(viewModel: RapStudioViewModel) {
  val activeTab by viewModel.activeTab.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Ink,
    bottomBar = {
      NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = InkElevated,
        tonalElevation = 0.dp
      ) {
        BottomTab(AppTab.STUDIO, Icons.Outlined.Waves, activeTab, viewModel)
        BottomTab(AppTab.LIBRARY, Icons.Outlined.LibraryMusic, activeTab, viewModel)
        BottomTab(AppTab.DICTIONARY, Icons.Outlined.MenuBook, activeTab, viewModel)
        BottomTab(AppTab.PROMPT, Icons.Outlined.AutoAwesome, activeTab, viewModel)
      }
    }
  ) { padding ->
    Box(modifier = Modifier.fillMaxSize().background(Ink).padding(padding)) {
      when (activeTab) {
        AppTab.STUDIO -> StudioScreen(viewModel, Modifier.fillMaxSize().background(Ink))
        AppTab.LIBRARY -> LibraryScreen(viewModel, Modifier.fillMaxSize().background(Ink))
        AppTab.DICTIONARY -> DictionaryScreen(viewModel, Modifier.fillMaxSize().background(Ink))
        AppTab.PROMPT -> PromptScreen(viewModel, Modifier.fillMaxSize().background(Ink))
      }
    }
  }
}

@Composable
private fun BottomTab(
  tab: AppTab,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  activeTab: AppTab,
  viewModel: RapStudioViewModel
) {
  NavigationBarItem(
    selected = activeTab == tab,
    onClick = { viewModel.selectTab(tab) },
    icon = { Icon(icon, contentDescription = tab.label) },
    label = { Text(tab.label) },
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = Ink,
      selectedTextColor = Lime,
      indicatorColor = Lime,
      unselectedIconColor = Muted,
      unselectedTextColor = Muted
    )
  )
}
