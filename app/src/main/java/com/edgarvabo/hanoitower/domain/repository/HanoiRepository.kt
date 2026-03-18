package com.edgarvabo.hanoitower.domain.repository

import com.edgarvabo.hanoitower.domain.model.HanoiSolution

interface HanoiRepository {
    suspend fun getSolution(disks: Int): Result<HanoiSolution>
}