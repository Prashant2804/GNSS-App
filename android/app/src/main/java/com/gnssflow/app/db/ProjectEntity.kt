package com.gnssflow.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val autoCollectEnabled: Boolean,
    val autoCollectMinSeconds: Int,
    val autoCollectMinDistanceM: Double,
)

