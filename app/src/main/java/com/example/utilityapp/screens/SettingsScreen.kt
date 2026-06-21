package com.example.utilityapp.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.utilityapp.viewmodels.WeatherViewModel
import com.example.utilityapp.utils.Translations

@Composable
fun SettingsScreen(weatherViewModel: WeatherViewModel = viewModel()) {

    val useCelsius   by weatherViewModel.useCelsius.collectAsState()
    val showFeelsLike by weatherViewModel.showFeelsLike.collectAsState()
    val showHumidity  by weatherViewModel.showHumidity.collectAsState()
    val showWind      by weatherViewModel.showWind.collectAsState()
    val isDarkMode    by weatherViewModel.isDarkMode.collectAsState()
    val currentLang   by weatherViewModel.currentLanguage.collectAsState()

    val context = LocalContext.current
    val languages = listOf("English", "Nepali", "Chinese", "Hindi")
    var showLangDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Translations.getString("settings", currentLang),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // ── Temperature unit ──────────────────────────────────────────
        SettingsGroupLabel(Translations.getString("temp_unit", currentLang))

        SettingsToggleRow(
            label = if (useCelsius) "Celsius (°C)" else "Fahrenheit (°F)",
            description = "Switch between metric and imperial",
            checked = useCelsius,
            onCheckedChange = { weatherViewModel.toggleUnit(context) }
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // ── Visible details ───────────────────────────────────────────
        SettingsGroupLabel(Translations.getString("main_screen_details", currentLang))
        Text(
            text = "Choose which extra details appear on the weather card.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        SettingsToggleRow(
            label = Translations.getString("feels_like", currentLang),
            description = "Show apparent temperature",
            checked = showFeelsLike,
            onCheckedChange = { weatherViewModel.toggleFeelsLike() }
        )

        SettingsToggleRow(
            label = Translations.getString("humidity", currentLang),
            description = "Show relative humidity percentage",
            checked = showHumidity,
            onCheckedChange = { weatherViewModel.toggleHumidity() }
        )

        SettingsToggleRow(
            label = Translations.getString("wind_speed", currentLang),
            description = "Show current wind speed",
            checked = showWind,
            onCheckedChange = { weatherViewModel.toggleWind() }
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // ── Personalization ───────────────────────────────────────────
        SettingsGroupLabel(Translations.getString("personalization", currentLang))
        
        SettingsToggleRow(
            label = Translations.getString("night_mode", currentLang),
            description = "Enable dark theme across the app",
            checked = isDarkMode,
            onCheckedChange = { weatherViewModel.toggleDarkMode() }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLangDialog = true }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(Translations.getString("app_language", currentLang), style = MaterialTheme.typography.bodyLarge)
                Text(currentLang, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (showLangDialog) {
            AlertDialog(
                onDismissRequest = { showLangDialog = false },
                title = { Text("Select Language") },
                text = {
                    Column {
                        languages.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        weatherViewModel.setLanguage(lang)
                                        showLangDialog = false 
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (lang == currentLang), onClick = null)
                                Spacer(Modifier.width(8.dp))
                                Text(lang)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLangDialog = false }) { Text("Cancel") }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // ── Info card ─────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ℹ️  About Weather Data",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Weather data is provided by Open-Meteo (Free, No-Key). " +
                           "Forecasts refresh each time you search a city. " +
                           "Settings take effect immediately on the Utility screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// ── Reusable composables ──────────────────────────────────────────────────

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
