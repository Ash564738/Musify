package com.example.musify.domain

import java.util.Date

data class Playlist(
    val id: String = "",
    val name: String = "",
    val songIds: List<String> = emptyList(),
    val createdAt: Date = Date()
) {
    @Suppress("unused")
    constructor() : this("", "", emptyList(), Date())
}