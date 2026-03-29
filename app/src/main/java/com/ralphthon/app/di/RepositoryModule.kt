package com.ralphthon.app.di

import com.ralphthon.app.data.repository.MockCardRepository
import com.ralphthon.app.data.repository.MockCustomerRepository
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
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
}
