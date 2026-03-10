package com.carmona.clarachallenge.core.model

/**
 * Represents a music release (album, single, EP, etc.).
 *
 * @param id Unique identifier for the release
 * @param title The release's title
 * @param type Type of release (e.g., "Album", "Single", "EP")
 * @param year Optional year of release
 * @param label Optional record label that published the release
 * @param genre Optional primary genre of the release
 * @param thumbnailUrl Optional URL to the release's cover/image
 * @param trackCount Optional number of tracks in the release
 */
data class Release(
    val id: String,
    val title: String,
    val type: String,
    val year: Int?,
    val label: String?,
    val genre: String?,
    val thumbnailUrl: String?,
    val trackCount: Int? = null
)