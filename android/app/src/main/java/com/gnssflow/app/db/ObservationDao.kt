package com.gnssflow.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ObservationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ObservationEpochEntity)

    @Query("SELECT * FROM observation_epochs WHERE projectId = :projectId ORDER BY timestampUtc ASC")
    suspend fun getByProject(projectId: String): List<ObservationEpochEntity>

    @Query("SELECT COUNT(*) FROM observation_epochs WHERE projectId = :projectId")
    suspend fun countByProject(projectId: String): Int

    @Query("DELETE FROM observation_epochs WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: String)
}
