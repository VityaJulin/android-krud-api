package com.example.dto

data class PostRequestDto(
    val id: Long,
    val sourceId: Long? = null,
    val content: String? = null,
    val link: String? = null,
    val attachmentId: String? = null
)