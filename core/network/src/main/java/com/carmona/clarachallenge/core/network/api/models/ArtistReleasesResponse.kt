/**
 * Artist Releases Response Model
 *
 * Data classes representing the Discogs API artist releases response.
 * This matches the structure used in your existing code.
 */
package com.carmona.clarachallenge.core.network.api.models

import kotlinx.serialization.Serializable

/**
 * Response containing an artist's releases (discography).
 *
 * @param pagination Pagination metadata
 * @param releases List of releases
 */
@Serializable
data class ArtistReleasesResponse(
    val pagination: ReleasesPagination,
    val releases: List<ArtistRelease>
)

/**
 * Pagination information for releases.
 *
 * @param page Current page number
 * @param pages Total number of pages
 * @param perPage Items per page
 * @param items Total items across all pages
 */
@Serializable
data class ReleasesPagination(
    val page: Int,
    val pages: Int,
    val per_page: Int,
    val items: Int
)

/**
 * Release information for an artist's discography.
 *
 * @param id Release ID
 * @param title Release title
 * @param year Release year
 * @param type Release type (master, release, etc.)
 * @param format Physical format (CD, vinyl, digital, etc.)
 * @param label Record label
 * @param thumb Thumbnail image URL
 * @param role Artist's role on this release (main, producer, etc.)
 * @param artist Artist name (if different from main artist)
 */
@Serializable
data class ArtistRelease(
    val id: Long,
    val title: String,
    val year: Int? = null,
    val type: String,
    val format: String? = null,
    val label: String? = null,
    val thumb: String? = null,
    val role: String? = null,
    val artist: String? = null
)