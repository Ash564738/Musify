package com.example.musify.domain

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val streamUrl: String?,
    val imageUrl: String
) : Streamable {
    constructor() : this("", "", "", "", 0L, null, "")

    override val streamInfo: StreamInfo
        get() = StreamInfo(
            streamUrl = streamUrl,
            imageUrl = imageUrl,
            title = title,
            subtitle = artist
        )

    fun toMap() = mapOf(
        "id" to id,
        "title" to title,
        "artist" to artist,
        "streamUrl" to streamUrl,
        "imageUrl" to imageUrl
    )
}