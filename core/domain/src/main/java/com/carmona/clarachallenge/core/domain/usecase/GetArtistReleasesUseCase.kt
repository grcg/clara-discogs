package com.carmona.clarachallenge.core.domain.usecase

import com.carmona.clarachallenge.core.model.PaginatedResult
import com.carmona.clarachallenge.core.model.Release
import com.carmona.clarachallenge.core.model.Result
import com.carmona.clarachallenge.core.domain.repository.ArtistRepository
import javax.inject.Inject

/**
 * Use case for fetching artist releases with pagination.
 *
 * @param repository The artist repository
 */
class GetArtistReleasesUseCase @Inject constructor(
    private val repository: ArtistRepository
) {
    /**
     * Executes the use case.
     *
     * @param artistId The artist ID (as String)
     * @param page Page number for pagination
     * @param filters Optional filters
     * @return [Result] containing [PaginatedResult] of [Release]
     */
    suspend operator fun invoke(
        artistId: String,
        page: Int,
        filters: Map<String, String>
    ): Result<PaginatedResult<Release>> {
        return repository.getArtistReleases(artistId, page, filters)
    }
}