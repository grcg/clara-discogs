package com.carmona.clarachallenge.core.network.api.models

import kotlinx.serialization.Serializable

/**
 * Member information for groups/bands.
 *
 * @param id Member artist ID
 * @param name Member name
 * @param active Whether member is currently active
 */
@Serializable
data class ArtistMember(
    val id: String,
    val name: String,
    val active: Boolean
)