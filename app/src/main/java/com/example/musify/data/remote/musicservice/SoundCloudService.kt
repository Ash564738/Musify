package com.example.musify.data.remote.musicservice
//
//import retrofit2.http.GET
//import retrofit2.http.Query
//import com.google.gson.annotations.SerializedName // Fix for SerializedName
//
//interface SoundCloudService {
//    @GET("tracks")
//    suspend fun searchTracks(
//        @Query("client_id") clientId: String,
//        @Query("q")          query:    String,
//        @Query("limit")      limit:    Int = 20
//    ): List<SCTrack>
//}
//
//data class SCTrack(
//    val id: Int,
//    val title: String,
//    @SerializedName("stream_url") val streamUrl: String,
//    @SerializedName("artwork_url") val imageUrl:  String?
//)
//
//fun SCTrack.toDomainTrack() = Track(
//    id       = id.toString(),
//    title    = title,
//    artist   = "",
//    imageUrl = imageUrl ?: "",
//    audioUrl = "$streamUrl?client_id=$YOUR_CLIENT_ID"
//)