/**
 * Ktor Implementation of DiscogsApi
 *
 * This class implements the [DiscogsApi] interface using Ktor HTTP client.
 * It handles all network communication with the Discogs REST API and
 * wraps responses in [Result] types for safe error handling.
 */
package com.carmona.clarachallenge.core.network

import com.carmona.clarachallenge.core.common.Constants
import com.carmona.clarachallenge.core.network.api.models.ArtistReleasesResponse
import com.carmona.clarachallenge.core.network.api.models.ArtistSearchResponse
import com.carmona.clarachallenge.core.network.api.DiscogsApi
import com.carmona.clarachallenge.core.network.api.models.DiscogsArtist
import com.carmona.clarachallenge.core.network.api.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton implementation of DiscogsApi using Ktor HttpClient.
 *
 * @param client The Ktor HTTP client, injected via Hilt
 */
@Singleton
class DiscogsApiImpl @Inject constructor(
    private val client: HttpClient
) : DiscogsApi {

    private companion object {
        /** Base URL for the Discogs API v2 */
        const val BASE_URL = "https://api.discogs.com"
    }

    /**
     * Searches for artists in the Discogs database.
     *
     * Endpoint: GET /database/search?type=artist&q={query}
     */
    override suspend fun searchArtists(
        query: String,
        page: Int
    ): Result<ArtistSearchResponse> = safeRequest {
        client.get("$BASE_URL/database/search") {
            parameter("q", query)
            parameter("type", "artist")
            parameter("per_page", Constants.DEFAULT_PAGE_SIZE)
            parameter("page", page)
        }
    }

    /**
     * Retrieves detailed artist information.
     *
     * Endpoint: GET /artists/{artistId}
     */
    override suspend fun getArtistDetails(
        artistId: String
    ): Result<DiscogsArtist> = safeRequest {
        client.get("$BASE_URL/artists/$artistId")
    }

    /**
     * Retrieves releases for an artist with pagination and optional filters.
     *
     * Endpoint: GET /artists/{artistId}/releases
     *
     * Common filters:
     * - "sort" -> "year" (sort by year)
     * - "sort_order" -> "desc" (descending order)
     */
    override suspend fun getArtistReleases(
        artistId: String,
        page: Int,
        filters: Map<String, String>
    ): Result<ArtistReleasesResponse> = safeRequest {
        client.get("$BASE_URL/artists/$artistId/releases") {
            parameter("per_page", Constants.DEFAULT_PAGE_SIZE)
            parameter("page", page)
            filters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}