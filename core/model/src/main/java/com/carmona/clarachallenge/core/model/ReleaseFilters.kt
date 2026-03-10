package com.carmona.clarachallenge.core.model

/**
 * Filter criteria for searching/filtering releases.
 * All parameters are optional - only provided filters will be applied.
 *
 * @param year Filter releases by specific year
 * @param genre Filter releases by music genre
 * @param label Filter releases by record label
 */
data class ReleaseFilters(
    val year: Int? = null,
    val genre: String? = null,
    val label: String? = null
)