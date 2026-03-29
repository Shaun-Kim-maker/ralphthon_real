package com.ralphthon.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ralphthon.app.data.api.BriefApiService
import com.ralphthon.app.data.api.CardApiService
import com.ralphthon.app.data.api.CustomerApiService
import com.ralphthon.app.data.api.KnowledgeApiService
import com.ralphthon.app.data.api.UploadApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://localhost:8080/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideCustomerApiService(retrofit: Retrofit): CustomerApiService =
        retrofit.create(CustomerApiService::class.java)

    @Provides
    @Singleton
    fun provideCardApiService(retrofit: Retrofit): CardApiService =
        retrofit.create(CardApiService::class.java)

    @Provides
    @Singleton
    fun provideKnowledgeApiService(retrofit: Retrofit): KnowledgeApiService =
        retrofit.create(KnowledgeApiService::class.java)

    @Provides
    @Singleton
    fun provideUploadApiService(retrofit: Retrofit): UploadApiService =
        retrofit.create(UploadApiService::class.java)

    @Provides
    @Singleton
    fun provideBriefApiService(retrofit: Retrofit): BriefApiService =
        retrofit.create(BriefApiService::class.java)

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
