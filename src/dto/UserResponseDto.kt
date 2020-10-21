package com.example.dto

import com.example.model.AttachmentModel
import com.example.model.UserModel

data class UserResponseDto(val id: Long, val username: String, val avatar: AttachmentModel?, val isReadOnly: Boolean) {
    companion object {
        fun fromModel(model: UserModel) = UserResponseDto(
                id = model.id,
                username = model.username,
                avatar = model.avatar,
                isReadOnly = model.isReadOnly
        )

        fun unknown() = UserResponseDto(
                id = 0,
                username = "unknown",
                avatar = null,
                isReadOnly = true
        )
    }
}
