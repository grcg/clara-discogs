/**
 * Dagger Hilt module for the artist feature.
 * Provides the navigation implementation for the artist feature.
 */
package com.carmona.clarachallenge.feature.artist.impl.di

import com.carmona.clarachallenge.feature.artist.api.ArtistsNavigation
import com.carmona.clarachallenge.feature.artist.impl.navigation.ArtistNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ArtistModule {

    /**
     * Binds the ArtistNavigator implementation to the ArtistsNavigation interface.
     *
     * @param artistsNavigator The concrete navigator implementation
     * @return The navigation interface for dependency injection
     */
    @Binds
    @Singleton
    fun bindArtistsNavigation(
        artistsNavigator: ArtistNavigator
    ): ArtistsNavigation
}