package com.mohsenmashkour.bykingweatherappyt.data.repository

import com.mohsenmashkour.bykingweatherappyt.data.remote.Config
import com.mohsenmashkour.bykingweatherappyt.data.remote.WeatherApiService
import com.mohsenmashkour.bykingweatherappyt.domain.model.WeatherResponse
import com.mohsenmashkour.bykingweatherappyt.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService
) : WeatherRepository {
    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double
    ): Result<WeatherResponse> {
        return try {
            val response = apiService.getWeatherForecast(lat = lat, lon = lon, apiKey = Config.OPENWEATHER_API_KEY)
            Result.success(response)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}