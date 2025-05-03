package com.example.musify.data.repositories.podcastsrepository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.musify.data.paging.PodcastEpisodesForPodcastShowPagingSource
import com.example.musify.data.remote.musicservice.SpotifyService
import com.example.musify.data.remote.response.toPodcastEpisode
import com.example.musify.data.remote.response.toPodcastShow
import com.example.musify.data.repositories.tokenrepository.TokenRepository
import com.example.musify.data.repositories.tokenrepository.runCatchingWithToken
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.PodcastEpisode
import com.example.musify.domain.PodcastShow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MusifyPodcastsRepository @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val spotifyService: SpotifyService,
    private val pagingConfig: PagingConfig
) : PodcastsRepository {

    override suspend fun fetchPodcastEpisode(
        episodeId: String,
        countryCode: String
    ): FetchedResource<PodcastEpisode, MusifyErrorType> {
        Log.d("MusifyPodcastsRepository", "fetchPodcastEpisode called with episodeId=$episodeId, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.getEpisodeWithId(
                    token = it, id = episodeId, market = countryCode
                ).toPodcastEpisode()
            }.also {
                Log.d("MusifyPodcastsRepository", "Successfully fetched podcast episode with episodeId=$episodeId")
            }
        } catch (e: Exception) {
            Log.e("MusifyPodcastsRepository", "Error fetching podcast episode with episodeId=$episodeId: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override suspend fun fetchPodcastShow(
        showId: String,
        countryCode: String
    ): FetchedResource<PodcastShow, MusifyErrorType> {
        Log.d("MusifyPodcastsRepository", "fetchPodcastShow called with showId=$showId, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.getShowWithId(
                    token = it, id = showId, market = countryCode
                ).toPodcastShow()
            }.also {
                Log.d("MusifyPodcastsRepository", "Successfully fetched podcast show with showId=$showId")
            }
        } catch (e: Exception) {
            Log.e("MusifyPodcastsRepository", "Error fetching podcast show with showId=$showId: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override fun getPodcastEpisodesStreamForPodcastShow(
        showId: String,
        countryCode: String
    ): Flow<PagingData<PodcastEpisode>> {
        Log.d("MusifyPodcastsRepository", "getPodcastEpisodesStreamForPodcastShow called with showId=$showId, countryCode=$countryCode")
        return Pager(pagingConfig) {
            PodcastEpisodesForPodcastShowPagingSource(
                showId = showId,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifyPodcastsRepository", "Created paginated stream for podcast episodes of showId=$showId")
        }
    }
}