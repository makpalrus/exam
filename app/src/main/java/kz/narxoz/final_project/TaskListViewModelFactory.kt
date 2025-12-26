package kz.narxoz.final_project



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kz.narxoz.final_project.TaskRepository

class TaskListViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            return TaskListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}