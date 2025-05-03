package com.example.musify.data.repositories.homefeedrepository

import android.util.Log
import com.example.musify.data.remote.musicservice.SpotifyService
import com.example.musify.data.remote.musicservice.SupportedSpotifyGenres
import com.example.musify.data.remote.response.PlaylistsForSpecificCategoryResponse
import com.example.musify.data.remote.response.toAlbumSearchResultList
import com.example.musify.data.remote.response.toFeaturedPlaylists
import com.example.musify.data.remote.response.toPlaylistSearchResultList
import com.example.musify.data.remote.response.toSearchResults
import com.example.musify.data.repositories.tokenrepository.TokenRepository
import com.example.musify.data.repositories.tokenrepository.runCatchingWithToken
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.FeaturedPlaylists
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.PlaylistsForCategory
import com.example.musify.domain.SearchResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import retrofit2.HttpException
import javax.inject.Inject

class MusifyHomeFeedRepository @Inject constructor(
    private val spotifyService: SpotifyService,
    private val tokenRepository: TokenRepository
) : HomeFeedRepository {

    override suspend fun fetchNewlyReleasedAlbums(
        countryCode: String
    ): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType> {
        return try {
            tokenRepository.runCatchingWithToken { token ->
                spotifyService
                    .getNewReleases(token = token, market = countryCode)
                    .toAlbumSearchResultList()
            }
        } catch (e: Exception) {
            Log.e("MusifyHomeFeedRepository", "Error fetching newly released albums", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR, null)
        }
    }

//    override suspend fun fetchFeaturedPlaylistsForCurrentTimeStamp(
//        timestampMillis: Long,
//        countryCode: String,
//        languageCode: ISO6391LanguageCode
//    ): FetchedResource<FeaturedPlaylists, MusifyErrorType> {
//        val timestamp = ISODateTimeString.from(timestampMillis)
//        val locale = "${languageCode.value}_$countryCode"
//        return try {
//            tokenRepository.runCatchingWithToken { token ->
//                spotifyService.getFeaturedPlaylists(
//                    token = token,
//                    market = countryCode,
//                    locale = locale,
//                    timestamp = timestamp
//                ).toFeaturedPlaylists()
//            }
//        } catch (e: Exception) {
//            Log.e("MusifyHomeFeedRepository", "Error fetching featured playlists", e)
//            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR, null)
//        }
//    }
//
//    override suspend fun fetchPlaylistsBasedOnCategoriesAvailableForCountry(
//        countryCode: String,
//        languageCode: ISO6391LanguageCode,
//    ): FetchedResource<List<PlaylistsForCategory>, MusifyErrorType> {
//        return try {
//            tokenRepository.runCatchingWithToken { token ->
//                val locale = "${languageCode.value}_$countryCode"
//                val categories = spotifyService
//                    .getBrowseCategories(token = token, market = countryCode, locale = locale)
//                    .categories.items
//
//                val semaphore = Semaphore(5)
//                coroutineScope {
//                    val deferredLists = categories.map { category ->
//                        async {
//                            semaphore.withPermit {
//                                try {
//                                    spotifyService.getPlaylistsForCategory(
//                                        token = token,
//                                        categoryId = category.id,
//                                        market = countryCode,
//                                        limit = 20,
//                                        offset = 0
//                                    ).toPlaylistSearchResultList()
//                                } catch (e: HttpException) {
//                                    if (e.code() == 404) emptyList<SearchResult.PlaylistSearchResult>()
//                                    else throw e
//                                }
//                            }
//                        }
//                    }
//                    deferredLists.awaitAll().mapIndexed { idx, playlists ->
//                        PlaylistsForCategory(
//                            categoryId = categories[idx].id,
//                            nameOfCategory = categories[idx].name,
//                            associatedPlaylists = playlists
//                        )
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("MusifyHomeFeedRepository", "Error fetching playlists by category", e)
//            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR, null)
//        }
//    }

    override suspend fun fetchPlaylistsByGenre(
        genre: SupportedSpotifyGenres,
        country: String
    ): FetchedResource<List<SearchResult.PlaylistSearchResult>, MusifyErrorType> {
        return try {
            tokenRepository.runCatchingWithToken { token ->
                spotifyService
                    .search(
                        searchQuery = genre.toString(),
                        market = country,
                        token = token,
                        type = "playlist",
                        limit = 20
                    )
                    .toSearchResults()
                    .playlists
            }
        } catch (e: Exception) {
            Log.e("MusifyHomeFeedRepository", "Error fetching playlists by genre", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR, null)
        }
    }
}
