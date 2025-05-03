package com.example.musify.data.repositories.categoriesrepository

import com.example.musify.domain.Category
import com.example.musify.domain.PlaylistsForCategory

interface CategoriesRepository {
    suspend fun fetchAvailableCategories(country: String): List<Category>
    suspend fun fetchPlaylistsForCategory(
        categoryId: String,
        categoryName: String,
        country: String,
        limit: Int = 20,
        offset: Int = 0
    ): PlaylistsForCategory
}