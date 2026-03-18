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
    val latitudeDeg: Double?,
    val longitudeDeg: Double?,
    val altitudeMSL: Double?,
    val imu: ImuDto?,
    val horizontalAccuracyM: Double,
    val verticalAccuracyM: Double?,
    val ageOfDiffSec: Double?,
    val updateRateHz: Double?,
    val corrections: CorrectionsDto?,
)

data class ImuDto(
    val rollDeg: Double,
    val pitchDeg: Double,
    val yawDeg: Double,
)

data class CorrectionsDto(
    val connected: Boolean,
    val bytesPerSec: Double,
)

data class NtripConfigDto(
    val casterHost: String,
    val casterPort: Int = 2101,
    val mountPoint: String,
    val username: String? = null,
    val password: String? = null,
)

data class NtripConfigResponse(
    val config: NtripConfigDto?,
)

data class StoredResponse(
    val stored: Boolean,
)

data class ConnectedResponse(
    val connected: Boolean,
)

data class EnabledResponse(
    val enabled: Boolean,
)

interface PiApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @GET("ntrip/config")
    suspend fun getNtripConfig(): NtripConfigResponse

    @retrofit2.http.POST("ntrip/config")
    suspend fun setNtripConfig(@retrofit2.http.Body payload: NtripConfigDto): StoredResponse

    @retrofit2.http.POST("ntrip/connect")
    suspend fun ntripConnect(): ConnectedResponse

    @retrofit2.http.POST("ntrip/disconnect")
    suspend fun ntripDisconnect(): ConnectedResponse

    @GET("imu/enabled")
    suspend fun getImuEnabled(): EnabledResponse

    @retrofit2.http.POST("imu/enabled")
    suspend fun setImuEnabled(@retrofit2.http.Body payload: EnabledResponse): EnabledResponse
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

