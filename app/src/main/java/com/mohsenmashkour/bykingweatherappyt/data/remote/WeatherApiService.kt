package com.mohsenmashkour.bykingweatherappyt.data.remote

import com.mohsenmashkour.bykingweatherappyt.domain.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat")lat: Double,
        @Query("lon")lon: Double,
        @Query("appid")apiKey: String,
        @Query("units")units: String = "metric"
    ): WeatherResponse
}