package com.example.musify.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.musify.data.remote.musicservice.JamendoService
import com.example.musify.domain.SearchResult
import kotlinx.coroutines.delay
import java.io.IOException

class JamendoPlaylistTracksPagingSource(
    private val playlistId: String,
    private val jamendoService: JamendoService,
    private val clientId: String
) : PagingSource<Int, SearchResult.TrackSearchResult>() {
    private var retryCount = 0

    override fun getRefreshKey(state: PagingState<Int, SearchResult.TrackSearchResult>): Int? {
        return try {
            val refreshKey = state.anchorPosition?.let { anchorPosition ->
                state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
            }
            Log.d(TAG, "Calculated refresh key: $refreshKey")
            refreshKey
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating refresh key: ${e.message}", e)
            null
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult.TrackSearchResult> {
        val position = params.key ?: 0
        Log.d(TAG, "Starting load with playlistId=$playlistId, position=$position, loadSize=${params.loadSize}")
        return try {
            val response = jamendoService.getPlaylistTracks(
                clientId = clientId,
                playlistId = playlistId,
                offset = position,
                limit = params.loadSize
            )

            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody?.headers?.code != 0) {
                    val errorMessage = "API error: ${responseBody?.headers?.errorMessage}"
                    Log.e(TAG, errorMessage)
                    throw IOException(errorMessage)
                }

                val results = responseBody.results
                    .flatMap { playlist -> playlist.tracks }
                    .filter { jamendoTrack -> jamendoTrack.audioUrl != null }
                    .map { jamendoTrack -> jamendoTrack.toTrackSearchResult() }

                Log.d(TAG, "Successfully loaded ${results.size} tracks for playlistId=$playlistId")
                LoadResult.Page(
                    data = results,
                    prevKey = if (position == 0) null else position - params.loadSize,
                    nextKey = if (results.isEmpty()) null else position + params.loadSize
                )
            } else {
                val errorMessage = "HTTP error: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMessage)
                throw IOException(errorMessage)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in load: ${e.message}", e)
            if (retryCount < MAX_RETRIES) {
                retryCount++
                Log.d(TAG, "Retrying load (attempt $retryCount/$MAX_RETRIES) after delay of $RETRY_DELAY_MS ms")
                delay(RETRY_DELAY_MS)
                return load(params)
            } else {
                Log.e(TAG, "Max retries reached. Failing load.", e)
                return LoadResult.Error(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in load: ${e.message}", e)
            return LoadResult.Error(e)
        }
    }

    companion object {
        private const val TAG = "JamendoPlaylistTracksPagingSource"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
    }
}