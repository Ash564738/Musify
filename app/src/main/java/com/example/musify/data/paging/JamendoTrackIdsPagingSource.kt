package com.example.musify.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.musify.data.remote.musicservice.JamendoService
import com.example.musify.domain.SearchResult

class JamendoTrackIdsPagingSource(
    private val songIds: List<String>,
    private val jamendoService: JamendoService,
    private val clientId: String
) : PagingSource<Int, SearchResult.TrackSearchResult>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult.TrackSearchResult> {
        return try {
            val pageNumber = params.key ?: 0
            val validIds = songIds
                .mapNotNull { it.toIntOrNull() }
                .distinct()

            if (pageNumber >= validIds.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }

            val trackId = validIds[pageNumber]
            val response = jamendoService.getTrackById(
                clientId = clientId,
                trackId = trackId
            )

            if (response.isSuccessful) {
                val track = response.body()?.results?.firstOrNull()
                    ?.toTrackSearchResult()
                    ?: return LoadResult.Error(Exception("Invalid track ID: $trackId"))

                LoadResult.Page(
                    data = listOf(track),
                    prevKey = if (pageNumber > 0) pageNumber - 1 else null,
                    nextKey = if (pageNumber < validIds.size - 1) pageNumber + 1 else null
                )
            } else {
                LoadResult.Error(Exception("API error: ${response.message()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, SearchResult.TrackSearchResult>): Int? {
        Log.d(TAG, "Calculating refresh key with state: $state")
        return try {
            state.anchorPosition?.let { anchorPosition ->
                state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
            }.also {
                Log.d(TAG, "Refresh key calculated: $it")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating refresh key: ${e.message}", e)
            null
        }
    }
    companion object {
        private const val TAG = "JamendoTrackIdsPagingSource"
    }
}
