package kz.narxoz.final_project.db




import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY " +
            "CASE WHEN :sortByDate THEN dateTime END ASC, " +
            "isCompleted ASC, " +
            "CASE WHEN :sortByDate THEN id END DESC")
    fun getAllTasks(sortByDate: Boolean = true): Flow<List<TaskEntity>> // Изменено на Flow

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dateTime IS NOT NULL AND dateTime >= :startOfDay ORDER BY dateTime ASC")
    fun getTodayTasks(startOfDay: Long): Flow<List<TaskEntity>> // Изменено на Flow

    @Query("SELECT * FROM tasks WHERE isImportant = 1 AND isCompleted = 0 ORDER BY dateTime ASC")
    fun getImportantTasks(): Flow<List<TaskEntity>> // Изменено на Flow

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    // Запрос для календаря (Участник 2): получить задачи на конкретный день
    @Query("SELECT * FROM tasks WHERE dateTime BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTasksForDay(startOfDay: Long, endOfDay: Long): List<TaskEntity>
}