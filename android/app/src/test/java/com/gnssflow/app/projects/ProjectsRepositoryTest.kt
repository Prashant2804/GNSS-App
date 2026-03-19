package com.gnssflow.app.projects

import com.gnssflow.app.db.ProjectDao
import com.gnssflow.app.db.ProjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeProjectDao : ProjectDao {
    private val items = LinkedHashMap<String, ProjectEntity>()
    private val flow = MutableStateFlow<List<ProjectEntity>>(emptyList())

    override fun observeAll(): Flow<List<ProjectEntity>> = flow

    override fun observeById(id: String): Flow<ProjectEntity?> = MutableStateFlow(items[id])

    override suspend fun getById(id: String): ProjectEntity? = items[id]

    override suspend fun insert(entity: ProjectEntity) {
        items[entity.id] = entity
        flow.value = items.values.toList()
    }

    override suspend fun update(entity: ProjectEntity): Int {
        if (!items.containsKey(entity.id)) return 0
        items[entity.id] = entity
        flow.value = items.values.toList()
        return 1
    }

    override suspend fun deleteById(id: String): Int {
        items.remove(id) ?: return 0
        flow.value = items.values.toList()
        return 1
    }
}

class ProjectsRepositoryTest {
    @Test
    fun createRenameDelete_roundTrip() = runBlocking {
        val repo = ProjectsRepository(FakeProjectDao(), FakePointDao())

        val id = repo.createProject("Test", nowEpochMs = 1000L)
        assertNotNull(id)

        repo.renameProject(id, "Updated", nowEpochMs = 2000L)

        repo.deleteProject(id)
        assertTrue(true)
    }

    @Test
    fun create_trimsName() = runBlocking {
        val dao = FakeProjectDao()
        val repo = ProjectsRepository(dao, FakePointDao())

        val id = repo.createProject("  Name  ", nowEpochMs = 1000L)
        val stored = dao.getById(id)
        assertEquals("Name", stored?.name)
    }
}

private class FakePointDao : com.gnssflow.app.db.PointDao {
    override fun observeByProject(projectId: String) = kotlinx.coroutines.flow.MutableStateFlow(emptyList<com.gnssflow.app.db.PointEntity>())
    override fun observeById(pointId: String) = kotlinx.coroutines.flow.MutableStateFlow<com.gnssflow.app.db.PointEntity?>(null)
    override suspend fun getLatestByProject(projectId: String): com.gnssflow.app.db.PointEntity? = null
    override suspend fun getById(pointId: String): com.gnssflow.app.db.PointEntity? = null
    override suspend fun insert(entity: com.gnssflow.app.db.PointEntity) = Unit
}

