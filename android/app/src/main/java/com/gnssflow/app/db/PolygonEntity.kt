package com.gnssflow.app.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "polygons",
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
data class PolygonEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val vertexPointIdsCsv: String,
    val createdAtEpochMs: Long,
)

