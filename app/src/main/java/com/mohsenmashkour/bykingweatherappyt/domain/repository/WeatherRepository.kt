package com.mohsenmashkour.bykingweatherappyt.domain.repository

import com.mohsenmashkour.bykingweatherappyt.domain.model.WeatherResponse

interface WeatherRepository {
    suspend fun getWeatherForecast(lat: Double, lon: Double): Result<WeatherResponse>
}