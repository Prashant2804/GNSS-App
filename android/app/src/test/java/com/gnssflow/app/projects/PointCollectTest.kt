package com.gnssflow.app.projects

import com.gnssflow.app.db.PointDao
import com.gnssflow.app.db.PointEntity
import com.gnssflow.app.network.CorrectionsDto
import com.gnssflow.app.network.TelemetryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private class CapturingPointDao : PointDao {
    val inserted = mutableListOf<PointEntity>()

    override fun observeByProject(projectId: String): Flow<List<PointEntity>> = MutableStateFlow(emptyList())

    override fun observeById(pointId: String): Flow<PointEntity?> = MutableStateFlow(null)

    override suspend fun getLatestByProject(projectId: String): PointEntity? = inserted.lastOrNull()

    override suspend fun insert(entity: PointEntity) {
        inserted.add(entity)
    }
}

class PointCollectTest {
    @Test
    fun collectPoint_usesTelemetryPosition() = runBlocking {
        val pointDao = CapturingPointDao()
        val repo = ProjectsRepository(
            dao = object : com.gnssflow.app.db.ProjectDao {
                override fun observeAll() = MutableStateFlow(emptyList<com.gnssflow.app.db.ProjectEntity>())
                override fun observeById(id: String) = MutableStateFlow<com.gnssflow.app.db.ProjectEntity?>(null)
                override suspend fun getById(id: String) = null
                override suspend fun insert(entity: com.gnssflow.app.db.ProjectEntity) = Unit
                override suspend fun update(entity: com.gnssflow.app.db.ProjectEntity) = 0
                override suspend fun deleteById(id: String) = 0
            },
            pointDao = pointDao,
        )

        val telemetry = TelemetryDto(
            fixQuality = "fix",
            satellites = 10,
            latitudeDeg = 12.0,
            longitudeDeg = 77.0,
            altitudeMSL = 900.0,
            imu = null,
            horizontalAccuracyM = 0.5,
            verticalAccuracyM = null,
            ageOfDiffSec = null,
            updateRateHz = null,
            corrections = CorrectionsDto(connected = false, bytesPerSec = 0.0),
        )

        repo.collectPoint(projectId = "p1", code = "CP", telemetry = telemetry, nowEpochMs = 1234L)

        assertEquals(1, pointDao.inserted.size)
        val p = pointDao.inserted.single()
        assertEquals("p1", p.projectId)
        assertEquals("CP", p.code)
        assertEquals(12.0, p.latitudeDeg, 0.000001)
        assertEquals(77.0, p.longitudeDeg, 0.000001)
        assertEquals(900.0, p.altitudeMSL, 0.000001)
        assertEquals(null, p.imuRollDeg)
        assertEquals(null, p.imuPitchDeg)
        assertEquals(null, p.imuYawDeg)
        assertEquals(1234L, p.createdAtEpochMs)
    }
}

