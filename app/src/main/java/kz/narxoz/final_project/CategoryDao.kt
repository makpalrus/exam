package kz.narxoz.final_project



import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kz.narxoz.final_project.db.CategoryEntity

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>> // Изменено на Flow

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    suspend fun getTaskCountForCategory(categoryId: Long): Int

    // Для статистики (Участник 2, страница 5)
    @Query("""
        SELECT c.*, COUNT(t.id) as taskCount 
        FROM categories c 
        LEFT JOIN tasks t ON c.id = t.categoryId AND t.isCompleted = 0
        GROUP BY c.id 
        ORDER BY c.name ASC
    """)
    fun getCategoriesWithTaskCount(): Flow<List<CategoryWithTaskCount>> // Изменено на Flow

    // Для Spinner'а при создании задачи (Участник 1, страница 2)
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getCategoriesForSpinner(): List<CategoryEntity>
}

// Data class для статистики (количество задач по категориям)
data class CategoryWithTaskCount(
    @Embedded
    val category: CategoryEntity,
    @ColumnInfo(name = "taskCount")
    val taskCount: Int
)