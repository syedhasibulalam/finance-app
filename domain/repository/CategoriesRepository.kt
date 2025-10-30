package com.achievemeaalk.freedjf.domain.repository

import com.achievemeaalk.freedjf.data.db.categories.CategoryDao
import com.achievemeaalk.freedjf.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoriesRepository @Inject constructor(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategory(id: Int): Flow<Category?> = categoryDao.getCategory(id)

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
} 