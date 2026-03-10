/**
 * Artist Repository Implementation
 *
 * Implements the [ArtistRepository] interface using the Discogs API.
 * Converts network models to domain models and handles error cases.
 */
package com.carmona.clarachallenge.core.data.repository

import com.carmona.clarachallenge.core.model.Artist
import com.carmona.clarachallenge.core.model.ArtistDetails
import com.carmona.clarachallenge.core.model.Member
import com.carmona.clarachallenge.core.model.PaginatedResult
import com.carmona.clarachallenge.core.model.Release
import com.carmona.clarachallenge.core.model.Result
import com.carmona.clarachallenge.core.domain.repository.ArtistRepository
import com.carmona.clarachallenge.core.network.api.models.ArtistMember
import com.carmona.clarachallenge.core.network.api.models.ArtistRelease
import com.carmona.clarachallenge.core.network.api.models.ArtistSearchResult
import com.carmona.clarachallenge.core.network.api.DiscogsApi
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation that fetches artist data from the Discogs API.
 *
 * @param discogsApi The Discogs API client for network requests
 */
@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val discogsApi: DiscogsApi
) : ArtistRepository {

    /**
     * Searches for artists by query string.
     *
     * @param query The search term (artist name)
     * @param page Page number for pagination (starts at 1)
     * @return [Result] containing [PaginatedResult] of [Artist] on success, or [Result.Error] on failure
     */
    override suspend fun searchArtists(query: String, page: Int): Result<PaginatedResult<Artist>> {
        return try {
            val response = discogsApi.searchArtists(query, page)
                .getOrElse { exception ->
                    return Result.Error(exception.message ?: "Failed to search artists")
                }

            val artists = response.results.map { result: ArtistSearchResult ->
                Artist(
                    id = result.id,
                    name = result.title,
                    thumbnailUrl = result.cover_image ?: result.thumb
                )
            }

            Result.Success(
                PaginatedResult(
                    data = artists,
                    page = response.pagination.page,
                    hasMorePages = response.pagination.page < response.pagination.pages,
                    totalPages = response.pagination.pages,
                    totalItems = response.pagination.items
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to search artists: ${e::class.simpleName}")
        }
    }

    /**
     * Retrieves detailed information about a specific artist.
     *
     * @param artistId The Discogs artist ID (as String)
     * @return [Result] containing [ArtistDetails] on success, or [Result.Error] on failure
     */
    override suspend fun getArtistDetails(artistId: String): Result<ArtistDetails> {
        return try {
            val discogsArtist = discogsApi.getArtistDetails(artistId)
                .getOrElse { exception ->
                    return Result.Error(exception.message ?: "Failed to get artist details")
                }

            // Log the response to see what we're getting
            Timber.d("Artist details response: id=${discogsArtist.id}, name=${discogsArtist.name}, profile length=${discogsArtist.profile?.length}")

            // Convert network members to domain members
            val members = discogsArtist.members?.map { member: ArtistMember ->
                Member(
                    id = member.id.toString(), // Make sure this is converting to String
                    name = member.name,
                    role = null,
                    thumbnailUrl = null
                )
            } ?: emptyList()

            // Get the first image as thumbnail if available
            val thumbnailUrl = discogsArtist.images?.firstOrNull()?.uri

            val artistDetails = ArtistDetails(
                id = discogsArtist.id.toString(), // Convert to String
                name = discogsArtist.name,
                profile = discogsArtist.profile,
                thumbnailUrl = thumbnailUrl,
                members = members,
                yearFormed = null, // Discogs API might not provide this directly
                genres = null, // You might need to fetch this from elsewhere
                releasesCount = null // You might need to fetch this from elsewhere
            )

            Result.Success(artistDetails)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get artist details")
            Result.Error(e.message ?: "Failed to get artist details: ${e::class.simpleName}")
        }
    }

    /**
     * Retrieves releases (discography) for a specific artist.
     *
     * @param artistId The Discogs artist ID (as String)
     * @param page Page number for pagination (starts at 1)
     * @param filters Optional filters (sort, sort_order, etc.) as key-value pairs
     * @return [Result] containing [PaginatedResult] of [Release] on success, or [Result.Error] on failure
     */
    override suspend fun getArtistReleases(
        artistId: String,
        page: Int,
        filters: Map<String, String>
    ): Result<PaginatedResult<Release>> {
        return try {
            val response = discogsApi.getArtistReleases(artistId, page, filters)
                .getOrElse { exception ->
                    return Result.Error(exception.message ?: "Failed to get artist releases")
                }

            val releases = response.releases
                .map { release: ArtistRelease ->
                    Release(
                        id = release.id.toString(),
                        title = release.title,
                        type = release.type,
                        year = release.year,
                        label = release.label,
                        genre = null, // Genre not available in releases endpoint
                        thumbnailUrl = release.thumb
                    )
                }
                .sortedByDescending { it.year ?: 0 } // Sort by year descending (newest first)

            Result.Success(
                PaginatedResult(
                    data = releases,
                    page = response.pagination.page,
                    hasMorePages = response.pagination.page < response.pagination.pages,
                    totalPages = response.pagination.pages,
                    totalItems = response.pagination.items
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get artist releases: ${e::class.simpleName}")
        }
    }
}