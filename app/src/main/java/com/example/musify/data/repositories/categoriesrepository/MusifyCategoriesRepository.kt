package com.example.musify.data.repositories.categoriesrepository
//
//import com.example.musify.data.remote.musicservice.SpotifyService
//import com.example.musify.data.remote.response.BrowseCategoriesResponse
//import com.example.musify.data.remote.response.PlaylistsForSpecificCategoryResponse
//import com.example.musify.data.remote.response.toPlaylistSearchResultList
//import com.example.musify.data.repositories.tokenrepository.TokenRepository
//import com.example.musify.data.repositories.tokenrepository.runCatchingWithToken
//import com.example.musify.domain.Category
//import com.example.musify.domain.PlaylistsForCategory
//import javax.inject.Inject
//
//class MusifyCategoriesRepository @Inject constructor(
//    private val tokenRepository: TokenRepository,
//    private val spotifyService: SpotifyService
//) : CategoriesRepository {
//
//    override suspend fun fetchAvailableCategories(country: String): List<Category> {
//        return tokenRepository
//            .runCatchingWithToken { t ->
//                spotifyService.getCategories(country = country, token = t)
//            }
//            .map(BrowseCategoriesResponse::toDomainCategoryList)
//            .getOrElse { emptyList() }
//    }
//
//    override suspend fun fetchPlaylistsForCategory(
//        categoryId: String,
//        categoryName: String,
//        country: String,
//        limit: Int,
//        offset: Int
//    ): PlaylistsForCategory {
//        val playlists = tokenRepository
//            .runCatchingWithToken { t ->
//                spotifyService.getPlaylistsForCategory(
//                    categoryId  = categoryId,
//                    country     = country,
//                    limit       = limit,
//                    offset      = offset,
//                    token       = t
//                )
//            }
//            .map(PlaylistsForSpecificCategoryResponse::toPlaylistSearchResultList)
//            .getOrElse { emptyList() }
//
//        return PlaylistsForCategory(
//            categoryId         = categoryId,
//            nameOfCategory     = categoryName,
//            associatedPlaylists = playlists
//        )
//    }
//}