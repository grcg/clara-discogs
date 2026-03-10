package com.carmona.clarachallenge.core.network.api.models

import kotlinx.serialization.Serializable

/**
 * Image information for artist.
 *
 * @param type Image type (primary, secondary, etc.)
 * @param uri Image URI
 * @param width Image width in pixels
 * @param height Image height in pixels
 */
@Serializable
data class ArtistImage(
    val type: String,
    val uri: String,
    val width: Int,
    val height: Int
)