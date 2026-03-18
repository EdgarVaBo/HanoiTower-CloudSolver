package com.edgarvabo.hanoitower.domain.model

data class HanoiSolution(
    val disks: Int,
    val totalMoves: Long,
    val executionTimeMs: Double,
    val moves: List<Move>
)