package com.edgarvabo.hanoitower.data.remote

import com.edgarvabo.hanoitower.data.remote.dto.HanoiResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HanoiApiService {
    @GET("api/solve")
    suspend fun getSolution(
        @Query("disks") disks: Int
    ): Response<HanoiResponseDto>
}