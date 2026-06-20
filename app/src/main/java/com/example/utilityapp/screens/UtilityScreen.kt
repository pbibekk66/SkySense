package com.example.utilityapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.utilityapp.model.OpenMeteoResponse
import com.example.utilityapp.model.WeatherUiState
import com.example.utilityapp.viewmodels.WeatherViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

// Quick-pick cities shown as clickable chips
private val PRESET_CITIES = listOf(
    "Kathmandu", "Melbourne", "China"
)

@SuppressLint("MissingPermission")
@Composable
fun UtilityScreen(weatherViewModel: WeatherViewModel = viewModel()) {
    val uiState by weatherViewModel.uiState.collectAsState()
    val useCelsius by weatherViewModel.useCelsius.collectAsState()
    val showFeelsLike by weatherViewModel.showFeelsLike.collectAsState()
    val showHumidity by weatherViewModel.showHumidity.collectAsState()
    val showWind by weatherViewModel.showWind.collectAsState()

    var searchText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val unit = if (useCelsius) "°C" else "°F"

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permission granted, fetch location
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        weatherViewModel.fetchWeatherByLocation(context, it.latitude, it.longitude)
                    }
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Search bar ─────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search Country, City") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(onSearch = {
                        weatherViewModel.fetchWeather(searchText)
                        focusManager.clearFocus()
                    }),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        
                        if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { location ->
                                    location?.let {
                                        weatherViewModel.fetchWeatherByLocation(context, it.latitude, it.longitude)
                                    }
                                }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Preset city chips ──────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PRESET_CITIES.size) { index ->
                    val city = PRESET_CITIES[index]
                    CityChip(city = city, onClick = {
                        searchText = city
                        weatherViewModel.fetchWeather(city)
                        focusManager.clearFocus()
                    })
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Content area ───────────────────────────────────────────────
        item {
            when (val state = uiState) {
                is WeatherUiState.Idle -> IdlePrompt()
                is WeatherUiState.Loading -> LoadingIndicator()
                is WeatherUiState.Error -> ErrorCard(state.message)
                is WeatherUiState.Success -> {
                    WeatherCard(
                        city = state.city,
                        country = state.country,
                        weather = state.weather,
                        useCelsius = useCelsius,
                        showFeelsLike = showFeelsLike,
                        showHumidity = showHumidity,
                        showWind = showWind
                    )
                    Spacer(Modifier.height(16.dp))
                    ForecastSection(weather = state.weather, useCelsius = useCelsius)
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(
    city: String,
    country: String,
    weather: OpenMeteoResponse,
    useCelsius: Boolean,
    showFeelsLike: Boolean,
    showHumidity: Boolean,
    showWind: Boolean
) {
    val current = weather.currentWeather
    val temp = if (useCelsius) current.temperature else (current.temperature * 9/5) + 32
    val unit = if (useCelsius) "°C" else "°F"
    val code = current.weatherCode
    val gradientColors = gradientForCode(code)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradientColors))
            .height(intrinsicSize = IntrinsicSize.Min)
    ) {
        // --- Animated Background Overlay ---
        WeatherAnimationBackground(code = code, windSpeed = current.windspeed)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "$city, $country",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${weatherEmoji(code)}  ${weatherDesc(code)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "${temp.toInt()}$unit",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            val minTemp = weather.daily.minTemps.firstOrNull() ?: 0.0
            val maxTemp = weather.daily.maxTemps.firstOrNull() ?: 0.0
            val displayMin = if (useCelsius) minTemp else (minTemp * 9/5) + 32
            val displayMax = if (useCelsius) maxTemp else (maxTemp * 9/5) + 32

            Text(
                text = "↓ ${displayMin.toInt()}$unit  ↑ ${displayMax.toInt()}$unit",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showFeelsLike) WeatherDetail("Feels like", "${temp.toInt()}$unit")
                if (showHumidity) {
                    val humidity = weather.hourly.humidities.firstOrNull() ?: 0
                    WeatherDetail("Humidity", "$humidity%")
                }
                if (showWind) WeatherDetail("Wind", "${current.windspeed.toInt()} km/h")
                
                if (!showFeelsLike && !showHumidity && !showWind)
                    Text("Enable details in Settings", color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}

// ── Weather Animations ──────────────────────────────────────────────────

@Composable
private fun WeatherAnimationBackground(code: Int, windSpeed: Double) {
    val infiniteTransition = rememberInfiniteTransition(label = "weatherEffects")

    // Windy effect (can happen during any weather if wind is high)
    if (windSpeed > 15.0) {
        val windStep by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)), label = "wind"
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val count = 10
            for (i in 0 until count) {
                val x = ((i * 200 + windStep * size.width) % size.width)
                val y = (size.height / count) * i
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(x, y),
                    end = Offset(x + 40, y),
                    strokeWidth = 3f
                )
            }
        }
    }

    when (code) {
        // --- Sunny / Clear ---
        0, 1 -> {
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)), label = "sun"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                rotate(rotation) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.maxDimension / 2
                        )
                    )
                }
            }
        }
        // --- Rainy / Drizzle ---
        in 51..65, in 80..82 -> {
            val rainStep by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "rain"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val count = 30
                for (i in 0 until count) {
                    val x = (size.width / count) * i
                    val y = ((i * 100 + rainStep * size.height) % size.height)
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(x, y),
                        end = Offset(x - 5, y + 20),
                        strokeWidth = 2f
                    )
                }
            }
        }
        // --- Thunderstorm ---
        in 95..99 -> {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 3000
                        0f at 0
                        0f at 2500
                        0.5f at 2600
                        0f at 2700
                        0.8f at 2800
                        0f at 2900
                    }
                ), label = "lightning"
            )
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = alpha)))
            // Add some rain too
            WeatherAnimationBackground(code = 61, windSpeed = 0.0)
        }
        // --- Snowy ---
        in 71..77, in 85..86 -> {
            val snowStep by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "snow"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val count = 25
                for (i in 0 until count) {
                    val x = (size.width / count) * i + (Math.sin(snowStep.toDouble() * 5 + i).toFloat() * 20)
                    val y = ((i * 150 + snowStep * size.height) % size.height)
                    drawCircle(Color.White.copy(alpha = 0.5f), radius = 4f, center = Offset(x, y))
                }
            }
        }
        // --- Windy (represented via fast moving lines) ---
        else -> {
            // Clouds/Other - could add subtle movement here
        }
    }
}

// ── Existing Components ──────────────────────────────────────────────────

@Composable
private fun CityChip(city: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick)
    ) {
        Text(
            text = city,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun WeatherDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text(label, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ForecastSection(weather: OpenMeteoResponse, useCelsius: Boolean) {
    Text(
        text = "Next 24 Hours",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(Modifier.height(8.dp))
    val hourlyTimes = weather.hourly.time.take(24)
    val hourlyTemps = weather.hourly.temperatures.take(24)
    val hourlyCodes = weather.hourly.weatherCodes.take(24)
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(hourlyTimes) { index, timeStr ->
            if (index % 3 == 0) {
                val temp = hourlyTemps[index]
                val displayTemp = if (useCelsius) temp else (temp * 9/5) + 32
                val code = hourlyCodes[index]
                ForecastChip(timeStr = timeStr, temp = displayTemp, code = code, unit = if (useCelsius) "°C" else "°F")
            }
        }
    }
}

@Composable
private fun ForecastChip(timeStr: String, temp: Double, code: Int, unit: String) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("h a", Locale.getDefault())
    val time = try {
        val date = inputFormat.parse(timeStr)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        timeStr.split("T").lastOrNull() ?: timeStr
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(72.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(weatherEmoji(code), fontSize = 20.sp)
            Text("${temp.toInt()}$unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun IdlePrompt() {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🌍", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text("Search a city/country or tap a chip above", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⚠️", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────

private fun weatherEmoji(code: Int): String = when (code) {
    0               -> "☀️"
    1, 2, 3         -> "🌤️"
    45, 48          -> "🌫️"
    51, 53, 55      -> "🌦️"
    61, 63, 65      -> "🌧️"
    71, 73, 75      -> "❄️"
    77              -> "❄️"
    80, 81, 82      -> "🌧️"
    85, 86          -> "❄️"
    95              -> "⛈️"
    96, 99          -> "⛈️"
    else            -> "🌈"
}

private fun weatherDesc(code: Int): String = when (code) {
    0               -> "Clear sky"
    1, 2, 3         -> "Partly cloudy"
    45, 48          -> "Fog"
    51, 53, 55      -> "Drizzle"
    61, 63, 65      -> "Rain"
    71, 73, 75      -> "Snow"
    77              -> "Snow grains"
    80, 81, 82      -> "Rain showers"
    85, 86          -> "Snow showers"
    95              -> "Thunderstorm"
    96, 99          -> "Thunderstorm with hail"
    else            -> "Unknown"
}

private fun gradientForCode(code: Int): List<Color> = when (code) {
    0, 1            -> listOf(Color(0xFFFF8C00), Color(0xFFFF4500))
    2, 3            -> listOf(Color(0xFF607D8B), Color(0xFF37474F))
    45, 48          -> listOf(Color(0xFF90A4AE), Color(0xFF546E7A))
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
    71, 73, 75, 77, 85, 86 -> listOf(Color(0xFF81D4FA), Color(0xFF4FC3F7))
    95, 96, 99      -> listOf(Color(0xFF37474F), Color(0xFF1A237E))
    else            -> listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
}
