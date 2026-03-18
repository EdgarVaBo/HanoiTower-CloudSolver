package com.edgarvabo.hanoitower.domain.usecase

import com.edgarvabo.hanoitower.domain.model.HanoiSolution
import com.edgarvabo.hanoitower.domain.repository.HanoiRepository

class GetHanoiSolutionUseCase(
    private val repository: HanoiRepository
) {
    suspend operator fun invoke(disks: Int): Result<HanoiSolution> {
        if (disks <= 0) return Result.failure(IllegalArgumentException("El número de discos debe ser mayor a 0"))
        return repository.getSolution(disks)
    }
}