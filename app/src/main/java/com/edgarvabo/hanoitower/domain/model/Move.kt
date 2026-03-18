package com.edgarvabo.hanoitower.domain.model

data class Move(
    val disk: Int,
    val fromRod: String,
    val toRod: String
)