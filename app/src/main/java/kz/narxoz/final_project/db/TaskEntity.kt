package kz.narxoz.final_project.db



import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dateTime: Long? = null,
    val isCompleted: Boolean = false,
    val isImportant: Boolean = false,
    val categoryId: Long? = null,
    val reminderTime: Long? = null
) {

    fun isDueToday(): Boolean {
        if (dateTime == null) return false
        val taskCalendar = Calendar.getInstance().apply { timeInMillis = dateTime }
        val today = Calendar.getInstance()
        return taskCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                taskCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }
}