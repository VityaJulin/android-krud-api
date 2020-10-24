package com.example.model

import com.example.dto.UserResponseDto


enum class ReactionType {
    LIKE, DISLIKE
}

data class Reaction(
        val user: UserResponseDto,
        val date: Long,
        val type: ReactionType
)