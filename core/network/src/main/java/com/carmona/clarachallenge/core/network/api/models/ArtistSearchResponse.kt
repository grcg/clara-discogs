/**
 * Artist Search Response Model
 *
 * Data class representing the Discogs API artist search response.
 * This matches the structure used in your existing code.
 */
package com.carmona.clarachallenge.core.network.api.models

import kotlinx.serialization.Serializable

/**
 * Response object for artist searches.
 *
 * @param pagination Pagination metadata
 * @param results List of search result items
 */
@Serializable
data class ArtistSearchResponse(
    val pagination: SearchPagination,
    val results: List<ArtistSearchResult>
)

/**
 * Pagination information returned by the Discogs API.
 *
 * @param page Current page number
 * @param pages Total number of pages available
 * @param perPage Items per page
 * @param items Total items across all pages
 * @param urls URLs for next/previous pages (optional)
 */
@Serializable
data class SearchPagination(
    val page: Int,
    val pages: Int,
    val per_page: Int,
    val items: Int,
    val urls: Map<String, String>? = null
)

/**
 * Individual search result representing an artist.
 *
 * @param id Discogs artist ID
 * @param title Artist name
 * @param thumb Thumbnail image URL
 * @param coverImage Full cover image URL
 * @param type Result type (should be "artist")
 * @param genre List of genres associated with the artist
 * @param style List of styles associated with the artist
 * @param year Year of first release (may be null)
 */
@Serializable
data class ArtistSearchResult(
    val id: String,
    val title: String,
    val thumb: String,
    val cover_image: String? = null,
    val type: String,
    val genre: List<String>? = null,
    val style: List<String>? = null,
    val year: String? = null
)