package com.gnssflow.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PointDao {
    @Query("SELECT * FROM points WHERE projectId = :projectId ORDER BY createdAtEpochMs DESC")
    fun observeByProject(projectId: String): Flow<List<PointEntity>>

    @Query("SELECT * FROM points WHERE id = :pointId LIMIT 1")
    fun observeById(pointId: String): Flow<PointEntity?>

    @Query("SELECT * FROM points WHERE id = :pointId LIMIT 1")
    suspend fun getById(pointId: String): PointEntity?

    @Query("SELECT * FROM points WHERE projectId = :projectId ORDER BY createdAtEpochMs DESC LIMIT 1")
    suspend fun getLatestByProject(projectId: String): PointEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PointEntity)
}

