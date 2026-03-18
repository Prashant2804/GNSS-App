package com.gnssflow.app.projects

import com.gnssflow.app.db.ProjectDao
import com.gnssflow.app.db.ProjectEntity
import com.gnssflow.app.db.PointDao
import com.gnssflow.app.db.PointEntity
import com.gnssflow.app.network.TelemetryDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProjectsRepository(
    private val dao: ProjectDao,
    private val pointDao: PointDao,
) {
    fun observeProjects(): Flow<List<ProjectEntity>> = dao.observeAll()

    fun observeProject(projectId: String): Flow<ProjectEntity?> = dao.observeById(projectId)

    suspend fun getProject(projectId: String): ProjectEntity? = dao.getById(projectId)

    fun observePoints(projectId: String): Flow<List<PointEntity>> = pointDao.observeByProject(projectId)

    suspend fun createProject(name: String, nowEpochMs: Long = System.currentTimeMillis()): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Project name required" }
        val id = UUID.randomUUID().toString()
        dao.insert(
            ProjectEntity(
                id = id,
                name = trimmed,
                createdAtEpochMs = nowEpochMs,
                updatedAtEpochMs = nowEpochMs,
                autoCollectEnabled = false,
                autoCollectMinSeconds = 5,
                autoCollectMinDistanceM = 1.0,
            ),
        )
        return id
    }

    suspend fun renameProject(id: String, newName: String, nowEpochMs: Long = System.currentTimeMillis()) {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "Project name required" }
        val existing = dao.getById(id) ?: return
        dao.update(existing.copy(name = trimmed, updatedAtEpochMs = nowEpochMs))
    }

    suspend fun deleteProject(id: String) {
        dao.deleteById(id)
    }

    suspend fun updateAutoCollect(
        projectId: String,
        enabled: Boolean,
        minSeconds: Int,
        minDistanceM: Double,
        nowEpochMs: Long = System.currentTimeMillis(),
    ) {
        val existing = dao.getById(projectId) ?: return
        dao.update(
            existing.copy(
                autoCollectEnabled = enabled,
                autoCollectMinSeconds = minSeconds.coerceAtLeast(1),
                autoCollectMinDistanceM = minDistanceM.coerceAtLeast(0.0),
                updatedAtEpochMs = nowEpochMs,
            ),
        )
    }

    suspend fun collectPoint(
        projectId: String,
        code: String,
        telemetry: TelemetryDto,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): String {
        val lat = telemetry.latitudeDeg ?: error("No latitude yet")
        val lon = telemetry.longitudeDeg ?: error("No longitude yet")
        val alt = telemetry.altitudeMSL ?: error("No altitude yet")

        val trimmedCode = code.trim().ifEmpty { "PT" }
        val id = UUID.randomUUID().toString()
        val imu = telemetry.imu
        pointDao.insert(
            PointEntity(
                id = id,
                projectId = projectId,
                code = trimmedCode,
                latitudeDeg = lat,
                longitudeDeg = lon,
                altitudeMSL = alt,
                imuRollDeg = imu?.rollDeg,
                imuPitchDeg = imu?.pitchDeg,
                imuYawDeg = imu?.yawDeg,
                horizontalAccuracyM = telemetry.horizontalAccuracyM,
                createdAtEpochMs = nowEpochMs,
            ),
        )
        return id
    }

    suspend fun latestPoint(projectId: String): PointEntity? = pointDao.getLatestByProject(projectId)
}

