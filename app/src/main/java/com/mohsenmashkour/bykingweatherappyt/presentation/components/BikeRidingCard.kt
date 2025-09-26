package com.mohsenmashkour.bykingweatherappyt.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mohsenmashkour.bykingweatherappyt.domain.model.BikeRidingScore
import com.mohsenmashkour.bykingweatherappyt.domain.model.DailyForecast
import com.mohsenmashkour.bykingweatherappyt.presentation.screens.getScoreColor
import com.mohsenmashkour.bykingweatherappyt.presentation.viewModel.WeatherViewModel

@Composable
fun BikeRidingCard(
    forecast: DailyForecast,
    score: BikeRidingScore,
    viewModel: WeatherViewModel,
    isBest: Boolean
) {
    val scoreColor = getScoreColor(score.score)
    val backgroundColor = if (isBest) {
        Color(0xFF064E3B).copy(alpha = 0.3f) // Dark green background for best day
    } else {
        Color(0xFF1E293B).copy(alpha = 0.8f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isBest) Modifier.border(
                    3.dp,
                    Color(0xFF22C55E),
                    RoundedCornerShape(20.dp)
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = viewModel.formatDate(forecast.date),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = score.recommendation.name,
                        color = scoreColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                // Circular Progress Bar
                CircularProgressBar(
                    score = score.score,
                    color = scoreColor,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = score.overallRating,
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = viewModel.getWeatherIconUrl(forecast.weather.firstOrNull()?.icon ?: ""),
                    contentDescription = forecast.weather.firstOrNull()?.description,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${forecast.temperature.max.toInt()}° / ${forecast.temperature.min.toInt()}°",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = forecast.weather.firstOrNull()?.description?.capitalize() ?: "",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Show factors with better layout
            val maxFactorHeight = remember(score.factors) {
                // Calculate max height needed for factors
                score.factors.maxOfOrNull { factor ->
                    // More generous height calculation to ensure all content is visible
                    val baseHeight = 100.dp
                    val extraHeight = when {
                        factor.description.length > 30 -> 30.dp
                        factor.description.length > 20 -> 20.dp
                        else -> 0.dp
                    }
                    baseHeight + extraHeight
                } ?: 120.dp
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(score.factors) { factor ->
                    FactorItem(
                        factor = factor,
                        height = maxFactorHeight
                    )
                }
            }
        }
    }
}