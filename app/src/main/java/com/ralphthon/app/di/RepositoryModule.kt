package com.ralphthon.app.di

import com.ralphthon.app.data.repository.FavoritesRepository
import com.ralphthon.app.data.repository.FavoritesRepositoryImpl
import com.ralphthon.app.data.repository.MockBriefRepository
import com.ralphthon.app.data.repository.MockCardRepository
import com.ralphthon.app.data.repository.MockCustomerRepository
import com.ralphthon.app.data.repository.MockKnowledgeRepository
import com.ralphthon.app.data.repository.MockUploadRepository
import com.ralphthon.app.data.repository.SearchHistoryRepository
import com.ralphthon.app.data.repository.SearchHistoryRepositoryImpl
import com.ralphthon.app.data.repository.ThemeRepository
import com.ralphthon.app.data.repository.ThemeRepositoryImpl
import com.ralphthon.app.domain.repository.BriefRepository
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
import com.ralphthon.app.domain.repository.KnowledgeRepository
import com.ralphthon.app.domain.repository.UploadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: MockCustomerRepository
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(
        impl: MockCardRepository
    ): CardRepository

    @Binds
    @Singleton
    abstract fun bindKnowledgeRepository(
        impl: MockKnowledgeRepository
    ): KnowledgeRepository

    @Binds
    @Singleton
    abstract fun bindBriefRepository(
        impl: MockBriefRepository
    ): BriefRepository

    @Binds
    @Singleton
    abstract fun bindUploadRepository(
        impl: MockUploadRepository
    ): UploadRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        impl: FavoritesRepositoryImpl
    ): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        impl: SearchHistoryRepositoryImpl
    ): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: ThemeRepositoryImpl
    ): ThemeRepository
}
