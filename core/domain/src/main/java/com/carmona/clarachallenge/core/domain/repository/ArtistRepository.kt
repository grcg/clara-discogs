package com.carmona.clarachallenge.core.domain.repository

import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.ArtistDetails
import com.carmona.clarachallenge.core.model.PaginatedResult
import com.carmona.clarachallenge.core.model.Release
import com.carmona.clarachallenge.core.model.Result

/**
 * Repository interface defining data operations for artist-related functionality.
 * Implementations should handle data source operations and error mapping.
 */
interface ArtistRepository {
    /**
     * Searches for artists matching the provided query.
     *
     * @param query Search query string
     * @param page Page number for pagination (starts at 1)
     * @return [Result] containing [PaginatedResult] of [Artist] on success
     */
    suspend fun searchArtists(query: String, page: Int): Result<PaginatedResult<Artist>>

    /**
     * Retrieves detailed information for a specific artist.
     *
     * @param artistId Unique identifier of the artist
     * @return [Result] containing [ArtistDetails] on success
     */
    suspend fun getArtistDetails(artistId: String): Result<ArtistDetails>

    /**
     * Retrieves paginated releases for a specific artist with optional filtering.
     *
     * @param artistId Unique identifier of the artist
     * @param page Page number for pagination (starts at 1)
     * @param filters Optional filter criteria as key-value pairs
     * @return [Result] containing [PaginatedResult] of [Release] on success
     */
    suspend fun getArtistReleases(
        artistId: String,
        page: Int,
        filters: Map<String, String> = emptyMap()
    ): Result<PaginatedResult<Release>>
}