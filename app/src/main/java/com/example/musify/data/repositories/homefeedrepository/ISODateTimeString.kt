package com.example.musify.data.repositories.homefeedrepository

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * A helper class that is meant to contain methods used for converting
 * a timestamp to a string that conforms to ISO-8601 format (without milliseconds or timezone).
 */
class ISODateTimeString {
    companion object {
        private val formatter = DateTimeFormatter.ISO_INSTANT

        fun from(millis: Long): String = Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .format(formatter)
    }
}