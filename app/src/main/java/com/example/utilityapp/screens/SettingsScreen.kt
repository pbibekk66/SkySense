package com.example.utilityapp.screens

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

@Composable
fun SettingsScreen(weatherViewModel: WeatherViewModel = viewModel()) {

    val useCelsius   by weatherViewModel.useCelsius.collectAsState()
    val showFeelsLike by weatherViewModel.showFeelsLike.collectAsState()
    val showHumidity  by weatherViewModel.showHumidity.collectAsState()
    val showWind      by weatherViewModel.showWind.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // ── Temperature unit ──────────────────────────────────────────
        SettingsGroupLabel("Temperature Unit")

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
        SettingsGroupLabel("Main Screen Details")
        Text(
            text = "Choose which extra details appear on the weather card.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        SettingsToggleRow(
            label = "Feels Like",
            description = "Show apparent temperature",
            checked = showFeelsLike,
            onCheckedChange = { weatherViewModel.toggleFeelsLike() }
        )

        SettingsToggleRow(
            label = "Humidity",
            description = "Show relative humidity percentage",
            checked = showHumidity,
            onCheckedChange = { weatherViewModel.toggleHumidity() }
        )

        SettingsToggleRow(
            label = "Wind Speed",
            description = "Show current wind speed in m/s",
            checked = showWind,
            onCheckedChange = { weatherViewModel.toggleWind() }
        )

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
