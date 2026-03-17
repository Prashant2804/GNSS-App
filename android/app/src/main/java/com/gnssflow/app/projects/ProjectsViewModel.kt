package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.ProjectEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectsUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val error: String? = null,
)

class ProjectsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: ProjectsRepository =
        ProjectsRepository(
            AppDatabase.get(app).projectDao(),
            AppDatabase.get(app).pointDao(),
        )

    val uiState: StateFlow<ProjectsUiState> =
        repo.observeProjects()
            .map { ProjectsUiState(projects = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectsUiState())

    fun create(name: String) {
        viewModelScope.launch {
            runCatching { repo.createProject(name) }
        }
    }

    fun rename(id: String, newName: String) {
        viewModelScope.launch {
            runCatching { repo.renameProject(id, newName) }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { repo.deleteProject(id) }
        }
    }
}

