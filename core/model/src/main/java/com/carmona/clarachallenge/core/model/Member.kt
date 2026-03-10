package com.carmona.clarachallenge.core.model

/**
 * Represents a member of a band or musical group.
 *
 * @param id Unique identifier for the member
 * @param name The member's name
 * @param role Optional role or instrument played in the group
 * @param thumbnailUrl Optional URL to the member's photo/image
 */
data class Member(
    val id: String,
    val name: String,
    val role: String?,
    val thumbnailUrl: String?
)