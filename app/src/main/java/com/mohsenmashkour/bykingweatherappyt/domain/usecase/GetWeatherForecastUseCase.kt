package com.mohsenmashkour.bykingweatherappyt.domain.usecase

import com.mohsenmashkour.bykingweatherappyt.domain.model.WeatherResponse
import com.mohsenmashkour.bykingweatherappyt.domain.repository.WeatherRepository

class GetWeatherForecastUseCase(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double): Result<WeatherResponse> {
        return repository.getWeatherForecast(lat, lon)

    }
}