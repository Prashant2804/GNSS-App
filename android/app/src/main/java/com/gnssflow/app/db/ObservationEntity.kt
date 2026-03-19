package com.gnssflow.app.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "observation_epochs",
    indices = [Index(value = ["projectId", "timestampUtc"])],
)
data class ObservationEpochEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: String,
    val timestampUtc: String,
    val gpsWeek: Int,
    val gpsTowS: Double,
    val receiverClockBiasS: Double,
    val satellitesJson: String,
)
