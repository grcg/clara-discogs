/**
 * Discogs API Interface
 *
 * Defines the contract for all Discogs API operations. This interface is
 * implemented by [DiscogsApiImpl] and provided via dependency injection.
 *
 * All methods are suspending functions for coroutine support and return
 * [Result] types to handle both success and failure cases gracefully.
 */
package com.carmona.clarachallenge.core.network.api

import com.carmona.clarachallenge.core.network.api.models.ArtistReleasesResponse
import com.carmona.clarachallenge.core.network.api.models.ArtistSearchResponse
import com.carmona.clarachallenge.core.network.api.models.DiscogsArtist

/**
 * Interface defining all network operations against the Discogs API.
 */
interface DiscogsApi {

    /**
     * Searches for artists in the Discogs database.
     *
     * @param query The search term (artist name)
     * @param page Page number for pagination (starts at 1)
     * @return [Result] containing [com.carmona.clarachallenge.core.network.api.models.ArtistSearchResponse] on success, or an exception on failure
     */
    suspend fun searchArtists(
        query: String,
        page: Int
    ): Result<ArtistSearchResponse>

    /**
     * Retrieves detailed information about a specific artist.
     *
     * @param artistId The Discogs artist ID
     * @return [Result] containing [com.carmona.clarachallenge.core.network.api.models.DiscogsArtist] on success, or an exception on failure
     */
    suspend fun getArtistDetails(
        artistId: String
    ): Result<DiscogsArtist>

    /**
     * Retrieves releases (discography) for a specific artist.
     *
     * @param artistId The Discogs artist ID
     * @param page Page number for pagination
     * @param filters Optional filters (e.g., "sort" -> "year", "sort_order" -> "desc")
     * @return [Result] containing [com.carmona.clarachallenge.core.network.api.models.ArtistReleasesResponse] on success, or an exception on failure
     */
    suspend fun getArtistReleases(
        artistId: String,
        page: Int,
        filters: Map<String, String> = emptyMap()
    ): Result<ArtistReleasesResponse>
}