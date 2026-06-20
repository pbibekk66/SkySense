package com.example.utilityapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
                        weather = state.weather,
                        useCelsius = useCelsius,
                        showFeelsLike = showFeelsLike,
                        showHumidity = showHumidity,
                        showWind = showWind
                    )
                    Spacer(Modifier.height(8.dp))
                    ForecastSection(weather = state.weather, useCelsius = useCelsius)
                    Spacer(Modifier.height(12.dp))
                    SmartAssistantSection(weather = state.weather)
                }
            }
        }
    }
}

@Composable
private fun SmartAssistantSection(weather: OpenMeteoResponse) {
    val current = weather.currentWeather
    val temp = current.temperature
    val code = current.weatherCode
    val wind = current.windspeed

    // ... logic remains same ...
    val clothing = when {
        temp < 10 -> "Heavy winter gear is a must."
        temp in 10.0..20.0 -> "A light jacket should be enough."
        temp in 20.0..30.0 -> "T-shirts and shorts are ideal."
        else -> "Stay cool with breathable clothing."
    } + if (code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)) " ☂️" else ""

    val waterIntake = if (temp > 25) "Drink ~3L of water." else "Drink ~2L of water."

    val travelAdvice = when {
        code in listOf(95, 96, 99) -> "Severe storms! Stay home."
        code in listOf(65, 75, 82, 86) -> "Heavy rain/snow. Expect delays."
        wind > 40 -> "High winds! Drive carefully."
        code in listOf(45, 48) -> "Foggy. Drive slowly."
        else -> "Perfect for travel!"
    }

    val isIndoorDay = code in listOf(51, 53, 55, 61, 63, 65, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99)

    val moodMessage = when (code) {
        0, 1 -> "Keep shining! ☀️"
        in 2..3 -> "Find your own sunshine. ☁️"
        in 45..48 -> "Stay calm and focused. 🌫️"
        in 51..65, in 80..82 -> "Nature is refreshing. 🌧️"
        in 71..77, in 85..86 -> "Winter magic! ❄️"
        in 95..99 -> "Storms pass. Stay strong! ⛈️"
        else -> "Make it amazing! 🌈"
    }

    val summary = buildString {
        val tempDesc = when { temp > 28 -> "hot"; temp > 20 -> "warm"; temp > 10 -> "cool"; else -> "cold" }
        append("Today is $tempDesc & ${weatherDesc(code).lowercase()}. ")
        append(if (temp < 15) "Dress warm " else "Wear light ")
        append("& ${if (isIndoorDay) "stay in." else "head out!"}")
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Smart Assistant",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            Text(moodMessage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MiniAssistantItem("👕", clothing)
                    MiniAssistantItem("💧", waterIntake)
                    MiniAssistantItem("🚗", travelAdvice)
                }
            }
        }
    }
}

@Composable
private fun MiniAssistantItem(emoji: String, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Text(emoji, fontSize = 18.sp)
        Text(text, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeatherCard(
    city: String,
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
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(gradientColors))
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                RoundedCornerShape(20.dp)
            )
            .height(intrinsicSize = IntrinsicSize.Min)
    ) {
        WeatherAnimationBackground(code = code, windSpeed = current.windspeed)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = weatherDesc(code),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${temp.toInt()}$unit",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = weatherEmoji(code),
                        fontSize = 32.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (showFeelsLike) MiniDetail("Feels like", "${temp.toInt()}$unit")
                if (showHumidity) {
                    val humidity = weather.hourly.humidities.firstOrNull() ?: 0
                    MiniDetail("Humidity", "$humidity%")
                }
                if (showWind) MiniDetail("Wind", "${current.windspeed.toInt()} km/h")
            }
        }
    }
}

@Composable
private fun MiniDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
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
private fun ForecastSection(weather: OpenMeteoResponse, useCelsius: Boolean) {
    Text(
        text = "Next 24 Hours",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(Modifier.height(6.dp))
    val hourlyTimes = weather.hourly.time.take(24)
    val hourlyTemps = weather.hourly.temperatures.take(24)
    val hourlyCodes = weather.hourly.weatherCodes.take(24)
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
