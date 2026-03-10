/**
 * Search Feature Hilt Module
 *
 * This module provides the SearchNavigation interface binding.
 * It follows the exact same pattern as Artist and Discography modules.
 */
package com.carmona.clarachallenge.feature.search.impl.di

import com.carmona.clarachallenge.feature.search.api.SearchNavigation
import com.carmona.clarachallenge.feature.search.impl.navigation.SearchNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for search feature dependencies.
 * Uses @Binds to tell Hilt which implementation to use for SearchNavigation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {
    /**
     * Binds the SearchNavigator implementation to the SearchNavigation interface.
     *
     * @param searchNavigator The concrete navigator implementation
     * @return The navigation interface for dependency injection
     */
    @Binds
    @Singleton
    abstract fun bindSearchNavigator(
        searchNavigator: SearchNavigator
    ): SearchNavigation
}