package com.edgarvabo.hanoitower.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.edgarvabo.hanoitower.domain.model.HanoiSolution
import com.edgarvabo.hanoitower.domain.model.Move

@Keep
data class HanoiResponseDto(
    @SerializedName("disks") val disks: Int?,
    @SerializedName("totalMoves") val totalMoves: Long?,
    @SerializedName("executionTimeMs") val executionTimeMs: Double?,
    @SerializedName("moves") val moves: List<MoveDto>?
)

@Keep
data class MoveDto(
    @SerializedName("disk") val disk: Int?,
    @SerializedName("from") val fromRod: String?,
    @SerializedName("to") val toRod: String?
)

internal fun MoveDto.toDomain(): Move = Move(
    disk = this.disk ?: 0,
    fromRod = this.fromRod.orEmpty(),
    toRod = this.toRod.orEmpty()
)

internal fun HanoiResponseDto.toDomain(): HanoiSolution = HanoiSolution(
    disks = this.disks ?: 0,
    totalMoves = this.totalMoves ?: 0L,
    executionTimeMs = this.executionTimeMs ?: 0.0,
    moves = this.moves?.map { it.toDomain() } ?: emptyList()
)