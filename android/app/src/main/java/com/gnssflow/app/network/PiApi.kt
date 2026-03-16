package com.gnssflow.app.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

data class HealthResponse(
    val status: String,
)

data class TelemetryDto(
    val fixQuality: String,
    val satellites: Int,
    val horizontalAccuracyM: Double,
    val verticalAccuracyM: Double?,
    val ageOfDiffSec: Double?,
    val updateRateHz: Double?,
)

interface PiApi {
    @GET("health")
    suspend fun health(): HealthResponse
}

class PiClient(
    private val baseUrl: String,
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: PiApi = retrofit.create(PiApi::class.java)

    fun connectTelemetryWebSocket(listener: WebSocketListener): WebSocket {
        val request = Request.Builder()
            .url(baseUrl.removeSuffix("/") + "/ws/telemetry")
            .build()
        return okHttpClient.newWebSocket(request, listener)
    }

    fun telemetryJsonAdapter() = moshi.adapter(TelemetryDto::class.java)
}

