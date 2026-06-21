package com.example.utilityapp.viewmodels

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.utilityapp.api.RetrofitInstance
import com.example.utilityapp.model.WeatherUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherViewModel : ViewModel() {

    // --- Weather state ---
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState

    // --- Settings state (controlled by SettingsScreen) ---
    private val _useCelsius = MutableStateFlow(true)
    val useCelsius: StateFlow<Boolean> = _useCelsius

    private val _showFeelsLike = MutableStateFlow(true)
    val showFeelsLike: StateFlow<Boolean> = _showFeelsLike

    private val _showHumidity = MutableStateFlow(true)
    val showHumidity: StateFlow<Boolean> = _showHumidity

    private val _showWind = MutableStateFlow(true)
    val showWind: StateFlow<Boolean> = _showWind

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _currentLanguage = MutableStateFlow("English")
    val currentLanguage: StateFlow<String> = _currentLanguage

    // Cache last search so we can re-fetch when units toggle
    private var lastCitySearch: String = ""
    private var lastLocation: Pair<Double, Double>? = null

    // --- Public API ---

    fun fetchWeather(city: String) {
        if (city.isBlank()) return
        lastCitySearch = city.trim()
        lastLocation = null
        _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            try {
                // 1. Get coordinates for the city
                val geoResponse = RetrofitInstance.geoApi.search(lastCitySearch)
                val location = geoResponse.results?.firstOrNull()
                
                if (location == null) {
                    _uiState.value = WeatherUiState.Error("Location \"$lastCitySearch\" not found.")
                    return@launch
                }

                // 2. Get weather for those coordinates
                val weather = RetrofitInstance.api.getForecast(
                    lat = location.latitude,
                    lon = location.longitude
                )
                
                _uiState.value = WeatherUiState.Success(
                    city = location.name,
                    country = location.country,
                    weather = weather
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun fetchWeatherByLocation(context: Context, lat: Double, lon: Double) {
        lastLocation = Pair(lat, lon)
        lastCitySearch = ""
        _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            try {
                // Try to get city name via Geocoder
                val cityName = withContext(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        val address = addresses?.firstOrNull()
                        address?.locality ?: address?.subAdminArea ?: "Current Location"
                    } catch (e: Exception) {
                        "Current Location"
                    }
                }

                val weather = RetrofitInstance.api.getForecast(lat = lat, lon = lon)
                _uiState.value = WeatherUiState.Success(
                    city = cityName,
                    country = "",
                    weather = weather
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun toggleUnit(context: Context) {
        _useCelsius.value = !_useCelsius.value
        if (lastLocation != null) {
            fetchWeatherByLocation(context, lastLocation!!.first, lastLocation!!.second)
        } else if (lastCitySearch.isNotBlank()) {
            fetchWeather(lastCitySearch)
        }
    }
    fun toggleFeelsLike() { _showFeelsLike.value = !_showFeelsLike.value }
    fun toggleHumidity()  { _showHumidity.value  = !_showHumidity.value  }
    fun toggleWind()      { _showWind.value       = !_showWind.value      }
    fun toggleDarkMode()  { _isDarkMode.value     = !_isDarkMode.value    }
    fun setLanguage(language: String) { _currentLanguage.value = language }
}
