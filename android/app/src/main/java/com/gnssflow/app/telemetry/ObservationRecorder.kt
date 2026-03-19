package com.gnssflow.app.telemetry

import com.gnssflow.app.db.ObservationDao
import com.gnssflow.app.db.ObservationEpochEntity
import com.gnssflow.app.network.RawObservationDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Records raw GNSS observation epochs arriving via telemetry WebSocket
 * into Room for a specific project. Start/stop recording per project.
 */
class ObservationRecorder(private val dao: ObservationDao) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val satListType = Types.newParameterizedType(
        List::class.java,
        Map::class.java, String::class.java, Any::class.java,
    )

    private val activeProjects = mutableSetOf<String>()

    fun startRecording(projectId: String) {
        activeProjects.add(projectId)
    }

    fun stopRecording(projectId: String) {
        activeProjects.remove(projectId)
    }

    fun isRecording(projectId: String): Boolean = projectId in activeProjects

    suspend fun onEpoch(projectId: String, raw: RawObservationDto) {
        if (projectId !in activeProjects) return

        val satsJson = moshi.adapter<Any>(Any::class.java).toJson(raw.satellites)

        dao.insert(
            ObservationEpochEntity(
                projectId = projectId,
                timestampUtc = raw.timestamp_utc,
                gpsWeek = raw.gps_week,
                gpsTowS = raw.gps_tow_s,
                receiverClockBiasS = raw.receiver_clock_bias_s,
                satellitesJson = satsJson,
            ),
        )
    }
}
