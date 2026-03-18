package com.edgarvabo.hanoitower.di

import com.edgarvabo.hanoitower.data.remote.HanoiApiService
import com.edgarvabo.hanoitower.data.repository.HanoiRepositoryImpl
import com.edgarvabo.hanoitower.domain.repository.HanoiRepository
import com.edgarvabo.hanoitower.domain.usecase.GetHanoiSolutionUseCase
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Esto imprimirá todo el JSON en el Logcat
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS) // Le damos 30 segundos a Google Cloud para despertar
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://hanoi-backend-api-457038519914.europe-west1.run.app/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHanoiApiService(retrofit: Retrofit): HanoiApiService {
        return retrofit.create(HanoiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideHanoiRepository(
        apiService: HanoiApiService,
        ioDispatcher: CoroutineDispatcher
    ): HanoiRepository {
        return HanoiRepositoryImpl(apiService, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideGetHanoiSolutionUseCase(repository: HanoiRepository): GetHanoiSolutionUseCase {
        return GetHanoiSolutionUseCase(repository)
    }
}