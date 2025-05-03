package com.example.musify.data.repositories.tracksrepository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.musify.data.paging.PlaylistTracksPagingSource
import com.example.musify.data.remote.musicservice.SpotifyService
import com.example.musify.data.remote.response.getTracks
import com.example.musify.data.remote.response.toTrackSearchResult
import com.example.musify.data.repositories.tokenrepository.TokenRepository
import com.example.musify.data.repositories.tokenrepository.runCatchingWithToken
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.Genre
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import com.example.musify.domain.toSupportedSpotifyGenreType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MusifyTracksRepository @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val spotifyService: SpotifyService,
    private val pagingConfig: PagingConfig
) : TracksRepository {

    override suspend fun fetchTopTenTracksForArtistWithId(
        artistId: String,
        countryCode: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        Log.d("MusifyTracksRepository", "fetchTopTenTracksForArtistWithId called with artistId=$artistId, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.getTopTenTracksForArtistWithId(
                    artistId = artistId,
                    market = countryCode,
                    token = it,
                ).value.mapNotNull { trackDTOWithAlbumMetadata ->
                    val trackSearchResult = trackDTOWithAlbumMetadata.toTrackSearchResult()
                    if (trackSearchResult.trackUrlString != null) trackSearchResult else null
                }
            }.also {
                Log.d("MusifyTracksRepository", "Successfully fetched top ten tracks for artistId=$artistId")
            }
        } catch (e: Exception) {
            Log.e("MusifyTracksRepository", "Error fetching top ten tracks for artistId=$artistId: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override suspend fun fetchTracksForGenre(
        genre: Genre,
        countryCode: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        Log.d("MusifyTracksRepository", "fetchTracksForGenre called with genre=${genre.label}, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.getTracksForGenre(
                    genre = genre.genreType.toSupportedSpotifyGenreType(),
                    market = countryCode,
                    token = it
                ).value.mapNotNull { trackDTOWithAlbumMetadata ->
                    val trackSearchResult = trackDTOWithAlbumMetadata.toTrackSearchResult()
                    if (trackSearchResult.trackUrlString != null) trackSearchResult else null
                }
            }.also {
                Log.d("MusifyTracksRepository", "Successfully fetched tracks for genre=${genre.label}")
            }
        } catch (e: Exception) {
            Log.e("MusifyTracksRepository", "Error fetching tracks for genre=${genre.label}: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override suspend fun fetchTracksForAlbumWithId(
        albumId: String,
        countryCode: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        Log.d("MusifyTracksRepository", "fetchTracksForAlbumWithId called with albumId=$albumId, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.getAlbumWithId(albumId, countryCode, it).getTracks()
                    .let { tracks ->
                        tracks.filter { track -> track.trackUrlString != null }
                    }
            }.also {
                Log.d("MusifyTracksRepository", "Successfully fetched tracks for albumId=$albumId")
            }
        } catch (e: Exception) {
            Log.e("MusifyTracksRepository", "Error fetching tracks for albumId=$albumId: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override fun getPaginatedStreamForPlaylistTracks(
        playlistId: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.TrackSearchResult>> {
        Log.d("MusifyTracksRepository", "getPaginatedStreamForPlaylistTracks called with playlistId=$playlistId, countryCode=$countryCode")
        return Pager(pagingConfig) {
            PlaylistTracksPagingSource(
                playlistId = playlistId,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifyTracksRepository", "Created paginated stream for playlistId=$playlistId")
        }
    }
}