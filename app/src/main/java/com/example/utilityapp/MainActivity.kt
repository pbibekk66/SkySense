package com.example.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.utilityapp.screens.SettingsScreen
import com.example.utilityapp.screens.UtilityScreen
import com.example.utilityapp.utils.Translations
import com.example.utilityapp.ui.theme.UtilityAppTheme
import com.example.utilityapp.viewmodels.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val weatherViewModel: WeatherViewModel = viewModel()
            val isDarkMode by weatherViewModel.isDarkMode.collectAsState()
            
            UtilityAppTheme(darkTheme = isDarkMode) {
                WeatherApp(weatherViewModel)
            }
        }
    }
}

@Composable
fun WeatherApp(weatherViewModel: WeatherViewModel) {
    // Single shared ViewModel — both screens read the same state
    val currentLang by weatherViewModel.currentLanguage.collectAsState()
    var selectedTab by remember { mutableStateOf("Utility") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(Translations.getString("weather", currentLang)) },
                    selected = selectedTab == "Utility",
                    onClick = { selectedTab = "Utility" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(Translations.getString("settings", currentLang)) },
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "Utility"  -> UtilityScreen(weatherViewModel)
                "Settings" -> SettingsScreen(weatherViewModel)
            }
        }
    }
}
