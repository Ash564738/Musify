package com.example.musify.data.remote.musicservice

import android.content.res.Resources
import android.util.DisplayMetrics
import com.example.musify.domain.Genre
import com.example.musify.domain.Genre.GenreType
import com.example.musify.domain.SearchResult
import com.example.musify.utils.Constants.DEFAULT_TRACK_IMAGE_URL
import com.example.musify.utils.Constants.JAMENDO_CLIENT_ID
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

enum class SupportedJamendoTags(private val queryStringValue: String) {
    POP("pop"),
    ROCK("rock"),
    JAZZ("jazz"),
    ELECTRONIC("electro"),
    AMBIENT("ambient"),
    CHILL("chill"),
    CLASSICAL("classical"),
    DANCE("dance"),
    METAL("metal"),
    RAINY_DAY("rainy_day"),
    PIANO("piano"),
    SLEEP("sleep"),
    ENERGETIC("energetic"),
    GUITAR("guitar"),
    ELECTRIC_GUITAR("electric_guitar"),
    SAD("sad"),
    SYNTHESIZER("synthesizer"),
    FUNK("funk");

    override fun toString() = queryStringValue
}

fun SupportedJamendoTags.toGenre(): Genre {
    val genreType = getGenreType()
    val name = when (genreType) {
        GenreType.POP -> "Pop"
        GenreType.ROCK -> "Rock"
        GenreType.JAZZ -> "Jazz"
        GenreType.ELECTRONIC -> "Electronic"
        GenreType.AMBIENT -> "Ambient"
        GenreType.CHILL -> "Chill"
        GenreType.CLASSICAL -> "Classical"
        GenreType.DANCE -> "Dance"
        GenreType.METAL -> "Metal"
        GenreType.RAINY_DAY -> "Rainy Day"
        GenreType.PIANO -> "Piano"
        GenreType.SLEEP -> "Sleep"
        GenreType.ENERGETIC -> "Energetic"
        GenreType.GUITAR -> "Guitar"
        GenreType.ELECTRIC_GUITAR -> "Electric Guitar"
        GenreType.SAD -> "Sad"
        GenreType.SYNTHESIZER -> "Synthesizer"
        GenreType.FUNK -> "Funk"
    }
    return Genre(
        id = "$ordinal:${this.name}",
        label = name,
        genreType = genreType
    )
}

private fun SupportedJamendoTags.getGenreType() = when (this) {
    SupportedJamendoTags.POP -> GenreType.POP
    SupportedJamendoTags.ROCK -> GenreType.ROCK
    SupportedJamendoTags.JAZZ -> GenreType.JAZZ
    SupportedJamendoTags.ELECTRONIC -> GenreType.ELECTRONIC
    SupportedJamendoTags.AMBIENT -> GenreType.AMBIENT
    SupportedJamendoTags.CHILL -> GenreType.CHILL
    SupportedJamendoTags.CLASSICAL -> GenreType.CLASSICAL
    SupportedJamendoTags.DANCE -> GenreType.DANCE
    SupportedJamendoTags.METAL -> GenreType.METAL
    SupportedJamendoTags.RAINY_DAY -> GenreType.RAINY_DAY
    SupportedJamendoTags.PIANO -> GenreType.PIANO
    SupportedJamendoTags.SLEEP -> GenreType.SLEEP
    SupportedJamendoTags.ENERGETIC -> GenreType.ENERGETIC
    SupportedJamendoTags.GUITAR -> GenreType.GUITAR
    SupportedJamendoTags.ELECTRIC_GUITAR -> GenreType.ELECTRIC_GUITAR
    SupportedJamendoTags.SAD -> GenreType.SAD
    SupportedJamendoTags.SYNTHESIZER -> GenreType.SYNTHESIZER
    SupportedJamendoTags.FUNK -> GenreType.FUNK
}

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

    @GET("albums")
    suspend fun getAlbumsByTag(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("tags") tags: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("imagesize") imageSize: Int = if (isHighDpiDisplay()) 600 else 300,
        @Query("format") format: String = "json"
    ): Response<JamendoAlbumsResponse>

    @GET("albums/tracks")
    suspend fun getAlbumTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("id") albumId: String,
        @Query("order") order: String = "track_position_asc",
        @Query("format") format: String = "json",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 200,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = if (isHighDpiDisplay()) 600 else 300,
    ): Response<JamendoAlbumTracksResponse>

    @GET("playlists")
    suspend fun getPlaylistsByTag(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("tags") tags: String?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("imagesize") imageSize: Int = if (isHighDpiDisplay()) 600 else 300,
        @Query("format") format: String = "json"
    ): Response<JamendoPlaylistsResponse>

    @GET("playlists/tracks")
    suspend fun getPlaylistTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("id") playlistId: String,
        @Query("order") order: String = "track_position_asc",
        @Query("format") format: String = "json",
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = if (isHighDpiDisplay()) 600 else 300,
        @Query("track_type") trackType: String? = "single albumtrack"
    ): Response<JamendoPlaylistTracksResponse>

    @GET("tracks")
    suspend fun getArtistTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("artist_id") artistId: String,
        @Query("limit") limit: Int,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = when {
            isHighDpiDisplay() -> 600
            else -> 300
        }
        ): Response<JamendoArtistTracksResponse>
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
data class JamendoTrack(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "position") val position: String? = null,
    @Json(name = "artist_id") val artistId: String? = null,
    @Json(name = "artist_name") val artistName: String? = null,
    @Json(name = "audio") val audioUrl: String?,
    @Json(name = "image") val imageUrl: String?,
    @Json(name = "album_image") val albumImageUrl: String?,
    @Json(name = "shareurl") val shareUrl: String?,
    @Json(name = "shorturl") val shortUrl: String?,
    @Json(name = "audiodownload_allowed") val audioDownloadAllowed: Boolean? = null,
    @Json(name = "audiodownload") val audioDownload: String? = null
) {
    fun toTrackSearchResult() = SearchResult.TrackSearchResult(
        id = id,
        name = name,
        imageUrlString = imageUrl
            ?: albumImageUrl
            ?: DEFAULT_TRACK_IMAGE_URL,
        artistsString = artistName ?: "",
        trackUrlString = audioUrl,
        duration = duration ?: 0,
        trackPosition = position?.toIntOrNull() ?: 0,
        audioDownloadAllowed = audioDownloadAllowed == true,
        audioDownloadUrl = audioDownload ?: "",
        shareUrl = shareUrl ?: "",
        shortUrl = shortUrl ?: ""
    )
}
@JsonClass(generateAdapter = true)
data class JamendoArtistTracksResponse(
    @Json(name = "results") val results: List<JamendoTrack>,
    @Json(name = "headers") val headers: JamendoHeaders
)

@JsonClass(generateAdapter = true)
data class JamendoAlbumTracksResponse(
    @Json(name = "headers") val headers: JamendoHeaders,
    @Json(name = "results") val results: List<JamendoAlbumWithTracks>
)

@JsonClass(generateAdapter = true)
data class JamendoAlbumWithTracks(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "artist_id") val artistId: String,
    @Json(name = "artist_name") val artistName: String,
    @Json(name = "image") val image: String,
    @Json(name = "releasedate") val releaseDate: String,
    @Json(name = "zip") val zip: String?,
    @Json(name = "zip_allowed") val zipAllowed: Boolean?,
    @Json(name = "shorturl") val shortUrl: String?,
    @Json(name = "shareurl") val shareUrl: String?,
    @Json(name = "tracks") val tracks: List<JamendoTrack>
)

@JsonClass(generateAdapter = true)
data class JamendoPlaylistTracksResponse(
    @Json(name = "results") val results: List<JamendoPlaylistWithTracks>,
    @Json(name = "headers") val headers: JamendoHeaders
)

@JsonClass(generateAdapter = true)
data class JamendoPlaylistWithTracks(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "creationdate") val creationDate: String?,
    @Json(name = "zip") val zip: String?,
    @Json(name = "zip_allowed") val zipAllowed: Boolean?,
    @Json(name = "shorturl") val shortUrl: String?,
    @Json(name = "shareurl") val shareUrl: String?,
    @Json(name = "tracks") val tracks: List<JamendoTrack>
)

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