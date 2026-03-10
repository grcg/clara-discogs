package com.carmona.clarachallenge.core.model

/**
 * Represents an artist/musician in the system.
 *
 * @param id Unique identifier for the artist
 * @param name The artist's display name
 * @param bio Optional biographical information about the artist
 * @param thumbnailUrl Optional URL to the artist's thumbnail/profile image
 * @param yearFormed Optional year when the artist was formed or started
 * @param genres Optional list of music genres associated with the artist
 */
data class Artist(
    val id: String,
    val name: String,
    val bio: String? = null,
    val thumbnailUrl: String?,
    val yearFormed: Int? = null,
    val genres: List<String>? = null
)