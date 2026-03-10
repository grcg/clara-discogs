/**
 * Dagger Hilt module for the discography feature.
 * Provides the navigation implementation for the discography feature.
 */
package com.carmona.clarachallenge.feature.discography.impl.di

import com.carmona.clarachallenge.feature.discography.api.DiscographyNavigation
import com.carmona.clarachallenge.feature.discography.impl.navigation.DiscographyNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DiscographyModule {

    /**
     * Binds the DiscographyNavigator implementation to the DiscographyNavigation interface.
     *
     * @param discographyNavigator The concrete navigator implementation
     * @return The navigation interface for dependency injection
     */
    @Binds
    @Singleton
    fun bindDiscographyNavigation(
        discographyNavigator: DiscographyNavigator
    ): DiscographyNavigation
}