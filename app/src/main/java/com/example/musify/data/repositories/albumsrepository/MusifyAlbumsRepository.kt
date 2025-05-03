package com.example.musify.data.repositories.albumsrepository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.musify.data.paging.AlbumsOfArtistPagingSource
import com.example.musify.data.remote.musicservice.SpotifyService
import com.example.musify.data.remote.response.toAlbumSearchResult
import com.example.musify.data.remote.response.toAlbumSearchResultList
import com.example.musify.data.repositories.tokenrepository.TokenRepository
import com.example.musify.data.repositories.tokenrepository.runCatchingWithToken
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MusifyAlbumsRepository @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val spotifyService: SpotifyService,
    private val pagingConfig: PagingConfig
) : AlbumsRepository {

    override suspend fun fetchAlbumsOfArtistWithId(
        artistId: String,
        countryCode: String
    ): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType> {
        Log.d("MusifyAlbumsRepository", "fetchAlbumsOfArtistWithId called with artistId=$artistId, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.getAlbumsOfArtistWithId(
                    artistId,
                    countryCode,
                    it
                ).toAlbumSearchResultList()
            }.also {
                Log.d("MusifyAlbumsRepository", "Successfully fetched albums for artistId=$artistId")
            }
        } catch (e: Exception) {
            Log.e("MusifyAlbumsRepository", "Error fetching albums for artistId=$artistId: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override suspend fun fetchAlbumWithId(
        albumId: String,
        countryCode: String
    ): FetchedResource<SearchResult.AlbumSearchResult, MusifyErrorType> {
        Log.d("MusifyAlbumsRepository", "fetchAlbumWithId called with albumId=$albumId, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.getAlbumWithId(albumId, countryCode, it).toAlbumSearchResult()
            }.also {
                Log.d("MusifyAlbumsRepository", "Successfully fetched album with albumId=$albumId")
            }
        } catch (e: Exception) {
            Log.e("MusifyAlbumsRepository", "Error fetching album with albumId=$albumId: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override fun getPaginatedStreamForAlbumsOfArtist(
        artistId: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.AlbumSearchResult>> {
        Log.d("MusifyAlbumsRepository", "getPaginatedStreamForAlbumsOfArtist called with artistId=$artistId, countryCode=$countryCode")
        return Pager(pagingConfig) {
            AlbumsOfArtistPagingSource(
                artistId = artistId,
                market = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifyAlbumsRepository", "Created paginated stream for artistId=$artistId")
        }
    }
}