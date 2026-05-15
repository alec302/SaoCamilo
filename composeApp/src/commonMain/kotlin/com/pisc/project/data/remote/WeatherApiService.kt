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
data class FreeIpResponse(
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class GeoJsResponse(
    val latitude: String? = null,
    val longitude: String? = null
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
            var lat: Double? = null
            var lon: Double? = null

            // 1. Get approximate location via IP over HTTPS using freeipapi.com
            try {
                val freeIpResp: FreeIpResponse = client.get("https://freeipapi.com/api/json").body()
                lat = freeIpResp.latitude
                lon = freeIpResp.longitude
            } catch (e: Exception) {
                println("Fallback: FreeIpApi falhou (${e.message}), tentando GeoJS...")
            }

            // Fallback para get.geojs.io caso o primeiro falhe
            if (lat == null || lon == null || (lat == 0.0 && lon == 0.0)) {
                try {
                    val geoJsResp: GeoJsResponse = client.get("https://get.geojs.io/v1/ip/geo.json").body()
                    lat = geoJsResp.latitude?.toDoubleOrNull()
                    lon = geoJsResp.longitude?.toDoubleOrNull()
                } catch (e: Exception) {
                    println("Erro ao buscar localização via GeoJS: ${e.message}")
                }
            }

            if (lat == null || lon == null) {
                return null
            }
            
            // 2. Get current weather from Open-Meteo over HTTPS
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m"
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
