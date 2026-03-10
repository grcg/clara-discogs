package com.carmona.clarachallenge.core.domain.usecase

import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.Result
import com.carmona.clarachallenge.core.domain.repository.ArtistRepository
import com.carmona.clarachallenge.core.model.PaginatedResult
import javax.inject.Inject

/**
 * Use case for searching artists with pagination.
 *
 * @param repository The artist repository
 */
class SearchArtistsUseCase @Inject constructor(
    private val repository: ArtistRepository
) {
    /**
     * Executes the search.
     *
     * @param query Search query string (minimum 2 characters)
     * @param page Page number for pagination (starts at 1)
     * @return [Result] containing [PaginatedResult] of [Artist]
     */
    suspend operator fun invoke(query: String, page: Int = 1): Result<PaginatedResult<Artist>> {
        return repository.searchArtists(query, page)
    }
}