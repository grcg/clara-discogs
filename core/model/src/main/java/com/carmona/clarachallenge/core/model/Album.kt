package com.carmona.clarachallenge.core.model

/**
 * Represents a music album in the system.
 *
 * @param id Unique identifier for the album
 * @param title The album's title
 * @param artistId Unique identifier of the artist who created the album
 * @param artistName Name of the artist for display purposes
 * @param releaseDate Date when the album was released (format depends on data source)
 * @param trackCount Number of tracks in the album
 * @param coverArtUrl Optional URL to the album's cover artwork image
 */
data class Album(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val releaseDate: String,
    val trackCount: Int,
    val coverArtUrl: String?
)