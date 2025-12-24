package kz.narxoz.final_project





import kotlinx.coroutines.flow.Flow
import kz.narxoz.final_project.db.TaskDao
import kz.narxoz.final_project.db.TaskEntity
import java.util.Calendar

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(sortByDate: Boolean): Flow<List<TaskEntity>> = taskDao.getAllTasks(sortByDate)

    fun getTodayTasks(): Flow<List<TaskEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        return taskDao.getTodayTasks(startOfDay)
    }

    fun getImportantTasks(): Flow<List<TaskEntity>> = taskDao.getImportantTasks()

    suspend fun insertTask(task: TaskEntity): Long {
        val id = taskDao.insertTask(task)
        // TODO: Здесь позже будет логика установки уведомления (AlarmManager)
        // если task.reminderTime != null
        return id
    }

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
}