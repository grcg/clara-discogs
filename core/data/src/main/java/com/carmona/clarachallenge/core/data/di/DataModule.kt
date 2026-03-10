/**
 * Dagger Hilt module providing data layer dependencies.
 * This module is installed in the singleton component to ensure repository instances are shared across the app.
 */
package com.carmona.clarachallenge.core.data.di

import com.carmona.clarachallenge.core.data.repository.ArtistRepositoryImpl
import com.carmona.clarachallenge.core.domain.repository.ArtistRepository
import com.carmona.clarachallenge.core.network.api.DiscogsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * Provides the ArtistRepository implementation.
     *
     * @param api The Discogs API client for making network requests
     * @return A singleton instance of ArtistRepositoryImpl
     */
    @Provides
    @Singleton
    fun provideArtistRepository(
        api: DiscogsApi
    ): ArtistRepository {
        return ArtistRepositoryImpl(api)
    }
}