package com.example.dto

import com.example.model.AttachmentModel

data class UserRequestDto (
    val userId: Long,
    val avatar: AttachmentModel
)