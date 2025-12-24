package kz.narxoz.final_project


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kz.narxoz.final_project.CategoryRepository
import kz.narxoz.final_project.TaskRepository

class EditTaskViewModelFactory(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTaskViewModel::class.java)) {
            return EditTaskViewModel(taskRepository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}