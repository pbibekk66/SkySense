package com.example.utilityapp.model

import com.google.gson.annotations.SerializedName

// --- Open-Meteo API Models ---

data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    @SerializedName("admin1") val state: String? = null
)

data class OpenMeteoResponse(
    @SerializedName("current_weather") val currentWeather: CurrentWeather,
    val hourly: HourlyData,
    val daily: DailyData
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    @SerializedName("weathercode") val weatherCode: Int,
    val time: String
)

data class HourlyData(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperatures: List<Double>,
    @SerializedName("relative_humidity_2m") val humidities: List<Int>,
    @SerializedName("weather_code") val weatherCodes: List<Int>
)

data class DailyData(
    @SerializedName("temperature_2m_max") val maxTemps: List<Double>,
    @SerializedName("temperature_2m_min") val minTemps: List<Double>
)

// --- UI State ---

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(
        val city: String,
        val country: String,
        val weather: OpenMeteoResponse
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}
