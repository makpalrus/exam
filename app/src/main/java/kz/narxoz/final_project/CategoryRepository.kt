package kz.narxoz.final_project




import kotlinx.coroutines.flow.Flow
import kz.narxoz.final_project.db.CategoryEntity


class CategoryRepository(
    private val categoryDao: CategoryDao
) { // Убираем @Inject конструктор, если не используете DI

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCategoriesWithTaskCount(): Flow<List<CategoryWithTaskCount>> =
        categoryDao.getCategoriesWithTaskCount()

    suspend fun getCategoriesForSpinner(): List<CategoryEntity> =
        categoryDao.getCategoriesForSpinner()

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    suspend fun getCategoryById(id: Long): CategoryEntity? =
        categoryDao.getCategoryById(id)

    suspend fun getTaskCountForCategory(categoryId: Long): Int =
        categoryDao.getTaskCountForCategory(categoryId)
}