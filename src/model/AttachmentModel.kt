package com.example.model

enum class MediaType {
    IMAGE
}

data class AttachmentModel(val id: String, val mediaType: MediaType)