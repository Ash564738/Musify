package com.example.musify.data.remote.musicservice

import com.example.musify.data.remote.response.*
import com.example.musify.data.remote.token.BearerToken
import com.example.musify.domain.Genre
import com.example.musify.domain.Genre.GenreType
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * An enum that contains all the generes supported by the spotify api.
 * Note: It is not an exhaustive collection of all the genres supported
 * by the spotify api.
 * @param queryStringValue depicts the actual string value that will be
 * appended to the query when the GET request is made. This value will
 * be the returned when [SupportedSpotifyGenres.toString] is called.
 */
enum class SupportedSpotifyGenres(private val queryStringValue: String) {
    AMBIENT("ambient"),
    CHILL("chill"),
    CLASSICAL("classical"),
    DANCE("dance"),
    ELECTRONIC("electronic"),
    METAL("metal"),
    RAINY_DAY("rainy-day"),
    ROCK("rock"),
    PIANO("piano"),
    POP("pop"),
    SLEEP("sleep");

    override fun toString() = queryStringValue

}

fun SupportedSpotifyGenres.toGenre(): Genre {
    val genreType = getGenreType()
    val name = when (genreType) {
        GenreType.POP -> "Pop"
        GenreType.ROCK -> "Rock"
        GenreType.JAZZ -> "Jazz"
        GenreType.ELECTRONIC -> "Electronic"
        else -> "Unknown"
    }
    return Genre(
        id = "$ordinal:${this.name}",
        label = name,
        genreType = genreType
    )
}

private fun SupportedSpotifyGenres.getGenreType() = when (this) {
    SupportedSpotifyGenres.AMBIENT -> Genre.GenreType.AMBIENT
    SupportedSpotifyGenres.CHILL -> Genre.GenreType.CHILL
    SupportedSpotifyGenres.CLASSICAL -> Genre.GenreType.CLASSICAL
    SupportedSpotifyGenres.DANCE -> Genre.GenreType.DANCE
    SupportedSpotifyGenres.ELECTRONIC -> Genre.GenreType.ELECTRONIC
    SupportedSpotifyGenres.METAL -> Genre.GenreType.METAL
    SupportedSpotifyGenres.RAINY_DAY -> Genre.GenreType.RAINY_DAY
    SupportedSpotifyGenres.ROCK -> Genre.GenreType.ROCK
    SupportedSpotifyGenres.PIANO -> Genre.GenreType.PIANO
    SupportedSpotifyGenres.POP -> Genre.GenreType.POP
    SupportedSpotifyGenres.SLEEP -> Genre.GenreType.SLEEP
}

interface SpotifyService {
    @GET(SpotifyEndPoints.SPECIFIC_ARTIST_ENDPOINT)
    suspend fun getArtistInfoWithId(
        @Path("id") artistId: String,
        @Header("Authorization") token: BearerToken,
    ): ArtistResponse

    @GET(SpotifyEndPoints.SPECIFIC_ARTIST_ALBUMS_ENDPOINT)
    suspend fun getAlbumsOfArtistWithId(
        @Path("id") artistId: String,
        @Query("market") market: String,
        @Header("Authorization") token: BearerToken,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("include_groups") includeGroups: String? = null,
    ): AlbumsMetadataResponse

    @GET(SpotifyEndPoints.TOP_TRACKS_ENDPOINT)
    suspend fun getTopTenTracksForArtistWithId(
        @Path("id") artistId: String,
        @Query("market") market: String,
        @Header("Authorization") token: BearerToken
    ): TracksWithAlbumMetadataListResponse

    @GET(SpotifyEndPoints.SPECIFIC_ALBUM_ENDPOINT)
    suspend fun getAlbumWithId(
        @Path("id") albumId: String,
        @Query("market") market: String,
        @Header("Authorization") token: BearerToken
    ): AlbumResponse

    @GET(SpotifyEndPoints.SEARCH_ENDPOINT)
    suspend fun search(
        @Query("q") searchQuery: String,
        @Query("market") market: String,
        @Header("Authorization") token: BearerToken,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("type") type: String = SpotifyEndPoints.Defaults.defaultSearchQueryTypes,
    ): SearchResultsResponse

    @GET(SpotifyEndPoints.RECOMMENDATIONS_ENDPOINT)
    suspend fun getTracksForGenre(
        @Query("seed_genres") genre: SupportedSpotifyGenres,
        @Query("market") market: String,
        @Header("Authorization") token: BearerToken,
        @Query("limit") limit: Int = 20
    ): TracksWithAlbumMetadataListResponse

    @GET(SpotifyEndPoints.PLAYLIST_TRACKS_ENDPOINT)
    suspend fun getTracksForPlaylist(
        @Path("playlist_id") playlistId: String,
        @Query("market") market: String,
        @Header("Authorization") token: BearerToken,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): PlaylistItemsResponse

    @GET(SpotifyEndPoints.NEW_RELEASES_ENDPOINT)
    suspend fun getNewReleases(
        @Header("Authorization") token: BearerToken,
        @Query("country") market: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): NewReleasesResponse

    @GET(SpotifyEndPoints.FEATURED_PLAYLISTS)
    suspend fun getFeaturedPlaylists(
        @Header("Authorization") token: BearerToken,
        @Query("country") market: String,
        @Query("locale") locale: String = "", // ISO 639-1 language code and an uppercase ISO 3166-1 alpha-2 country code, joined by an underscore.
        @Query("timestamp") timestamp: String = "", // A timestamp in ISO 8601 format: yyyy-MM-ddTHH:mm:ss
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): FeaturedPlaylistsResponse

    @GET(SpotifyEndPoints.BROWSE_CATEGORIES_FOR_COUNTRY_AND_LOCALE_ENDPOINT)
    suspend fun getBrowseCategories(
        @Header("Authorization") token: BearerToken,
        @Query("country") market: String,
        @Query("locale") locale: String, // ISO 639-1 language code and an uppercase ISO 3166-1 alpha-2 country code, joined by an underscore.
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): BrowseCategoriesResponse

    @GET(SpotifyEndPoints.PLAYLISTS_FOR_BESPOKE_CATEGORY)
    suspend fun getPlaylistsForCategory(
        @Header("Authorization") token: BearerToken,
        @Path("category_id") categoryId: String,
        @Query("country") market: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): PlaylistsForSpecificCategoryResponse

    @GET(SpotifyEndPoints.SPECIFIC_EPISODE_ENDPOINT)
    suspend fun getEpisodeWithId(
        @Header("Authorization") token: BearerToken,
        @Path("id") id: String,
        @Query("market") market: String
    ): EpisodeResponse

    @GET(SpotifyEndPoints.SPECIFIC_SHOW_ENDPOINT)
    suspend fun getShowWithId(
        @Header("Authorization") token: BearerToken,
        @Path("id") id: String,
        @Query("market") market: String
    ): ShowResponse

    @GET(SpotifyEndPoints.SHOW_EPISODES_ENDPOINT)
    suspend fun getEpisodesForShowWithId(
        @Header("Authorization") token: BearerToken,
        @Path("id") id: String,
        @Query("market") market: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): EpisodesWithPreviewUrlResponse
}