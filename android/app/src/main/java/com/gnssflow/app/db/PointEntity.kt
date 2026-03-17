package com.gnssflow.app.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "points",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["projectId", "createdAtEpochMs"]),
    ],
)
data class PointEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val code: String,
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val altitudeMSL: Double,
    val horizontalAccuracyM: Double?,
    val createdAtEpochMs: Long,
)

