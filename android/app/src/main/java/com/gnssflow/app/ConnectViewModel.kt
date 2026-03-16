package com.gnssflow.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.network.HealthResponse
import com.gnssflow.app.network.PiClient
import com.gnssflow.app.network.TelemetryDto
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

