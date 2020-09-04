package com.example.dto

import com.example.model.AttachmentModel
import com.example.model.MediaType

data class AttachmentResponseDto(val id: String, val mediaType: MediaType) {
    companion object {
        fun fromModel(model: AttachmentModel) = AttachmentResponseDto(
            id = model.id,
            mediaType = model.mediaType
        )
    }
}