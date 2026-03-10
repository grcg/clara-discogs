/**
 * Discogs Artist Model
 *
 * Data class representing a Discogs artist from the API response.
 */
package com.carmona.clarachallenge.core.network.api.models

import kotlinx.serialization.Serializable

/**
 * Detailed artist information from Discogs.
 *
 * @param id Discogs artist ID
 * @param name Artist name
 * @param realname Real name of the artist (if applicable)
 * @param profile Artist biography/profile text
 * @param images List of associated images
 * @param urls External URLs (official website, social media, etc.)
 * @param members List of group members (for bands)
 * @param data_quality Data quality rating
 */
@Serializable
data class DiscogsArtist(
    val id: String,
    val name: String,
    val realname: String? = null,
    val profile: String? = null,
    val images: List<ArtistImage>? = null,
    val urls: List<String>? = null,
    val members: List<ArtistMember>? = null,
    val data_quality: String? = null
)