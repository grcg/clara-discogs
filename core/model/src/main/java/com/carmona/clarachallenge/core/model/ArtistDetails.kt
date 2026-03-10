package com.carmona.clarachallenge.core.model

/**
 * Detailed information about an artist, including members and statistics.
 *
 * @param id Unique identifier for the artist
 * @param name The artist's display name
 * @param profile Detailed profile/description of the artist
 * @param thumbnailUrl Optional URL to the artist's thumbnail/profile image
 * @param members List of band/group members (empty for solo artists)
 * @param releasesCount Optional total number of releases by this artist
 * @param yearFormed Optional year when the artist was formed or started
 * @param genres Optional list of music genres associated with the artist
 */
data class ArtistDetails(
    val id: String,
    val name: String,
    val profile: String?,
    val thumbnailUrl: String?,
    val members: List<Member> = emptyList(),
    val releasesCount: Int? = null,
    val yearFormed: Int? = null,
    val genres: List<String>? = null
)