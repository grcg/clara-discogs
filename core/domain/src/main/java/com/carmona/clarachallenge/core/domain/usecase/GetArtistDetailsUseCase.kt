package com.carmona.clarachallenge.core.domain.usecase

import com.carmona.clarachallenge.core.model.ArtistDetails
import com.carmona.clarachallenge.core.model.Result
import com.carmona.clarachallenge.core.domain.repository.ArtistRepository
import javax.inject.Inject

/**
 * Use case for fetching artist details.
 *
 * @param repository The artist repository
 */
class GetArtistDetailsUseCase @Inject constructor(
    private val repository: ArtistRepository
) {
    /**
     * Executes the use case.
     *
     * @param artistId The artist ID (as String to handle navigation arguments)
     * @return [Result] containing [ArtistDetails] on success
     */
    suspend operator fun invoke(artistId: String): Result<ArtistDetails> {
        return repository.getArtistDetails(artistId)
    }
}