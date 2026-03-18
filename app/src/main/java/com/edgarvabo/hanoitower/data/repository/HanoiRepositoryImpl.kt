package com.edgarvabo.hanoitower.data.repository

import com.edgarvabo.hanoitower.data.remote.HanoiApiService
import com.edgarvabo.hanoitower.data.remote.dto.toDomain
import com.edgarvabo.hanoitower.domain.model.HanoiSolution
import com.edgarvabo.hanoitower.domain.repository.HanoiRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

internal class HanoiRepositoryImpl(
    private val apiService: HanoiApiService,
    private val ioDispatcher: CoroutineDispatcher
) : HanoiRepository {

    override suspend fun getSolution(disks: Int): Result<HanoiSolution> = withContext(ioDispatcher) {
        try {
            val response = apiService.getSolution(disks)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it.toDomain()) }
                    ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red. Verifica tu internet.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}