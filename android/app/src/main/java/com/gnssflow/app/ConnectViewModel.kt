package com.gnssflow.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.network.NtripConfigDto
import com.gnssflow.app.network.HealthResponse
import com.gnssflow.app.network.PiClient
import com.gnssflow.app.network.TelemetryDto
import com.gnssflow.app.telemetry.TelemetryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

data class ConnectUiState(
    val baseUrl: String = "http://10.0.2.2:8000",
    val isCheckingHealth: Boolean = false,
    val health: HealthResponse? = null,
    val healthError: String? = null,
    val telemetry: TelemetryDto? = null,
    val isTelemetryConnected: Boolean = false,
    val ntripCasterHost: String = "",
    val ntripCasterPort: Int = 2101,
    val ntripMountPoint: String = "",
    val ntripUsername: String = "",
    val ntripPassword: String = "",
    val ntripStatus: String? = null,
)

class ConnectViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState: StateFlow<ConnectUiState> = _uiState.asStateFlow()

    private var webSocket: WebSocket? = null

    fun onBaseUrlChanged(newUrl: String) {
        _uiState.value = _uiState.value.copy(baseUrl = newUrl)
    }

    fun checkHealthAndConnect() {
        val baseUrl = _uiState.value.baseUrl
        val client = PiClient(baseUrl)

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                isCheckingHealth = true,
                healthError = null,
            )
            try {
                val health = client.api.health()
                _uiState.value = _uiState.value.copy(
                    isCheckingHealth = false,
                    health = health,
                )
                startTelemetry(client)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isCheckingHealth = false,
                    healthError = t.message ?: t::class.simpleName.orEmpty(),
                )
            }
        }
    }

    fun onNtripCasterHostChanged(value: String) {
        _uiState.value = _uiState.value.copy(ntripCasterHost = value)
    }

    fun onNtripCasterPortChanged(value: String) {
        val port = value.toIntOrNull() ?: return
        _uiState.value = _uiState.value.copy(ntripCasterPort = port)
    }

    fun onNtripMountPointChanged(value: String) {
        _uiState.value = _uiState.value.copy(ntripMountPoint = value)
    }

    fun onNtripUsernameChanged(value: String) {
        _uiState.value = _uiState.value.copy(ntripUsername = value)
    }

    fun onNtripPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(ntripPassword = value)
    }

    fun saveNtripConfig() {
        val baseUrl = _uiState.value.baseUrl
        val client = PiClient(baseUrl)
        val state = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                client.api.setNtripConfig(
                    NtripConfigDto(
                        casterHost = state.ntripCasterHost.trim(),
                        casterPort = state.ntripCasterPort,
                        mountPoint = state.ntripMountPoint.trim(),
                        username = state.ntripUsername.takeIf { it.isNotBlank() },
                        password = state.ntripPassword.takeIf { it.isNotBlank() },
                    ),
                )
                _uiState.value = _uiState.value.copy(ntripStatus = "NTRIP config stored")
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    ntripStatus = "NTRIP error: ${t.message ?: t::class.simpleName.orEmpty()}",
                )
            }
        }
    }

    fun toggleNtripConnection() {
        val baseUrl = _uiState.value.baseUrl
        val client = PiClient(baseUrl)
        val connected = _uiState.value.telemetry?.corrections?.connected == true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (connected) {
                    client.api.ntripDisconnect()
                    _uiState.value = _uiState.value.copy(ntripStatus = "NTRIP disconnected")
                } else {
                    client.api.ntripConnect()
                    _uiState.value = _uiState.value.copy(ntripStatus = "NTRIP connected")
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    ntripStatus = "NTRIP error: ${t.message ?: t::class.simpleName.orEmpty()}",
                )
            }
        }
    }

    private fun startTelemetry(client: PiClient) {
        val adapter = client.telemetryJsonAdapter()
        webSocket?.close(1000, null)

        webSocket = client.connectTelemetryWebSocket(
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _uiState.value = _uiState.value.copy(isTelemetryConnected = true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val dto = adapter.fromJson(text) ?: return
                    _uiState.value = _uiState.value.copy(telemetry = dto)
                    TelemetryStore.update(dto)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _uiState.value = _uiState.value.copy(isTelemetryConnected = false)
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?,
                ) {
                    _uiState.value = _uiState.value.copy(
                        isTelemetryConnected = false,
                        healthError = t.message ?: t::class.simpleName.orEmpty(),
                    )
                }
            },
        )
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, null)
        webSocket = null
    }
}

