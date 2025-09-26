package com.mohsenmashkour.bykingweatherappyt.presentation.viewModel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mohsenmashkour.bykingweatherappyt.data.remote.Config
import com.mohsenmashkour.bykingweatherappyt.domain.model.BikeRidingScore
import com.mohsenmashkour.bykingweatherappyt.domain.model.DailyForecast
import com.mohsenmashkour.bykingweatherappyt.domain.model.Temperature
import com.mohsenmashkour.bykingweatherappyt.domain.model.WeatherResponse
import com.mohsenmashkour.bykingweatherappyt.domain.usecase.CalculateBikeRidingScoreUseCase
import com.mohsenmashkour.bykingweatherappyt.domain.usecase.GetWeatherForecastUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherViewModel(
    application: Application,
    private val getWeatherForecastUseCase: GetWeatherForecastUseCase,
    private val calculateBikeRidingScoreUseCase: CalculateBikeRidingScoreUseCase
) : AndroidViewModel(application) {

    // location
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices
        .getFusedLocationProviderClient(application)

    private val _locationPermissionGranted = mutableStateOf(false)
    val locationPermissionGranted: State<Boolean> = _locationPermissionGranted

    // weather
    private val _weatherState = mutableStateOf(WeatherState())
    val weatherState: State<WeatherState> = _weatherState

    // scores
    private val _dailyScores =
        mutableStateOf<List<Pair<DailyForecast, BikeRidingScore>>>(emptyList())
    val dailyScores: State<List<Pair<DailyForecast, BikeRidingScore>>> = _dailyScores

    fun checkLocationPermission() {
        val context = getApplication<Application>()
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _locationPermissionGranted.value = hasPermission
        if (hasPermission) {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        fetchWeatherData(it.latitude, it.longitude)
                    }
                }
                .addOnFailureListener { exception ->
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        error = "Failed to get location: ${exception.message}"
                    )
                }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        _weatherState.value = _weatherState.value.copy(
            isLoading = true,
            error = null
        )
        viewModelScope.launch {
            getWeatherForecastUseCase(latitude, longitude)
                .onSuccess { response ->
                    val dailyForecasts = processForecastIntoDaily(response)
                    val score = dailyForecasts.map { forecast ->
                        forecast to calculateBikeRidingScoreUseCase(forecast)
                    }
                    _dailyScores.value = score
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        weatherData = response.copy(daily = dailyForecasts),
                        error = null
                    )
                }
                .onFailure { exception ->
                    _weatherState.value = _weatherState.value.copy(
                        isLoading = false,
                        error = "Failed to get weather data: ${exception.message}"
                    )
                }

        }
    }

    private fun processForecastIntoDaily(response: WeatherResponse): List<DailyForecast> {
        val allDailyForecasts = mutableListOf<DailyForecast>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Group forecast items by date string (yyyy-MM-dd)
        val dailyGroups = response.list.groupBy {
            dateFormat.format(it.date * 1000) // Convert to milliseconds
        }
        dailyGroups.values.forEach { singleDayForecast ->
            if (singleDayForecast.isNotEmpty()) {
                val firstForecast = singleDayForecast.first()
                val maxTemp = singleDayForecast.maxOf { it.main.tempMax }
                val minTemp = singleDayForecast.minOf { it.main.tempMin }
                val avgHumidity = singleDayForecast.map { it.main.humidity }.average().toInt()
                val avgWindSpeed = singleDayForecast.map { it.wind.speed }.average()
                val avgPrecipitation =
                    singleDayForecast.map { it.precipitationProbability }.average()

                // Get the most common weather condition for the day
                val mostCommonWeather = singleDayForecast
                    .flatMap { it.weather }
                    .groupBy { it.main }
                    .maxByOrNull { it.value.size }
                    ?.value?.first() ?: firstForecast.weather.first()

                val dailyForecast = DailyForecast(
                    date = firstForecast.date,
                    temperature = Temperature(
                        day = firstForecast.main.temp,
                        min = minTemp,
                        max = maxTemp,
                        night = firstForecast.main.temp
                    ),
                    weather = listOf(mostCommonWeather),
                    humidity = avgHumidity,
                    windSpeed = avgWindSpeed,
                    precipitationProbability = avgPrecipitation
                )
                allDailyForecasts.add(dailyForecast)
            }
        }
        return allDailyForecasts.take(6) // Return up to 6 days
    }

    fun getWeatherIconUrl(iconCode: String): String {
        return "${Config.WEATHER_ICON_BASE_URL}$iconCode@2x.png"
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp * 1000) // Convert to milliseconds
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        return dateFormat.format(date)
    }

}

data class WeatherState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val error: String? = null
)