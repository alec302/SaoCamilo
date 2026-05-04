package com.pisc.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class IpLocationResponse(
    val status: String,
    val lat: Double? = null,
    val lon: Double? = null
)

@Serializable
data class OpenMeteoCurrent(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val relativeHumidity: Int
)

@Serializable
data class OpenMeteoResponse(
    val current: OpenMeteoCurrent? = null
)

object WeatherApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchCurrentClimateString(): String? {
        return try {
            // 1. Get approximate location via IP
            val locationUrl = "http://ip-api.com/json/"
            val locationResponse: IpLocationResponse = client.get(locationUrl).body()
            
            if (locationResponse.status != "success" || locationResponse.lat == null || locationResponse.lon == null) {
                return null
            }
            
            // 2. Get current weather from Open-Meteo
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=${locationResponse.lat}&longitude=${locationResponse.lon}&current=temperature_2m,relative_humidity_2m"
            val weatherResponse: OpenMeteoResponse = client.get(weatherUrl).body()
            
            weatherResponse.current?.let { current ->
                "${current.temperature}°C, ${current.relativeHumidity}% umidade"
            }
        } catch (e: Exception) {
            println("Erro ao buscar clima: ${e.message}")
            null
        }
    }
}
