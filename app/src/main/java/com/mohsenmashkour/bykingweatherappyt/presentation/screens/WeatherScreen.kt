package com.mohsenmashkour.bykingweatherappyt.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohsenmashkour.bykingweatherappyt.domain.model.WeatherResponse
import com.mohsenmashkour.bykingweatherappyt.presentation.components.BikeRidingCard
import com.mohsenmashkour.bykingweatherappyt.presentation.viewModel.WeatherViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherScreen(
    modifier: Modifier,
    viewModel: WeatherViewModel = koinViewModel()
) {
    val weatherState by viewModel.weatherState
    val locationPermissionGranted by viewModel.locationPermissionGranted

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.checkLocationPermission()
        }
    }
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.checkLocationPermission()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Dark blue-gray
                        Color(0xFF1E293B), // Medium blue-gray
                        Color(0xFF334155)  // Light blue-gray
                    )
                )
            )
    ) {
        when {
            weatherState.isLoading && weatherState.weatherData == null -> {
                LoadingScreen()
            }

            weatherState.error != null -> {
                ErrorScreen(
                    error = weatherState.error!!,
                    onRetry = {
                        viewModel.checkLocationPermission()
                    }
                )
            }

            weatherState.weatherData != null -> {
                WeatherContent(
                    weatherData = weatherState.weatherData!!,
                    viewModel = viewModel
                )
            }
            else -> {
                WelcomeScreen()
            }
        }
    }
}
@Composable
fun WeatherContent(
    weatherData: WeatherResponse,
    viewModel: WeatherViewModel
) {
    val dailyScores by viewModel.dailyScores
    val bestDay = dailyScores.maxByOrNull { it.second.score }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection(weatherData, bestDay?.first, bestDay?.second, viewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(dailyScores) { (forecast, score) ->
                BikeRidingCard(
                    forecast = forecast,
                    score = score,
                    viewModel = viewModel,
                    isBest = bestDay?.first?.date == forecast.date
                )
            }
        }
    }
}
fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF22C55E) // Green - Excellent
        score >= 60 -> Color(0xFF4ADE80) // Light Green - Good
        score >= 40 -> Color(0xFFFACC15) // Yellow - Moderate
        score >= 20 -> Color(0xFFF87171) // Light Red - Poor
        else -> Color(0xFFDC2626) // Red - Dangerous
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}