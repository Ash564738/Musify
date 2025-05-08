package com.example.musify.data.remote.musicservice

import android.content.res.Resources
import android.util.DisplayMetrics
import com.example.musify.domain.SearchResult
import com.example.musify.utils.Constants.JAMENDO_CLIENT_ID
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
//
//enum class SupportedJamendoTags(private val queryStringValue: String) {
//    POP("pop"),
//    ROCK("rock"),
//    JAZZ("jazz"),
//    ELECTRONIC("electro"),
//    HIPHOP("hiphop");
//
//    override fun toString() = queryStringValue
//}
//
//fun SupportedJamendoTags.toGenre(): Genre {
//    val genreType = getGenreType()
//    val name = when (genreType) {
//        GenreType.POP -> "Pop"
//        GenreType.ROCK -> "Rock"
//        GenreType.JAZZ -> "Jazz"
//        GenreType.ELECTRONIC -> "Electronic"
//        GenreType.HIPHOP -> "Hip-Hop"
//        else -> "Unknown"
//    }
//    return Genre(
//        id = "$ordinal:${this.name}",
//        label = name,
//        genreType = genreType
//    )
//}
//
//private fun SupportedJamendoTags.getGenreType() = when (this) {
//    SupportedJamendoTags.POP -> GenreType.POP
//    SupportedJamendoTags.ROCK -> GenreType.ROCK
//    SupportedJamendoTags.JAZZ -> GenreType.JAZZ
//    SupportedJamendoTags.ELECTRONIC -> GenreType.ELECTRONIC
//    SupportedJamendoTags.HIPHOP -> GenreType.HIPHOP
//}
private fun isHighDpiDisplay(): Boolean {
    val densityDpi = Resources.getSystem().displayMetrics.densityDpi
    return densityDpi >= DisplayMetrics.DENSITY_HIGH
}
interface JamendoService {
    @GET("albums")
    suspend fun getNewAlbums(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("order") order: String = "releasedate_desc",
        @Query("imagesize") imageSize: Int = when {
            isHighDpiDisplay() -> 600
            else -> 300
        },
        @Query("audioformat") audioFormat: String = "mp32"
    ): Response<JamendoAlbumsResponse>

    @GET("playlists")
    suspend fun getFeaturedPlaylists(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("namesearch") nameSearch: String = "featured",
        @Query("order") order: String? = null,
        @Query("format") format: String = "json"
    ): Response<JamendoPlaylistsResponse>

    @GET("tracks")
    suspend fun getPlaylistTracks(
        @Query("client_id") clientId: String,
        @Query("id") trackIds: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = when {
            isHighDpiDisplay() -> 600
            else -> 300
        }
        ): Response<JamendoTracksResponse>

    @GET("tracks")
    suspend fun getArtistTracks(
        @Query("client_id") clientId: String,
        @Query("artist_id") artistId: String,
        @Query("limit") limit: Int,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = when {
            isHighDpiDisplay() -> 600
            else -> 300
        }
        ): Response<JamendoTracksResponse>

    @GET("tracks")
    suspend fun getAlbumTracks(
        @Query("client_id") clientId: String,
        @Query("id") albumId: String,
        @Query("format") format: String = "json",
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = when {
            isHighDpiDisplay() -> 600
            else -> 300
        }
    ): Response<JamendoTracksResponse>

    @GET("tracks")
    suspend fun searchTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("namesearch") nameSearch: String? = null,
        @Query("tags") tags: String? = null,
        @Query("fuzzytags") fuzzyTags: String? = null,
        @Query("type") type: String? = null,
        @Query("album_id") albumId: String? = null,
        @Query("artist_id") artistId: String? = null,
        @Query("datebetween") dateBetween: String? = null,
        @Query("durationbetween") durationBetween: String? = null,
        @Query("search") freeText: String? = null,
        @Query("order") order: String? = null,
        @Query("boost") boost: String? = null,
        @Query("include") include: String? = null,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("audiodlformat") audioDownloadFormat: String? = null,
        @Query("prolicensing") proLicensing: Boolean? = null,
        @Query("probackground") proBackground: Boolean? = null,
        @Query("ccsa") ccsa: Boolean? = null,
        @Query("ccnd") ccnd: Boolean? = null,
        @Query("ccnc") ccnc: Boolean? = null,
        @Query("fullcount") fullcount: Boolean? = null
    ): Response<JamendoTracksResponse>
}


@JsonClass(generateAdapter = true)
data class JamendoHeaders(
    @Json(name = "status") val status: String?,
    @Json(name = "code") val code: Int?,
    @Json(name = "error_message") val errorMessage: String?,
    @Json(name = "warnings") val warnings: String?,
    @Json(name = "results_count") val resultsCount: Int?
)

@JsonClass(generateAdapter = true)
data class JamendoTracksResponse(
    @Json(name = "results") val results: List<JamendoTrack>,
    @Json(name = "headers") val headers: JamendoHeaders
)

@JsonClass(generateAdapter = true)
data class JamendoPlaylistTracksResponse(
    @Json(name = "results") val results: List<JamendoTrack>,
    @Json(name = "headers") val headers: JamendoHeaders
)

@JsonClass(generateAdapter = true)
data class JamendoTrack(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "artist_id") val artistId: String? = null,
    @Json(name = "artist_name") val artistName: String? = null,
    @Json(name = "audio") val audioUrl: String?,
    @Json(name = "image") val imageUrl: String?,
    @Json(name = "shareurl") val shareUrl: String?,
    @Json(name = "shorturl") val shortUrl: String?,
    @Json(name = "audiodownload_allowed") val audioDownloadAllowed: Boolean? = null,
    @Json(name = "audiodownload") val audioDownload: String? = null
) {
    fun toTrackSearchResult() = SearchResult.TrackSearchResult(
        id = id,
        name = name,
        imageUrlString = imageUrl ?: "",
        artistsString = artistName ?: "",
        trackUrlString = audioUrl
    )
}

@JsonClass(generateAdapter = true)
data class JamendoAlbumsResponse(
    @Json(name = "results") val results: List<JamendoAlbum>,
    @Json(name = "headers") val headers: JamendoHeaders
)

@JsonClass(generateAdapter = true)
data class JamendoAlbum(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "artist_id") val artistId: String,
    @Json(name = "artist_name") val artistName: String,
    @Json(name = "image") val image: String,
    @Json(name = "releasedate") val releaseDate: String,
    @Json(name = "zip") val zip: String?,
    @Json(name = "zip_allowed") val zipAllowed: Boolean?,
    @Json(name = "shorturl") val shortUrl: String?,
    @Json(name = "shareurl") val shareUrl: String?
)

@JsonClass(generateAdapter = true)
data class JamendoPlaylistsResponse(
    @Json(name = "results") val results: List<JamendoPlaylist>,
    @Json(name = "headers") val headers: JamendoHeaders
)

@JsonClass(generateAdapter = true)
data class JamendoPlaylist(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "creationdate") val creationDate: String?,
    @Json(name = "zip") val zip: String?,
    @Json(name = "zip_allowed") val zipAllowed: Boolean?,
    @Json(name = "shorturl") val shortUrl: String?,
    @Json(name = "shareurl") val shareUrl: String?
)
