package com.gnssflow.app.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lines",
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
data class LineEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val aPointId: String,
    val bPointId: String,
    val createdAtEpochMs: Long,
)

