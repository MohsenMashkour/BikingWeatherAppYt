 package com.mohsenmashkour.bykingweatherappyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mohsenmashkour.bykingweatherappyt.presentation.screens.WeatherScreen
import com.mohsenmashkour.bykingweatherappyt.ui.theme.BykingWeatherAppYtTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BykingWeatherAppYtTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   WeatherScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

