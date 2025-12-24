package kz.narxoz.final_project


import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kz.narxoz.final_project.db.AppDatabase
import kz.narxoz.final_project.db.CategoryEntity

class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {
            // Вставляем категории по умолчанию при первом создании БД
            val database = AppDatabase.getDatabase(context)
            val defaultCategories = listOf(
                CategoryEntity(name = "Работа", colorHex = "#2196F3"),
                CategoryEntity(name = "Учеба", colorHex = "#4CAF50"),
                CategoryEntity(name = "Дом", colorHex = "#FF9800"),
                CategoryEntity(name = "Покупки", colorHex = "#E91E63"),
                CategoryEntity(name = "Здоровье", colorHex = "#9C27B0"),
                CategoryEntity(name = "Финансы", colorHex = "#FFEB3B"),
                CategoryEntity(name = "Разное", colorHex = "#9E9E9E")
            )

            defaultCategories.forEach { category ->
                database.categoryDao().insertCategory(category)
            }
        }
    }
}