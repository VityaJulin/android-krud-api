package com.example.dto

import com.example.model.Reaction
import com.example.model.ReactionType

data class StatisticResponseDto(
        val user: UserResponseDto,
        val date: Long,
        val type: ReactionType
) {
    companion object {
        fun fromModel(model: Reaction) = StatisticResponseDto(
                user = model.user,
                date = model.date,
                type = model.type
        )
    }
}