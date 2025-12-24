package kz.narxoz.final_project.db



import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kz.narxoz.final_project.CategoryDao
import kz.narxoz.final_project.Converters
import kz.narxoz.final_project.DatabaseCallback

@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 2, // Увеличиваем версию, так как добавляем новые запросы
    exportSchema = false
)
@TypeConverters(Converters::class) // Если понадобятся конвертеры для сложных типов
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration() // Удаляем при миграции (для разработки)
                    .addCallback(DatabaseCallback(context)) // Добавляем callback для начальных данных
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}