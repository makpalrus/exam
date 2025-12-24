package kz.narxoz.final_project



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kz.narxoz.final_project.db.TaskEntity

// Состояние UI для списка задач
sealed class TaskListUiState {
    object Loading : TaskListUiState()
    data class Success(val tasks: List<TaskEntity>) : TaskListUiState()
    data class Error(val message: String) : TaskListUiState()
}

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Loading)
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    val currentFilter: StateFlow<FilterType> = _currentFilter.asStateFlow()

    init {
        loadTasks(FilterType.ALL)
    }

    fun loadTasks(filter: FilterType) {
        _currentFilter.value = filter
        viewModelScope.launch {
            try {
                val flow = when (filter) {
                    FilterType.ALL -> repository.getAllTasks(sortByDate = true)
                    FilterType.TODAY -> repository.getTodayTasks()
                    FilterType.IMPORTANT -> repository.getImportantTasks()
                }
                flow.collect { tasks ->
                    _uiState.value = TaskListUiState.Success(tasks)
                }
            } catch (e: Exception) {
                _uiState.value = TaskListUiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updatedTask)
            // Обновление через Flow произойдет автоматически
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}

enum class FilterType {
    ALL, TODAY, IMPORTANT
}