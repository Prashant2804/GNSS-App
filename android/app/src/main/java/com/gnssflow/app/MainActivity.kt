package com.gnssflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConnectScreen()
                }
            }
        }
    }
}

@Composable
fun ConnectScreen() {
    val telemetry = TelemetryMock.sample()
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Connect")
        Text(text = "Pi: Not connected")
        Text(text = "Fix: ${telemetry.fixQuality}")
        Text(text = "Sats: ${telemetry.satellites}")
        Text(text = "H Acc: ${telemetry.horizontalAccuracyM} m")
        Button(onClick = { }) {
            Text(text = "Connect")
        }
    }
}
