package kz.narxoz.final_project



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kz.narxoz.final_project.CategoryRepository
import kz.narxoz.final_project.TaskRepository
import kz.narxoz.final_project.db.CategoryEntity
import kz.narxoz.final_project.db.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class EditTaskViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _task = MutableStateFlow<TaskEntity?>(null)
    val task: StateFlow<TaskEntity?> = _task

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            if (taskId != -1L) {
                _task.value = taskRepository.getTaskById(taskId)
            } else {
                // Создаем новую задачу с пустым заголовком
                _task.value = TaskEntity(title = "")
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = categoryRepository.getCategoriesForSpinner()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки категорий: ${e.message}"
            }
        }
    }

    fun updateTitle(title: String) {
        val currentTask = _task.value ?: TaskEntity(title = title)
        _task.value = currentTask.copy(title = title)
    }

    fun updateDescription(description: String) {
        val currentTask = _task.value ?: TaskEntity(title = "")
        _task.value = currentTask.copy(description = if (description.isBlank()) null else description)
    }

    fun updateDateTime(dateTime: Long?) {
        val currentTask = _task.value ?: TaskEntity(title = "")
        _task.value = currentTask.copy(dateTime = dateTime)
    }

    fun updateCategory(categoryId: Long?) {
        val currentTask = _task.value ?: TaskEntity(title = "")
        _task.value = currentTask.copy(categoryId = categoryId)
    }

    fun updateImportant(isImportant: Boolean) {
        val currentTask = _task.value ?: TaskEntity(title = "")
        _task.value = currentTask.copy(isImportant = isImportant)
    }

    fun updateReminder(reminderTime: Long?) {
        val currentTask = _task.value ?: TaskEntity(title = "")
        _task.value = currentTask.copy(reminderTime = reminderTime)
    }

    fun saveTask() {
        viewModelScope.launch {
            val currentTask = _task.value ?: return@launch

            if (currentTask.title.isBlank()) {
                _errorMessage.value = "Введите название задачи"
                return@launch
            }

            try {
                if (currentTask.id == 0L) {
                    taskRepository.insertTask(currentTask)
                } else {
                    taskRepository.updateTask(currentTask)
                }
                _saved.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сохранения: ${e.message}"
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            val currentTask = _task.value ?: return@launch

            try {
                taskRepository.deleteTask(currentTask)
                _saved.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка удаления: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}