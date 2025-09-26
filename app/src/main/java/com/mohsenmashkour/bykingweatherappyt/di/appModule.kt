package com.mohsenmashkour.bykingweatherappyt.di

import com.mohsenmashkour.bykingweatherappyt.data.remote.Config
import com.mohsenmashkour.bykingweatherappyt.data.remote.WeatherApiService
import com.mohsenmashkour.bykingweatherappyt.data.repository.WeatherRepositoryImpl
import com.mohsenmashkour.bykingweatherappyt.domain.repository.WeatherRepository
import com.mohsenmashkour.bykingweatherappyt.domain.usecase.CalculateBikeRidingScoreUseCase
import com.mohsenmashkour.bykingweatherappyt.domain.usecase.GetWeatherForecastUseCase
import com.mohsenmashkour.bykingweatherappyt.presentation.viewModel.WeatherViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(WeatherApiService::class.java)
    }

    //Repository
    single<WeatherRepository> {
        WeatherRepositoryImpl(get())
    }
    // UseCase
    single {
        GetWeatherForecastUseCase(get())
    }

    single { CalculateBikeRidingScoreUseCase() }

    // viewModel under construction
    viewModel { WeatherViewModel(get(), get(), get()) }

}